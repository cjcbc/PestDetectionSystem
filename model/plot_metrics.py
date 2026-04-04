"""
训练指标可视化脚本
读取训练过程中生成的 results.csv 文件，绘制各类训练指标曲线图。
包括：损失曲线、Precision/Recall、mAP、学习率变化等。
"""

import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei']  # 用于显示中文
plt.rcParams['axes.unicode_minus'] = False  # 用于正常显示负号

# ==================== 配置参数 ====================
RESULTS_CSV = r"D:\SHU files\Graduation project\model\results\results.csv"
OUTPUT_DIR = r"D:\SHU files\Graduation project\model\results"
# ==================================================

def plot_training_metrics():
    """绘制训练指标曲线"""
    # 读取数据
    df = pd.read_csv(RESULTS_CSV)
    epochs = df['epoch']
    
    # 创建大图
    fig = plt.figure(figsize=(20, 16))
    
    # ========== 1. 损失曲线 ==========
    ax1 = fig.add_subplot(3, 3, 1)
    ax1.plot(epochs, df['train/box_loss'], label='Train Box Loss', linewidth=2)
    ax1.plot(epochs, df['train/cls_loss'], label='Train Cls Loss', linewidth=2)
    ax1.plot(epochs, df['train/dfl_loss'], label='Train DFL Loss', linewidth=2)
    ax1.set_xlabel('Epoch')
    ax1.set_ylabel('Loss')
    ax1.set_title('训练损失曲线')
    ax1.legend()
    ax1.grid(True, alpha=0.3)
    
    # ========== 2. 验证损失曲线 ==========
    ax2 = fig.add_subplot(3, 3, 2)
    ax2.plot(epochs, df['val/box_loss'], label='Val Box Loss', linewidth=2, color='orange')
    ax2.plot(epochs, df['val/cls_loss'], label='Val Cls Loss', linewidth=2, color='red')
    ax2.plot(epochs, df['val/dfl_loss'], label='Val DFL Loss', linewidth=2, color='green')
    ax2.set_xlabel('Epoch')
    ax2.set_ylabel('Loss')
    ax2.set_title('验证损失曲线')
    ax2.legend()
    ax2.grid(True, alpha=0.3)
    
    # ========== 3. Precision & Recall ==========
    ax3 = fig.add_subplot(3, 3, 3)
    ax3.plot(epochs, df['metrics/precision(B)'], label='Precision', linewidth=2, color='blue')
    ax3.plot(epochs, df['metrics/recall(B)'], label='Recall', linewidth=2, color='orange')
    ax3.set_xlabel('Epoch')
    ax3.set_ylabel('Score')
    ax3.set_title('Precision & Recall')
    ax3.legend()
    ax3.grid(True, alpha=0.3)
    ax3.set_ylim(0, 1.05)
    
    # ========== 4. mAP曲线 ==========
    ax4 = fig.add_subplot(3, 3, 4)
    ax4.plot(epochs, df['metrics/mAP50(B)'], label='mAP@50', linewidth=2, color='blue')
    ax4.plot(epochs, df['metrics/mAP50-95(B)'], label='mAP@50-95', linewidth=2, color='red')
    ax4.set_xlabel('Epoch')
    ax4.set_ylabel('mAP')
    ax4.set_title('mAP 曲线')
    ax4.legend()
    ax4.grid(True, alpha=0.3)
    ax4.set_ylim(0, 1.05)
    
    # ========== 5. 学习率变化 ==========
    ax5 = fig.add_subplot(3, 3, 5)
    lr_cols = [col for col in df.columns if col.startswith('lr/')]
    for col in lr_cols[:4]:  # 只画前4个学习率
        ax5.plot(epochs, df[col], label=col, linewidth=1.5)
    ax5.set_xlabel('Epoch')
    ax5.set_ylabel('Learning Rate')
    ax5.set_title('学习率变化曲线')
    ax5.legend()
    ax5.grid(True, alpha=0.3)
    
    # ========== 6. 训练 vs 验证 Box Loss ==========
    ax6 = fig.add_subplot(3, 3, 6)
    ax6.plot(epochs, df['train/box_loss'], label='Train Box Loss', linewidth=2)
    ax6.plot(epochs, df['val/box_loss'], label='Val Box Loss', linewidth=2, color='orange')
    ax6.set_xlabel('Epoch')
    ax6.set_ylabel('Box Loss')
    ax6.set_title('Box Loss: Train vs Val')
    ax6.legend()
    ax6.grid(True, alpha=0.3)
    
    # ========== 7. 训练 vs 验证 Cls Loss ==========
    ax7 = fig.add_subplot(3, 3, 7)
    ax7.plot(epochs, df['train/cls_loss'], label='Train Cls Loss', linewidth=2)
    ax7.plot(epochs, df['val/cls_loss'], label='Val Cls Loss', linewidth=2, color='orange')
    ax7.set_xlabel('Epoch')
    ax7.set_ylabel('Cls Loss')
    ax7.set_title('Cls Loss: Train vs Val')
    ax7.legend()
    ax7.grid(True, alpha=0.3)
    
    # ========== 8. 训练 vs 验证 DFL Loss ==========
    ax8 = fig.add_subplot(3, 3, 8)
    ax8.plot(epochs, df['train/dfl_loss'], label='Train DFL Loss', linewidth=2)
    ax8.plot(epochs, df['val/dfl_loss'], label='Val DFL Loss', linewidth=2, color='orange')
    ax8.set_xlabel('Epoch')
    ax8.set_ylabel('DFL Loss')
    ax8.set_title('DFL Loss: Train vs Val')
    ax8.legend()
    ax8.grid(True, alpha=0.3)
    
    # ========== 9. 综合指标 ==========
    ax9 = fig.add_subplot(3, 3, 9)
    ax9.plot(epochs, df['metrics/precision(B)'], label='Precision', linewidth=2)
    ax9.plot(epochs, df['metrics/recall(B)'], label='Recall', linewidth=2)
    ax9.plot(epochs, df['metrics/mAP50(B)'], label='mAP@50', linewidth=2, linestyle='--')
    ax9.plot(epochs, df['metrics/mAP50-95(B)'], label='mAP@50-95', linewidth=2, linestyle='--')
    ax9.set_xlabel('Epoch')
    ax9.set_ylabel('Score')
    ax9.set_title('综合指标汇总')
    ax9.legend()
    ax9.grid(True, alpha=0.3)
    ax9.set_ylim(0, 1.05)
    
    plt.tight_layout()
    save_path = f"{OUTPUT_DIR}/training_metrics.png"
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"✅ 训练指标图已保存: {save_path}")

