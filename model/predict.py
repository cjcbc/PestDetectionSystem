import cv2
from pathlib import Path
from ultralytics import YOLO

# ==================== 配置参数（直接在此修改） ====================
MODEL_PATH = "best.pt"  # 训练好的模型路径
INPUT_PATH = r"D:\SHU files\Graduation project\model\Dataset\images\train\IP015001307.jpg"  # 输入图片或文件夹路径
OUTPUT_DIR = "./inference_results"              # 输出目录（保存带标注的图片）
CONF_THRESHOLD = 0.25                           # 置信度阈值
DEVICE = 0                                      # 推理设备：0（GPU）或 "cpu"
# ================================================================

def run_inference():
    # 1. 加载模型
    model = YOLO(MODEL_PATH)
    
    # 2. 准备输入
    input_path = Path(INPUT_PATH)
    output_dir = Path(OUTPUT_DIR)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # 确定是单张图片还是文件夹
    if input_path.is_file():
        image_paths = [input_path]
    else:
        # 支持常见图片格式
        image_paths = []
        for ext in ["*.jpg", "*.jpeg", "*.png", "*.bmp", "*.tif"]:
            image_paths.extend(input_path.glob(ext))
        image_paths.sort()
    
    # 3. 批量推理
    results = model(image_paths, conf=CONF_THRESHOLD, device=DEVICE, verbose=False)
    
    # 4. 处理每个结果
    for result in results:
        img_path = result.path
        img_name = Path(img_path).name
        
        # 获取检测信息
        boxes = result.boxes
        if boxes is not None:
            # 提取坐标、置信度、类别
            xyxy = boxes.xyxy.cpu().numpy()
            confs = boxes.conf.cpu().numpy()
            cls_ids = boxes.cls.cpu().numpy().astype(int)
            names = result.names
            
            # 打印检测结果
            print(f"\n图片: {img_name}")
            for i, (box, conf, cls_id) in enumerate(zip(xyxy, confs, cls_ids)):
                x1, y1, x2, y2 = box
                label = names[cls_id]
                print(f"  {i+1}. {label} | 置信度: {conf:.3f} | 坐标: ({x1:.0f}, {y1:.0f}, {x2:.0f}, {y2:.0f})")
        else:
            print(f"\n图片: {img_name} 未检测到任何目标")
        
        # 保存带标注的图片
        annotated_img = result.plot()   # BGR numpy array
        save_path = output_dir / img_name
        cv2.imwrite(str(save_path), annotated_img)
        print(f"  标注图片已保存: {save_path}")

if __name__ == "__main__":
    run_inference()