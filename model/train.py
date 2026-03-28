from ultralytics import YOLO

if __name__ == "__main__":
    # 加载预训练模型
    model = YOLO("yolov8x.pt") 

    model.train(
        # ----- 数据与基础配置 -----
        data="/workspace/Dataset/classes.yaml",   # 数据集配置文件
        epochs=200,                               # 训练轮数（140类需充分训练）
        batch=32,                                 # batch size（显存占用约22-24GB）
        imgsz=640,                                # 输入图像尺寸
        device=0,                                 # 使用第一张GPU（RTX 5090）
        workers=8,                                # 数据加载线程（根据CPU核心调整）
        cache="ram",                              # 缓存图片到内存加速
        
        # ----- 优化器与学习率 -----
        optimizer="auto",                         # 自动选择优化器（默认AdamW）
        lr0=0.01,                                 # 初始学习率
        lrf=0.01,                                 # 最终学习率因子（lr0 * lrf）
        warmup_epochs=3,                          # 预热轮数
        warmup_momentum=0.8,                      # 预热动量
        warmup_bias_lr=0.1,                       # 预热偏置学习率
        weight_decay=0.0005,                      # 权重衰减（正则化）
        
        # ----- 损失权重（针对大分类数微调）-----
        cls=0.5,                                  # 分类损失权重（适当提高）
        box=7.5,                                  # 边界框损失权重
        dfl=1.5,                                  # DFL损失权重
        
        # ----- 数据增强（平衡鲁棒性与过拟合）-----
        hsv_h=0.015,                              # 色调增强范围
        hsv_s=0.7,                                # 饱和度增强范围
        hsv_v=0.4,                                # 明度增强范围
        degrees=0.0,                              # 旋转角度（0表示不旋转）
        translate=0.1,                            # 平移增强（图像比例）
        scale=0.5,                                # 缩放增强
        shear=0.0,                                # 剪切变换
        perspective=0.0,                          # 透视变换
        flipud=0.0,                               # 上下翻转概率
        fliplr=0.5,                               # 左右翻转概率
        mosaic=1.0,                               # mosaic增强概率（1.0表示每个epoch都使用）
        mixup=0.2,                                # mixup增强概率（可提高泛化）
        copy_paste=0.0,                           # copy-paste增强（一般用于分割）
        
        # ----- 训练策略 -----
        cos_lr=True,                              # 使用余弦退火学习率调度
        close_mosaic=15,                          # 最后15个epoch关闭mosaic增强
        patience=50,                              # 早停耐心值（50轮无提升则停止）
        save=True,                                # 保存训练检查点
        save_period=10,                           # 每10个epoch保存一次（便于恢复）
        exist_ok=True,                            # 允许覆盖已有实验目录
        pretrained=True,                          # 使用预训练权重
        resume=False,                             # 是否从断点恢复（设为True可恢复）
        
        # ----- 正则化与辅助技术 -----
        label_smoothing=0.05,                     # 标签平滑（对大分类数非常重要）
        dropout=0.1,                              # Dropout率（仅分类头）
        overlap_mask=True,                        # 训练时使用重叠掩码（仅分割任务）
        
        # ----- 其他 -----
        seed=42,                                  # 固定随机种子
        verbose=True,                             # 打印详细日志
        plots=True,                               # 生成训练曲线图
        project="runs/train",                     # 保存目录
        name="yolov8x_140cls",                    # 实验名称
    )