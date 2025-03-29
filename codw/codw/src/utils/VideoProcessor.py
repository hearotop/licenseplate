import multiprocessing
import threading
import time
import cv2
from queue import Queue
from ultralytics import YOLO

from ss import ObjectCounter

class VideoProcessor:
    def __init__(self, video_path, model_path, cache_dir, region_points, CameraID, flag):
        """
        初始化视频处理器。
        Parameters:
        - video_path (str): 视频文件路径
        - model_path (str): YOLO模型文件路径
        - cache_dir (str): 缓存目录路径
        - region_points (list): 区域定义点列表
        - CameraID (str): 摄像头ID
        - flag (int): 标志位，用于控制处理逻辑
        """
        self.processing_thread = None
        self.frame_count = 0  # 初始化帧计数器
        self.video_path = video_path
        self.model =YOLO(model_path)# Load the YOLO11 model
        self.cache_dir = cache_dir
        self.CameraId = CameraID
        self.flag = flag
        self.im0 = None
        #视频捕获初始化
        self.cap = cv2.VideoCapture(self.video_path,)
        assert self.cap.isOpened(), "读取视频文件出错"
        # 定义ObjectCounter的区域点（根据需要调整）
        self.region_points = region_points
        # 初始化ObjectCounter
        self.counter = ObjectCounter(
            model=model_path,
            show=True,
            region=self.region_points,
      #      region=region_points
        )
    def process_frames(self):
        """ 处理队列中的帧 """
        while True:
            while self.cap.isOpened():
                success, self.im0 = self.cap.read()
                if not success:
                    print("Video frame is empty or video processing has been successfully completed.")
                    break
                self.counter.count(self.im0, self.cache_dir)
                # 等待指定的时间后播放下一帧
                if cv2.waitKey(25) & 0xFF == ord('q'):
                    break
    def start_processing(self):
        self.process_frames()

    def get_catch_flag(self):
        """ 获取标志位信息以确保前端是否发起请求 """
        return self.counter.get_flag()