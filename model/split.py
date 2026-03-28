# 划分训练集和测试集

import os
import random
import shutil

IMAGE_FOLDER = r"D:\SHU files\Graduation project\model\Dataset_Raw\images"
LABEL_FOLDER = r"D:\SHU files\Graduation project\model\Dataset_Raw\labels"

# 划分后输出的最终文件夹
OUTPUT_FOLDER = r"D:\SHU files\Graduation project\model\Dataset"
# ==============================================================

VAL_SPLIT = 0.2  # 验证集占 20%

# 自动创建文件夹
os.makedirs(f"{OUTPUT_FOLDER}/images/train", exist_ok=True)
os.makedirs(f"{OUTPUT_FOLDER}/images/val", exist_ok=True)
os.makedirs(f"{OUTPUT_FOLDER}/labels/train", exist_ok=True)
os.makedirs(f"{OUTPUT_FOLDER}/labels/val", exist_ok=True)

# 获取所有图片
all_images = [f for f in os.listdir(IMAGE_FOLDER) if f.endswith(('.jpg', '.png', '.jpeg'))]
random.seed(42)
random.shuffle(all_images)

# 划分
val_count = int(len(all_images) * VAL_SPLIT)
val_images = all_images[:val_count]
train_images = all_images[val_count:]

def move_files(imgs, is_val):
    mode = "val" if is_val else "train"
    for img in imgs:
        # 复制图片
        shutil.copy(os.path.join(IMAGE_FOLDER, img), 
                    os.path.join(OUTPUT_FOLDER, "images", mode, img))
        
        # 复制对应标签
        txt_name = os.path.splitext(img)[0] + ".txt"
        txt_src = os.path.join(LABEL_FOLDER, txt_name)
        if os.path.exists(txt_src):
            shutil.copy(txt_src, os.path.join(OUTPUT_FOLDER, "labels", mode, txt_name))

print("划分训练集...")
move_files(train_images, is_val=False)

print("划分验证集...")
move_files(val_images, is_val=True)

print(f"✅ 划分完成！")
print(f"训练集：{len(train_images)} 张")
print(f"验证集：{len(val_images)} 张")