def plot_individual_metrics():
    """单独绘制各类指标图（更清晰）"""
    df = pd.read_csv(RESULTS_CSV)
    epochs = df['epoch']
    
    # 1. 损失曲线（训练+验证）
    fig, axes = plt.subplots(1, 3, figsize=(18, 5))
    
    losses = ['box_loss', 'cls_loss', 'dfl_loss']
    titles = ['Box Loss', 'Cls Loss', 'DFL Loss']
    
    for i, (loss, title) in enumerate(zip(losses, titles)):
        axes[i].plot(epochs, df[f'train/{loss}'], label=f'Train {title}', linewidth=2)
        axes[i].plot(epochs, df[f'val/{loss}'], label=f'Val {title}', linewidth=2, color='orange')
        axes[i].set_xlabel('Epoch')
        axes[i].set_ylabel('Loss')
        axes[i].set_title(f'{title}: Train vs Val')
        axes[i].legend()
        axes[i].grid(True, alpha=0.3)
    
    plt.tight_layout()
    save_path = f"{OUTPUT_DIR}/loss_curves.png"
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"✅ 损失曲线图已保存: {save_path}")
    
    # 2. 指标曲线
    fig, axes = plt.subplots(1, 3, figsize=(18, 5))
    
    axes[0].plot(epochs, df['metrics/precision(B)'], linewidth=2, color='blue')
    axes[0].set_xlabel('Epoch')
    axes[0].set_ylabel('Precision')
    axes[0].set_title('Precision 曲线')
    axes[0].grid(True, alpha=0.3)
    axes[0].set_ylim(0, 1.05)
    
    axes[1].plot(epochs, df['metrics/recall(B)'], linewidth=2, color='orange')
    axes[1].set_xlabel('Epoch')
    axes[1].set_ylabel('Recall')
    axes[1].set_title('Recall 曲线')
    axes[1].grid(True, alpha=0.3)
    axes[1].set_ylim(0, 1.05)
    
    axes[2].plot(epochs, df['metrics/mAP50(B)'], label='mAP@50', linewidth=2, color='blue')
    axes[2].plot(epochs, df['metrics/mAP50-95(B)'], label='mAP@50-95', linewidth=2, color='red')
    axes[2].set_xlabel('Epoch')
    axes[2].set_ylabel('mAP')
    axes[2].set_title('mAP 曲线')
    axes[2].legend()
    axes[2].grid(True, alpha=0.3)
    axes[2].set_ylim(0, 1.05)
    
    plt.tight_layout()
    save_path = f"{OUTPUT_DIR}/metrics_curves.png"
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"✅ 指标曲线图已保存: {save_path}")
    
    # 3. 学习率曲线
    fig, ax = plt.subplots(figsize=(10, 5))
    lr_cols = [col for col in df.columns if col.startswith('lr/')]
    for col in lr_cols:
        ax.plot(epochs, df[col], label=col, linewidth=1.5)
    ax.set_xlabel('Epoch')
    ax.set_ylabel('Learning Rate')
    ax.set_title('学习率变化曲线 (Cosine Annealing)')
    ax.legend()
    ax.grid(True, alpha=0.3)
    
    plt.tight_layout()
    save_path = f"{OUTPUT_DIR}/learning_rate.png"
    plt.savefig(save_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f"✅ 学习率曲线图已保存: {save_path}")

