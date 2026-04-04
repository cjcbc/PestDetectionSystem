"""
测试集验证脚本
对验证集进行逐图片评估，计算每个类别的Precision、Recall、F1-Score。
对比模型预测与真实标签，生成详细的评估报告和可视化对比图。
"""

import os
import json
import cv2
import numpy as np
import matplotlib.pyplot as plt
from pathlib import Path
from collections import defaultdict
from ultralytics import YOLO
import yaml

# ==================== 配置参数（直接在此修改） ====================
MODEL_PATH = "best.pt"                          # 训练好的模型路径
DATA_CONFIG = r"D:\SHU files\Graduation project\model\Dataset\classes.yaml"  # 数据集配置文件
IMG_DIR = r"D:\SHU files\Graduation project\model\Dataset\images\val"        # 验证集图片目录
LABEL_DIR = r"D:\SHU files\Graduation project\model\Dataset\labels\val"      # 验证集标签目录
CONF_THRESHOLD = 0.25                           # 置信度阈值
IOU_THRESHOLD = 0.5                             # IoU阈值（用于匹配预测和真实框）
DEVICE = 0                                      # 推理设备：0（GPU）或 "cpu"
OUTPUT_DIR = "./validation_results"             # 输出目录
SAVE_VISUALIZATIONS = True                      # 是否保存可视化结果
MAX_VIS_SAMPLES = 50                            # 最多保存多少张可视化图片
# ================================================================

def load_class_names(data_config):
    """从YAML配置文件加载类别名称"""
    with open(data_config, 'r', encoding='utf-8') as f:
        config = yaml.safe_load(f)
    return config.get('names', {})

def parse_yolo_label(label_path, img_width, img_height):
    """解析YOLO格式的标签文件，返回绝对坐标的边界框"""
    boxes = []
    if not os.path.exists(label_path):
        return boxes
    
    with open(label_path, 'r') as f:
        for line in f.readlines():
            parts = line.strip().split()
            if len(parts) >= 5:
                cls_id = int(parts[0])
                x_center = float(parts[1]) * img_width
                y_center = float(parts[2]) * img_height
                width = float(parts[3]) * img_width
                height = float(parts[4]) * img_height
                
                x1 = x_center - width / 2
                y1 = y_center - height / 2
                x2 = x_center + width / 2
                y2 = y_center + height / 2
                
                boxes.append({
                    'class': cls_id,
                    'bbox': [x1, y1, x2, y2]
                })
    return boxes

def calculate_iou(box1, box2):
    """计算两个边界框的IoU"""
    x1 = max(box1[0], box2[0])
    y1 = max(box1[1], box2[1])
    x2 = min(box1[2], box2[2])
    y2 = min(box1[3], box2[3])
    
    inter_area = max(0, x2 - x1) * max(0, y2 - y1)
    
    box1_area = (box1[2] - box1[0]) * (box1[3] - box1[1])
    box2_area = (box2[2] - box2[0]) * (box2[3] - box2[1])
    
    union_area = box1_area + box2_area - inter_area
    
    if union_area == 0:
        return 0
    return inter_area / union_area

def evaluate_single_image(pred_boxes, gt_boxes, iou_threshold=0.5):
    """评估单张图片的预测结果"""
    num_classes = max([b['class'] for b in gt_boxes] + [0]) + 1
    
    tp = defaultdict(int)  # True Positives
    fp = defaultdict(int)  # False Positives
    fn = defaultdict(int)  # False Negatives
    
    matched_gt = set()
    
    # 对每个预测框，寻找最佳匹配的真实框
    for pred in pred_boxes:
        pred_cls = pred['class']
        pred_bbox = pred['bbox']
        pred_conf = pred.get('conf', 0)
        
        best_iou = 0
        best_gt_idx = -1
        
        for gt_idx, gt in enumerate(gt_boxes):
            if gt_idx in matched_gt:
                continue
            
            iou = calculate_iou(pred_bbox, gt['bbox'])
            if iou > best_iou and iou >= iou_threshold:
                best_iou = iou
                best_gt_idx = gt_idx
        
        if best_gt_idx >= 0 and gt_boxes[best_gt_idx]['class'] == pred_cls:
            tp[pred_cls] += 1
            matched_gt.add(best_gt_idx)
        else:
            fp[pred_cls] += 1
    
    # 统计未匹配的GT框为FN
    for gt_idx, gt in enumerate(gt_boxes):
        if gt_idx not in matched_gt:
            fn[gt['class']] += 1
    
    return tp, fp, fn

