"""
VOC转YOLO格式转换脚本
将IP102数据集的VOC格式XML标签转换为YOLO格式的TXT标签。
同时复制对应的图片文件到输出目录，支持批量转换和错误处理。
"""

import xml.etree.ElementTree as ET
import os
from pathlib import Path
import shutil


VOC_ANNOTATIONS = r"D:\SHU files\Graduation project\model\OpenDataLab___IP102\raw\IP102_v1.1\Detection\VOC2007\Annotations\Annotations"  # xml文件夹
VOC_IMAGES = r"D:\SHU files\Graduation project\model\OpenDataLab___IP102\raw\IP102_v1.1\Detection\VOC2007\JPEGImages\JPEGImages"        # 图片文件夹
YOLO_ROOT = r"D:\SHU files\Graduation project\model\dataset\OpenDataLab___IP102"               # 输出目录
ERROR_COUNT = 0

YOLO_LABELS = os.path.join(YOLO_ROOT, "labels")
YOLO_IMAGES = os.path.join(YOLO_ROOT, "images")
os.makedirs(YOLO_LABELS, exist_ok=True)
os.makedirs(YOLO_IMAGES, exist_ok=True)

def convert_voc_to_yolo(xml_path):
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()

        img_w = int(root.find('size/width').text)
        img_h = int(root.find('size/height').text)

        labels = []
        for obj in root.findall('object'):
            cls_id = int(obj.find('name').text)

            bndbox = obj.find('bndbox')
            xmin = float(bndbox.find('xmin').text)
            ymin = float(bndbox.find('ymin').text)
            xmax = float(bndbox.find('xmax').text)
            ymax = float(bndbox.find('ymax').text)

            x_center = (xmin + xmax) / 2.0 / img_w
            y_center = (ymin + ymax) / 2.0 / img_h
            w = (xmax - xmin) / img_w
            h = (ymax - ymin) / img_h

            labels.append(f"{cls_id} {x_center:.6f} {y_center:.6f} {w:.6f} {h:.6f}")
        return labels
    except Exception as e:
        # 坏文件直接跳过
        print(f"❌ 解析失败: {xml_path}，错误: {e}")
        global ERROR_COUNT
        ERROR_COUNT += 1
        return None

# 批量转换
for xml_file in os.listdir(VOC_ANNOTATIONS):
    if not xml_file.endswith(".xml"):
        continue

    xml_path = os.path.join(VOC_ANNOTATIONS, xml_file)
    img_stem = Path(xml_file).stem
    img_file = img_stem + ".jpg"
    img_path = os.path.join(VOC_IMAGES, img_file)

    if not os.path.exists(img_path):
        continue

    labels = convert_voc_to_yolo(xml_path)
    if not labels:
        print(f"⚠️  跳过坏文件: {xml_file}")
        continue

    # 保存标签
    txt_path = os.path.join(YOLO_LABELS, img_stem + ".txt")
    with open(txt_path, "w", encoding="utf-8") as f:
        f.write("\n".join(labels))

    # 复制图片
    shutil.copy(img_path, os.path.join(YOLO_IMAGES, img_file))

print(f"✅ 转换完成！有问题的XML已自动跳过,计数：{ERROR_COUNT}")