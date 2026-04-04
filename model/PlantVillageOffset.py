"""
PlantVillage标签偏移脚本
为PlantVillage数据集的标签文件中的类别ID加上102的偏移量。
用于将PlantVillage的38个类别(0-37)与IP102的102个类别(0-101)合并，形成140类统一数据集。
"""

import os


PLANT_LABELS = r"D:\SHU files\Graduation project\model\Dataset\labels"


def add_offset_to_labels(label_dir, offset=102):
    # 遍历当前文件夹
    for f in os.listdir(label_dir):
        if not f.endswith(".txt"):
            continue
        
        txt_path = os.path.join(label_dir, f)
        
        # 读取原来的标签
        with open(txt_path, "r", encoding="utf-8") as f:
            lines = f.readlines()
        
        new_lines = []
        for line in lines:
            line = line.strip()
            if not line:
                continue
            
            parts = line.split()
            # 类别ID +102，其他坐标不动
            cls_id = int(parts[0]) + offset
            new_line = f"{cls_id} {' '.join(parts[1:])}\n"
            new_lines.append(new_line)
        
        # 写回（覆盖原文件）
        with open(txt_path, "w", encoding="utf-8") as f:
            f.writelines(new_lines)

    print(f"✅ 完成！文件夹 {label_dir} 全部索引 +102")

# 运行
add_offset_to_labels(PLANT_LABELS, offset=102)