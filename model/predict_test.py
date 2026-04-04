"""
模型推理脚本
从验证集随机选择一张图片进行目标检测推理。
在图片上同时绘制预测框（蓝色）和真实标签框（绿色），
显示置信度、预测结果和实际标签，并用matplotlib展示。
"""

import json
import random
from pathlib import Path

import cv2
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
import yaml
from ultralytics import YOLO

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False

# ==================== 配置参数（直接在此修改） ====================
MODEL_PATH = "best.pt"  # 训练好的模型路径
VAL_IMG_DIR = r"D:\SHU files\Graduation project\model\Dataset\images\val"      # 验证集图片目录
VAL_LABEL_DIR = r"D:\SHU files\Graduation project\model\Dataset\labels\val"    # 验证集标签目录
DATA_CONFIG = r"D:\SHU files\Graduation project\model\Dataset\classes.yaml"    # 数据集配置文件
CONF_THRESHOLD = 0.25                           # 置信度阈值
DEVICE = 0                                      # 推理设备：0（GPU）或 "cpu"
# ================================================================

def load_class_names(config_path):
    """从YAML配置文件加载类别名称"""
    with open(config_path, 'r', encoding='utf-8') as f:
        config = yaml.safe_load(f)
    return config.get('names', {})

# 缓存模型，避免每次请求重复加载
MODEL = YOLO(MODEL_PATH)
CLASS_NAMES = load_class_names(DATA_CONFIG)

def parse_yolo_label(label_path, img_width, img_height):
    """解析YOLO格式的标签文件，返回绝对坐标的边界框"""
    boxes = []
    if not Path(label_path).exists():
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

def _result_to_json(result, image_name="image"):
    """将单张图片推理结果转换为完整JSON。"""
    pred_info = []
    if result.boxes is not None and len(result.boxes) > 0:
        boxes = result.boxes
        xyxy = boxes.xyxy.cpu().numpy()
        confs = boxes.conf.cpu().numpy()
        cls_ids = boxes.cls.cpu().numpy().astype(int)

        for i in range(len(xyxy)):
            cls_id = int(cls_ids[i])
            cls_name = CLASS_NAMES.get(cls_id, f"Class {cls_id}")
            x1, y1, x2, y2 = [int(x) for x in xyxy[i]]
            conf = float(confs[i])
            pred_info.append({
                "class_id": cls_id,
                "class_name": cls_name,
                "bbox": [x1, y1, x2, y2],
                "confidence": round(conf, 6),
            })

    return {
        "success": True,
        "image_name": image_name,
        "image_size": {
            "width": int(result.orig_shape[1]),
            "height": int(result.orig_shape[0]),
        },
        "threshold": CONF_THRESHOLD,
        "device": str(DEVICE),
        "predictions": pred_info,
        "prediction_count": len(pred_info),
    }

def predict_image(img, image_name="image", show=False, save=False):
    """对输入图片执行推理，默认只返回JSON数据。"""
    if img is None:
        return {
            "success": False,
            "message": "图片读取失败",
            "predictions": [],
        }, None, None

    result = MODEL.predict(source=img, conf=CONF_THRESHOLD, device=DEVICE, verbose=False)[0]
    json_result = _result_to_json(result, image_name=image_name)

    annotated_img = None
    if show or save:
        annotated_img = result.plot()

    if save and annotated_img is not None:
        output_dir = Path("./inference_results")
        output_dir.mkdir(parents=True, exist_ok=True)
        save_path = output_dir / f"{Path(image_name).stem}.jpg"
        cv2.imwrite(str(save_path), annotated_img)
        json_result["annotated_path"] = str(save_path)

    return json_result, result, annotated_img