def run_validation():
    """运行完整的验证流程"""
    print("=" * 60)
    print("开始验证集评估...")
    print("=" * 60)
    
    # 创建输出目录
    output_dir = Path(OUTPUT_DIR)
    output_dir.mkdir(parents=True, exist_ok=True)
    vis_dir = output_dir / "visualizations"
    if SAVE_VISUALIZATIONS:
        vis_dir.mkdir(parents=True, exist_ok=True)
    
    # 加载模型和类别名称
    print(f"\n加载模型: {MODEL_PATH}")
    model = YOLO(MODEL_PATH)
    class_names = load_class_names(DATA_CONFIG)
    num_classes = len(class_names)
    print(f"类别数量: {num_classes}")
    
    # 获取所有验证集图片
    img_extensions = ['*.jpg', '*.jpeg', '*.png', '*.bmp', '*.tif']
    img_paths = []
    for ext in img_extensions:
        img_paths.extend(Path(IMG_DIR).glob(ext))
    img_paths.sort()
    
    print(f"验证集图片数量: {len(img_paths)}")
    
    # 统计所有类别的TP, FP, FN
    total_tp = defaultdict(int)
    total_fp = defaultdict(int)
    total_fn = defaultdict(int)
    total_gt = defaultdict(int)
    total_pred = defaultdict(int)
    
    images_with_detections = 0
    images_without_detections = 0
    vis_count = 0
    
    print("\n开始逐张图片评估...")
    
    for img_idx, img_path in enumerate(img_paths):
        if (img_idx + 1) % 100 == 0:
            print(f"  处理进度: {img_idx + 1}/{len(img_paths)}")
        
        # 读取图片
        img = cv2.imread(str(img_path))
        if img is None:
            continue
        
        img_height, img_width = img.shape[:2]
        
        # 获取对应的标签文件
        label_path = Path(LABEL_DIR) / f"{img_path.stem}.txt"
        gt_boxes = parse_yolo_label(str(label_path), img_width, img_height)
        
        # 统计GT框
        for gt in gt_boxes:
            total_gt[gt['class']] += 1
        
        # 运行模型推理
        results = model([str(img_path)], conf=CONF_THRESHOLD, device=DEVICE, verbose=False)
        result = results[0]
        
        # 解析预测结果
        pred_boxes = []
        if result.boxes is not None and len(result.boxes) > 0:
            boxes = result.boxes
            xyxy = boxes.xyxy.cpu().numpy()
            confs = boxes.conf.cpu().numpy()
            cls_ids = boxes.cls.cpu().numpy().astype(int)
            
            for i in range(len(xyxy)):
                pred_boxes.append({
                    'class': int(cls_ids[i]),
                    'bbox': xyxy[i].tolist(),
                    'conf': float(confs[i])
                })
                total_pred[int(cls_ids[i])] += 1
            
            images_with_detections += 1
        else:
            images_without_detections += 1
        
        # 评估单张图片
        tp, fp, fn = evaluate_single_image(pred_boxes, gt_boxes, IOU_THRESHOLD)
        
        for cls_id in set(list(tp.keys()) + list(fp.keys()) + list(fn.keys())):
            total_tp[cls_id] += tp[cls_id]
            total_fp[cls_id] += fp[cls_id]
            total_fn[cls_id] += fn[cls_id]
        
        # 保存可视化结果
        if SAVE_VISUALIZATIONS and vis_count < MAX_VIS_SAMPLES:
            vis_img = img.copy()
            
            # 绘制GT框（绿色）
            for gt in gt_boxes:
                x1, y1, x2, y2 = [int(x) for x in gt['bbox']]
                cls_name = class_names.get(gt['class'], f"Class {gt['class']}")
                cv2.rectangle(vis_img, (x1, y1), (x2, y2), (0, 255, 0), 2)
                cv2.putText(vis_img, f"GT: {cls_name}", (x1, y1 - 10),
                           cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)
            
            # 绘制预测框（蓝色）
            for pred in pred_boxes:
                x1, y1, x2, y2 = [int(x) for x in pred['bbox']]
                cls_name = class_names.get(pred['class'], f"Class {pred['class']}")
                cv2.rectangle(vis_img, (x1, y1), (x2, y2), (255, 0, 0), 2)
                cv2.putText(vis_img, f"Pred: {cls_name} ({pred['conf']:.2f})", 
                           (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 1)
            
            save_path = vis_dir / f"{img_path.stem}_vis.jpg"
            cv2.imwrite(str(save_path), vis_img)
            vis_count += 1
    
    # 计算每个类别的Precision, Recall, F1
    print("\n" + "=" * 60)
    print("评估结果汇总")
    print("=" * 60)
    
    class_metrics = []
    all_precisions = []
    all_recalls = []
    all_f1s = []
    
    for cls_id in range(num_classes):
        tp = total_tp[cls_id]
        fp = total_fp[cls_id]
        fn = total_fn[cls_id]
        
        precision = tp / (tp + fp) if (tp + fp) > 0 else 0
        recall = tp / (tp + fn) if (tp + fn) > 0 else 0
        f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0
        
        class_metrics.append({
            'class_id': cls_id,
            'class_name': class_names.get(cls_id, f"Class {cls_id}"),
            'tp': tp,
            'fp': fp,
            'fn': fn,
            'precision': precision,
            'recall': recall,
            'f1': f1
        })
        
        all_precisions.append(precision)
        all_recalls.append(recall)
        all_f1s.append(f1)
    
    # 计算总体指标
    avg_precision = np.mean(all_precisions)
    avg_recall = np.mean(all_recalls)
    avg_f1 = np.mean(all_f1s)
    
    total_tp_sum = sum(total_tp.values())
    total_fp_sum = sum(total_fp.values())
    total_fn_sum = sum(total_fn.values())
    
    overall_precision = total_tp_sum / (total_tp_sum + total_fp_sum) if (total_tp_sum + total_fp_sum) > 0 else 0
    overall_recall = total_tp_sum / (total_tp_sum + total_fn_sum) if (total_tp_sum + total_fn_sum) > 0 else 0
    overall_f1 = 2 * overall_precision * overall_recall / (overall_precision + overall_recall) if (overall_precision + overall_recall) > 0 else 0
    
    # 打印结果
    print(f"\n{'类别ID':<8} {'类别名称':<40} {'TP':<6} {'FP':<6} {'FN':<6} {'Precision':<10} {'Recall':<10} {'F1':<10}")
    print("-" * 100)
    
    for metric in class_metrics:
        print(f"{metric['class_id']:<8} {metric['class_name']:<40} {metric['tp']:<6} {metric['fp']:<6} {metric['fn']:<6} {metric['precision']:<10.4f} {metric['recall']:<10.4f} {metric['f1']:<10.4f}")
    
    print("\n" + "=" * 60)
    print("总体指标")
    print("=" * 60)
    print(f"平均 Precision: {avg_precision:.4f}")
    print(f"平均 Recall:    {avg_recall:.4f}")
    print(f"平均 F1-Score:  {avg_f1:.4f}")
    print(f"\n总体 Precision: {overall_precision:.4f}")
    print(f"总体 Recall:    {overall_recall:.4f}")
    print(f"总体 F1-Score:  {overall_f1:.4f}")
    print(f"\n检测到目标的图片: {images_with_detections}")
    print(f"未检测到目标的图片: {images_without_detections}")
    print(f"可视化图片已保存: {vis_count} 张")
    
    # 保存结果到JSON
    results = {
        'model_path': MODEL_PATH,
        'conf_threshold': CONF_THRESHOLD,
        'iou_threshold': IOU_THRESHOLD,
        'num_images': len(img_paths),
        'images_with_detections': images_with_detections,
        'images_without_detections': images_without_detections,
        'overall_metrics': {
            'precision': overall_precision,
            'recall': overall_recall,
            'f1': overall_f1
        },
        'average_metrics': {
            'precision': avg_precision,
            'recall': avg_recall,
            'f1': avg_f1
        },
        'class_metrics': class_metrics
    }
    
    results_path = output_dir / "validation_results.json"
    with open(results_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    print(f"\n详细结果已保存: {results_path}")
    
    # 绘制指标图表
    plot_metrics(class_metrics, output_dir)
    
    print("\n验证完成！")

def plot_metrics(class_metrics, output_dir):
    """绘制各类别指标图表"""
    # 提取数据
    class_names = [m['class_name'][:20] for m in class_metrics]  # 截断长名称
    precisions = [m['precision'] for m in class_metrics]
    recalls = [m['recall'] for m in class_metrics]
    f1s = [m['f1'] for m in class_metrics]
    
    # 创建图表
    fig, axes = plt.subplots(1, 3, figsize=(18, 6))
    
    # Precision
    axes[0].bar(range(len(precisions)), precisions, color='steelblue')
    axes[0].set_title('Precision per Class')
    axes[0].set_xlabel('Class')
    axes[0].set_ylabel('Precision')
    axes[0].set_xticks(range(len(precisions)))
    axes[0].set_xticklabels(class_names, rotation=90, fontsize=6)
    axes[0].set_ylim(0, 1.05)
    
    # Recall
    axes[1].bar(range(len(recalls)), recalls, color='coral')
    axes[1].set_title('Recall per Class')
    axes[1].set_xlabel('Class')
    axes[1].set_ylabel('Recall')
    axes[1].set_xticks(range(len(recalls)))
    axes[1].set_xticklabels(class_names, rotation=90, fontsize=6)
    axes[1].set_ylim(0, 1.05)
    
    # F1-Score
    axes[2].bar(range(len(f1s)), f1s, color='seagreen')
    axes[2].set_title('F1-Score per Class')
    axes[2].set_xlabel('Class')
    axes[2].set_ylabel('F1-Score')
    axes[2].set_xticks(range(len(f1s)))
    axes[2].set_xticklabels(class_names, rotation=90, fontsize=6)
    axes[2].set_ylim(0, 1.05)
    
    plt.tight_layout()
    plot_path = output_dir / "metrics_comparison.png"
    plt.savefig(plot_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"指标对比图已保存: {plot_path}")
    
    # 绘制Top/Bottom类别
    sorted_by_f1 = sorted(class_metrics, key=lambda x: x['f1'], reverse=True)
    top_10 = sorted_by_f1[:10]
    bottom_10 = sorted_by_f1[-10:]
    
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))
    
    # Top 10
    axes[0].barh(range(len(top_10)), [m['f1'] for m in top_10], color='seagreen')
    axes[0].set_yticks(range(len(top_10)))
    axes[0].set_yticklabels([m['class_name'] for m in top_10], fontsize=8)
    axes[0].set_xlabel('F1-Score')
    axes[0].set_title('Top 10 Classes by F1-Score')
    axes[0].set_xlim(0, 1.05)
    
    # Bottom 10
    axes[1].barh(range(len(bottom_10)), [m['f1'] for m in bottom_10], color='salmon')
    axes[1].set_yticks(range(len(bottom_10)))
    axes[1].set_yticklabels([m['class_name'] for m in bottom_10], fontsize=8)
    axes[1].set_xlabel('F1-Score')
    axes[1].set_title('Bottom 10 Classes by F1-Score')
    axes[1].set_xlim(0, 1.05)
    
    plt.tight_layout()
    plot_path = output_dir / "top_bottom_classes.png"
    plt.savefig(plot_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"Top/Bottom类别图已保存: {plot_path}")

if __name__ == "__main__":
    run_validation()
