
import cv2

import torch

def check_gpu():
    """检查 GPU 和 CUDA 是否可用"""
    if torch.cuda.is_available():
        gpu_name = torch.cuda.get_device_name(0)
        print(f"GPU 可用: {gpu_name}")
    else:
        print("没有检测到可用的 GPU，程序将在 CPU 上运行。")
    print(f"OpenCV CUDA 支持: {cv2.cuda.getCudaEnabledDeviceCount() > 0}")