def run_inference():
    # 1. 加载模型和类别名称
    print("加载模型...")
    model = MODEL
    class_names = CLASS_NAMES
    
    # 2. 从验证集随机选择一张图片
    img_extensions = ['*.jpg', '*.jpeg', '*.png', '*.bmp', '*.tif']
    img_paths = []
    for ext in img_extensions:
        img_paths.extend(Path(VAL_IMG_DIR).glob(ext))
    
    if not img_paths:
        print("❌ 验证集目录未找到图片！")
        return
    
    selected_img = random.choice(img_paths)
    print(f"\n随机选择图片: {selected_img.name}")
    
    # 3. 读取图片
    img = cv2.imread(str(selected_img))
    if img is None:
        print("❌ 图片读取失败！")
        return
    
    img_height, img_width = img.shape[:2]
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    
    # 4. 读取真实标签
    label_path = Path(VAL_LABEL_DIR) / f"{selected_img.stem}.txt"
    gt_boxes = parse_yolo_label(str(label_path), img_width, img_height)
    
    print(f"\n{'='*60}")
    print(f"图片: {selected_img.name}")
    print(f"{'='*60}")
    
    # 5. 运行模型推理
    json_result, result, annotated_img = predict_image(img, image_name=selected_img.name, show=True, save=True)
    pred_info = [
        {
            'class': p['class_id'],
            'name': p['class_name'],
            'bbox': p['bbox'],
            'conf': p['confidence'],
        }
        for p in json_result['predictions']
    ]
    if not pred_info:
        print("  未检测到任何目标")
    
    # 7. 格式化输出
    gt_names = [class_names.get(gt['class'], f"Class {gt['class']}") for gt in gt_boxes]
    pred_names = [f"{p['name']}({p['conf']:.2f})" for p in pred_info]
    
    print(f"\n预测: {', '.join(pred_names) if pred_names else '无'}")
    print(f"实际: {', '.join(gt_names) if gt_names else '无'}")
    print(f"\n详细预测结果:")
    for i, pred in enumerate(pred_info):
        x1, y1, x2, y2 = pred['bbox']
        print(f"  {i+1}. {pred['name']} | 置信度: {pred['conf']:.3f} | 坐标: ({x1}, {y1}, {x2}, {y2})")
    
    print(f"\n详细实际标签:")
    for i, gt in enumerate(gt_boxes):
        cls_name = class_names.get(gt['class'], f"Class {gt['class']}")
        x1, y1, x2, y2 = [int(x) for x in gt['bbox']]
        print(f"  {i+1}. {cls_name} | 坐标: ({x1}, {y1}, {x2}, {y2})")
    
    # 8. 绘制图片（同时显示预测框和真实框）
    fig, ax = plt.subplots(1, 1, figsize=(14, 10))
    ax.imshow(img_rgb)
    
    # 格式化预测和实际标签文本
    gt_names = [class_names.get(gt['class'], f"Class {gt['class']}") for gt in gt_boxes]
    pred_names = [f"{p['name']}({p['conf']:.2f})" for p in pred_info]
    pred_text = ', '.join(pred_names) if pred_names else '无'
    gt_text = ', '.join(gt_names) if gt_names else '无'
    
    # 设置标题
    ax.set_title(f'图片: {selected_img.name}\n预测: {pred_text}\n实际: {gt_text}', 
                fontsize=12, fontweight='bold', pad=15, loc='left')
    ax.axis('off')
    
    # 绘制真实标签框（绿色虚线）
    for gt in gt_boxes:
        x1, y1, x2, y2 = [int(x) for x in gt['bbox']]
        cls_name = class_names.get(gt['class'], f"Class {gt['class']}")
        rect = mpatches.Rectangle((x1, y1), x2-x1, y2-y1, 
                                 fill=False, edgecolor='lime', linewidth=2.5, linestyle='--')
        ax.add_patch(rect)
        ax.text(x1, y1-8, f'实际: {cls_name}', 
               bbox=dict(boxstyle='round,pad=0.3', facecolor='lime', alpha=0.8, edgecolor='lime'),
               fontsize=9, color='black', fontweight='bold', verticalalignment='bottom')
    
    # 绘制预测框（青色实线）
    for pred in pred_info:
        x1, y1, x2, y2 = pred['bbox']
        rect = mpatches.Rectangle((x1, y1), x2-x1, y2-y1, 
                                 fill=False, edgecolor='cyan', linewidth=2.5)
        ax.add_patch(rect)
        ax.text(x1, y1-8, f'预测: {pred["name"]}({pred["conf"]:.2f})', 
               bbox=dict(boxstyle='round,pad=0.3', facecolor='cyan', alpha=0.8, edgecolor='cyan'),
               fontsize=9, color='black', fontweight='bold', verticalalignment='bottom')
    
    # 添加图例
    gt_patch = mpatches.Patch(color='lime', label='实际标签')
    pred_patch = mpatches.Patch(color='cyan', label='模型预测')
    ax.legend(handles=[gt_patch, pred_patch], loc='upper right', fontsize=11, framealpha=0.9)
    
    plt.tight_layout()
    plt.show()
    
    # 8. 保存结果图片
    output_dir = Path("./inference_results")
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 保存带标注的图片（OpenCV格式）
    save_path = output_dir / selected_img.name
    if annotated_img is not None:
        cv2.imwrite(str(save_path), annotated_img)
    print(f"\n✅ 标注图片已保存: {save_path}")

    print("\n完整JSON结果:")
    print(json.dumps(json_result, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    run_inference()