"""
数据集可视化脚本
随机抽取训练集中的图片，绘制YOLO格式的标注框和类别名称，用于检查数据集质量。
支持显示中文类别名称，绿色框表示真实标注。
"""

import os
import random
import cv2
import matplotlib.pyplot as plt
from ultralytics import YOLO

# ===================== 改成你自己的路径 =====================
IMG_DIR    = r"D:\SHU files\Graduation project\model\Dataset\images\train"
LABEL_DIR  = r"D:\SHU files\Graduation project\model\Dataset\labels\train"

# 把上面的 names 直接复制在这里（只复制 names 那一大段）
class_names = {
    0: "rice leaf roller 稻纵卷叶螟",
    1: "rice leaf caterpillar 稻叶夜蛾",
    2: "paddy stem maggot 稻秆蝇",
    3: "asiatic rice borer 二化螟",
    4: "yellow rice borer 三化螟",
    5: "rice gall midge 稻瘿蚊",
    6: "Rice Stemfly 稻秆蝇",
    7: "brown plant hopper 褐飞虱",
    8: "white backed plant hopper 白背飞虱",
    9: "small brown plant hopper 灰飞虱",
    10: "rice water weevil 稻水象甲",
    11: "rice leafhopper 稻叶蝉",
    12: "grain spread thrips 稻蓟马",
    13: "rice shell pest 稻壳虫",
    14: "grub 蛴螬",
    15: "mole cricket 蝼蛄",
    16: "wireworm 金针虫",
    17: "white margined moth 白边蛾",
    18: "black cutworm 小地老虎",
    19: "large cutworm 大地老虎",
    20: "yellow cutworm 黄地老虎",
    21: "red spider 红蜘蛛",
    22: "corn borer 玉米螟",
    23: "army worm 粘虫",
    24: "aphids 蚜虫",
    25: "Potosiabre vitarsis 玉米跳甲",
    26: "peach borer 桃小食心虫",
    27: "English grain aphid 麦长管蚜",
    28: "green bug 麦二叉蚜",
    29: "bird cherry-oat aphid 禾谷缢管蚜",
    30: "wheat blossom midge 麦红吸浆虫",
    31: "penthaleus major 麦圆蜘蛛",
    32: "longlegged spider mite 山楂叶螨",
    33: "wheat phloeothrips 麦蓟马",
    34: "wheat sawfly 麦叶蜂",
    35: "cerodonta denticornis 黑角金龟",
    36: "beet fly 甜菜潜叶蝇",
    37: "flea beetle 跳甲",
    38: "cabbage army worm 菜青虫",
    39: "beet army worm 甜菜夜蛾",
    40: "Beet spot flies 甜菜斑蝇",
    41: "meadow moth 草地螟",
    42: "beet weevil 甜菜象甲",
    43: "sericaorientalis moth 东方金龟",
    44: "alfalfa weevil 苜蓿象甲",
    45: "flax budworm 亚麻夜蛾",
    46: "alfalfa plant bug 苜蓿盲蝽",
    47: "tarnished plant bug 牧草盲蝽",
    48: "Locustoidea 蝗虫",
    49: "lytta polita 绿芫菁",
    50: "legume blister beetle 豆芫菁",
    51: "blister beetle 芫菁",
    52: "therioaphis maculata 苜蓿斑蚜",
    53: "odontothrips loti 牛角花齿蓟马",
    54: "Thrips 蓟马",
    55: "alfalfa seed chalcid 苜蓿种子小蜂",
    56: "Pieris canidia 菜粉蝶",
    57: "Apolygus lucorum 绿盲蝽",
    58: "Limacodidae 刺蛾",
    59: "Viteus vitifoliae 葡萄根瘤蚜",
    60: "Colomerus vitis 葡萄瘿螨",
    61: "Brevipalpus lewisi 短须螨",
    62: "oides decempunctata 十星瓢萤叶甲",
    63: "Polyphagotarsonemus latus 茶黄螨",
    64: "Pseudococcus comstocki 康氏粉蚧",
    65: "parathrene regalis 葡萄透翅蛾",
    66: "Ampelophaga 葡萄天蛾",
    67: "Lycorma delicatula 斑衣蜡蝉",
    68: "Xylotrechus 虎天牛",
    69: "Cicadella viridis 大青叶蝉",
    70: "Miridae 盲蝽",
    71: "Trialeurodes vaporariorum 白粉虱",
    72: "Erythroneura apicalis 葡萄斑叶蝉",
    73: "Papilio xuthus 柑橘凤蝶",
    74: "Panonychus citri 柑橘红蜘蛛",
    75: "Phyllocoptes oleivorus 柑橘锈螨",
    76: "Icerya purchasi 吹绵蚧",
    77: "Unaspis yanonensis 矢尖蚧",
    78: "Ceroplastes rubens 红蜡蚧",
    79: "Chrysomphalus aonidum 褐圆蚧",
    80: "Parlatoria zizyphus 黑点蚧",
    81: "Nipaecoccus vastator 堆蜡粉蚧",
    82: "Aleurocanthus spiniferus 黑刺粉虱",
    83: "Bactrocera minax 柑橘大实蝇",
    84: "Dacus dorsalis 柑橘小实蝇",
    85: "Bactrocera tsuneonis 蜜柑大实蝇",
    86: "Prodenia litura 斜纹夜蛾",
    87: "Adristyrannus 柑橘蚜蝇",
    88: "Phyllocnistis citrella 柑橘潜叶蛾",
    89: "Toxoptera citricidus 橘蚜",
    90: "Toxoptera aurantii 茶二叉蚜",
    91: "Aphis citricola 绣线菊蚜",
    92: "Scirtothrips dorsalis 茶黄蓟马",
    93: "Dasineura sp 柑橘瘿蚊",
    94: "Lawana imitata 白蛾蜡蝉",
    95: "Salurnis marginella 缘纹广翅蜡蝉",
    96: "Deporaus marginatus 芒果切叶象甲",
    97: "Chlumetia transversa 芒果横线尾夜蛾",
    98: "Mango flat beak leafhopper 芒果扁喙叶蝉",
    99: "Rhytidodera bowrinii 芒果天牛",
    100: "Sternochetus frigidus 芒果象甲",
    101: "Cicadellidae 叶蝉",

    102: "Apple___Apple_scab 苹果黑星病",
    103: "Apple___Black_rot 苹果黑腐病",
    104: "Apple___Cedar_apple_rust 苹果锈病",
    105: "Apple___healthy 苹果健康",
    106: "Blueberry___healthy 蓝莓健康",
    107: "Cherry___Powdery_mildew 樱桃白粉病",
    108: "Cherry___healthy 樱桃健康",
    109: "Corn___Cercospora_leaf_spot 玉米灰斑病",
    110: "Corn___Common_rust 玉米锈病",
    111: "Corn___Northern_Leaf_Blight 玉米大斑病",
    112: "Corn___healthy 玉米健康",
    113: "Grape___Black_rot 葡萄黑腐病",
    114: "Grape___Esca 葡萄藤斑病",
    115: "Grape___Leaf_blight 葡萄叶斑病",
    116: "Grape___healthy 葡萄健康",
    117: "Orange___Haunglongbing 柑橘黄龙病",
    118: "Peach___Bacterial_spot 桃细菌性穿孔病",
    119: "Peach___healthy 桃健康",
    120: "Pepper_bell___Bacterial_spot 甜椒斑点病",
    121: "Pepper_bell___healthy 甜椒健康",
    122: "Potato___Early_blight 马铃薯早疫病",
    123: "Potato___Late_blight 马铃薯晚疫病",
    124: "Potato___healthy 马铃薯健康",
    125: "Raspberry___healthy 树莓健康",
    126: "Soybean___healthy 大豆健康",
    127: "Squash___Powdery_mildew 南瓜白粉病",
    128: "Strawberry___Leaf_scorch 草莓叶枯病",
    129: "Strawberry___healthy 草莓健康",
    130: "Tomato___Bacterial_spot 番茄斑点病",
    131: "Tomato___Early_blight 番茄早疫病",
    132: "Tomato___Late_blight 番茄晚疫病",
    133: "Tomato___Leaf_Mold 番茄叶霉病",
    134: "Tomato___Septoria_leaf_spot 番茄斑枯病",
    135: "Tomato___Spider_mites 番茄红蜘蛛",
    136: "Tomato___Target_Spot 番茄靶斑病",
    137: "Tomato___Yellow_Leaf_Curl_Virus 番茄黄化曲叶病毒",
    138: "Tomato___Tomato_mosaic_virus 番茄花叶病毒",
    139: "Tomato___healthy 番茄健康"
}

