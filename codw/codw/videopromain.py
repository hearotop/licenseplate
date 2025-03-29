from src.Access.CameraAccess import CameraAccess
from src.utils.VideoProcessor import VideoProcessor

model_path = "/media/hearo/Datas/CarCode/src/yolo11n.pt"  # 模型路径
cache_dir = "/media/hearo/Datas/CarCode/src/image_cache"  # 缓存目录
region_points = [(1080, 230), (350, 260)]  # 区域点
CameraIDlist = {'001101': 0}  # 摄像头ID列表
cameraAccess = CameraAccess('001101')  # 通过数据库获取视频地址
videopath = cameraAccess.get_video_path()
print(videopath)  # 打印视频地址
video_processor = VideoProcessor(videopath, model_path, cache_dir, region_points, '001101', 0)
video_processor.start_processing()