def print_summary():
    """打印训练摘要"""
    df = pd.read_csv(RESULTS_CSV)
    
    print("=" * 60)
    print("训练摘要")
    print("=" * 60)
    print(f"总训练轮数: {df['epoch'].max()}")
    print(f"\n最终指标 (Epoch {df['epoch'].max()}):")
    print(f"  Precision:    {df['metrics/precision(B)'].iloc[-1]:.4f}")
    print(f"  Recall:       {df['metrics/recall(B)'].iloc[-1]:.4f}")
    print(f"  mAP@50:       {df['metrics/mAP50(B)'].iloc[-1]:.4f}")
    print(f"  mAP@50-95:    {df['metrics/mAP50-95(B)'].iloc[-1]:.4f}")
    
    print(f"\n最佳指标:")
    print(f"  最佳 Precision: {df['metrics/precision(B)'].max():.4f} (Epoch {df['metrics/precision(B)'].idxmax() + 1})")
    print(f"  最佳 Recall:    {df['metrics/recall(B)'].max():.4f} (Epoch {df['metrics/recall(B)'].idxmax() + 1})")
    print(f"  最佳 mAP@50:    {df['metrics/mAP50(B)'].max():.4f} (Epoch {df['metrics/mAP50(B)'].idxmax() + 1})")
    print(f"  最佳 mAP@50-95: {df['metrics/mAP50-95(B)'].max():.4f} (Epoch {df['metrics/mAP50-95(B)'].idxmax() + 1})")
    
    print(f"\n最终损失:")
    print(f"  Train Box Loss:  {df['train/box_loss'].iloc[-1]:.4f}")
    print(f"  Train Cls Loss:  {df['train/cls_loss'].iloc[-1]:.4f}")
    print(f"  Val Box Loss:    {df['val/box_loss'].iloc[-1]:.4f}")
    print(f"  Val Cls Loss:    {df['val/cls_loss'].iloc[-1]:.4f}")

if __name__ == "__main__":
    print_summary()
    plot_training_metrics()
    plot_individual_metrics()
    print("\n✅ 所有图表生成完成！")