# 随机选一张图
img_files = [f for f in os.listdir(IMG_DIR) if f.lower().endswith(('jpg', 'png', 'jpeg'))]
selected  = random.choice(img_files)
selected = "IP015001307.jpg"
name      = os.path.splitext(selected)[0]
img_path  = os.path.join(IMG_DIR, selected)
lab_path  = os.path.join(LABEL_DIR, name + ".txt")

print(f"随机选择的图片: {img_path}")
# 读图
img = cv2.imread(img_path)
img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
h_img, w_img = img.shape[:2]

# 读标签
with open(lab_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# model = YOLO("yolov8s.pt")  # 加载模型
# results = model(img_path)  # 进行推理
# print(results)  # 输出结果（包含检测框、类别等信息）


# 画图
plt.rcParams['font.sans-serif'] = ['SimHei']  # 显示中文
plt.figure(figsize=(8, 6))
plt.imshow(img)
plt.axis('off')
plt.title(f"文件：{selected}\n标签文件：{name}.txt", fontsize=10)

for line in lines:
    line = line.strip()
    if not line:
        continue
    cls_id, x, y, w, h = map(float, line.split())
    cls_id = int(cls_id)

    # YOLO转像素坐标
    x1 = int((x - w/2) * w_img)
    y1 = int((y - h/2) * h_img)
    x2 = int((x + w/2) * w_img)
    y2 = int((y + h/2) * h_img)

    # 画框
    plt.gca().add_patch(
        plt.Rectangle((x1, y1), x2-x1, y2-y1, 
                      edgecolor='red', linewidth=2, fill=False)
    )
    # 写类别
    plt.text(x1, y1-5, class_names[cls_id], 
             color='white', fontsize=10, 
             bbox=dict(boxstyle="round", facecolor='red', alpha=0.7))

plt.tight_layout()
plt.show()

# 控制台也输出一遍
print("✅ 随机测试结果")
print(f"图片：{selected}")
print(f"标签：{name}.txt")
print(f"类别号：{cls_id}")
print(f"类别名：{class_names[cls_id]}")