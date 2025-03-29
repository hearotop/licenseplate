# Ultralytics YOLO 🚀, AGPL-3.0 license
import base64
import os
from collections import defaultdict
import cv2
from PIL import Image
from ultralytics.utils.checks import check_imshow, check_requirements
from ultralytics.utils.plotting import Annotator, colors
from src.utils.CatcodeOCR import CatcodeOCR
check_requirements("shapely>=2.0.0")
from shapely.geometry import LineString, Point, Polygon
codeOCR = CatcodeOCR()
font = cv2.FONT_HERSHEY_SIMPLEX
class ObjectCounter:
    """A class to manage the counting of objects in a real-time video stream based on their tracks."""
    def __init__(
            self,
            classes_names,
            reg_pts=None,
            count_reg_color=(255, 0, 255),
            count_txt_color=(0, 0, 0),
            count_bg_color=(255, 255, 255),
            line_thickness=2,
            track_thickness=2,
            view_img=True,
            view_in_counts=True,
            draw_tracks=False,
            track_color=None,
            region_thickness=5,
            line_dist_thresh=15,
            cls_txtdisplay_gap=50,
    ):

        # Mouse events
        self.is_drawing = False
        self.selected_point = None
        self.cache_dir= None
        # Region & Line Information
        self.reg_pts = [(20, 400), (1260, 400)] if reg_pts is None else reg_pts
        self.line_dist_thresh = line_dist_thresh
        self.counting_region = None
        self.region_color = count_reg_color
        self.region_thickness = region_thickness
        self.incount = 0
        self.outcount = 0
        self.flag=0
        self.Cameraid=None
        self.parknumber = [150,200]
        self.accessnumber=[0,0]

        # Image and annotation Information
        self.im0 = None
        self.tf = line_thickness
        self.view_img = view_img
        self.view_in_counts = view_in_counts
        self.view_out_counts = False#view_out_counts
        self.names = None  # Classes names
        self.annotator = None  # Annotator
        self.window_name = "license_recongnition"
        self.inflag=None

        # Object counting Information
        self.in_counts = 0
        self.out_counts = 0
        self.count_ids = []
        self.class_wise_count = {}
        self.count_txt_thickness = 0
        self.count_txt_color = count_txt_color
        self.count_bg_color = count_bg_color
        self.cls_txtdisplay_gap = cls_txtdisplay_gap
        self.fontsize = 0.6

        # Tracks info
        self.track_history = defaultdict(list)
        self.track_thickness = track_thickness
        self.draw_tracks = draw_tracks
        self.track_color = track_color

        # Check if environment supports imshow
        self.env_check = check_imshow(warn=True)

        # Initialize counting region
        if len(self.reg_pts) == 2:
            print("Line Counter Initiated.")
            self.counting_region = LineString(self.reg_pts)
        elif len(self.reg_pts) >= 3:
            print("Polygon Counter Initiated.")
            self.counting_region = Polygon(self.reg_pts)
        else:
            print("Invalid Region points provided, region_points must be 2 for lines or >= 3 for polygons.")
            print("Using Line Counter Now")
            self.counting_region = LineString(self.reg_pts)



    def extract_and_process_tracks(self, tracks):
        """Extracts and processes tracks for object counting in a video stream."""

        # Annotator Init and region drawing
        self.annotator = Annotator(self.im0, self.tf, self.names)

        # Draw region or line
        self.annotator.draw_region(reg_pts=self.reg_pts, color=self.region_color, thickness=self.region_thickness)

        if tracks[0].boxes.id is not None:
            boxes = tracks[0].boxes.xyxy.cpu()
            clss = tracks[0].boxes.cls.cpu().tolist()
            track_ids = tracks[0].boxes.id.int().cpu().tolist()

            # Extract tracks
            for box, track_id, cls in zip(boxes, track_ids, clss):
                # Draw bounding box
                # Ensure cls is an integer
                cls = int(cls) if isinstance(cls, (int, float)) and cls.is_integer() else None
                if cls is not None and 0 <= cls < len(
                        self.names):  # Assuming valid indices are from 0 to len(self.names) - 1
                    # Now it should be safe to use cls for indexing
                    self.annotator.box_label(box, label=f"{self.names[cls]}#{track_id}",
                                             color=colors(int(track_id), True))
                else:
                    print("Invalid class identifier, skipping annotation.")

                # Store class info
                if self.names[cls] not in self.class_wise_count:
                    self.class_wise_count[self.names[cls]] = {"IN": 0, "OUT": 0}

                # Draw Tracks
                track_line = self.track_history[track_id]
                track_line.append((float((box[0] + box[2]) / 2), float((box[1] + box[3]) / 2)))
                if len(track_line) > 30:
                    track_line.pop(0)

                # Draw track trails
                if self.draw_tracks:
                    self.annotator.draw_centroid_and_tracks(
                        track_line,
                        color=self.track_color or colors(int(track_id), True),
                        track_thickness=self.track_thickness,
                    )

                prev_position = self.track_history[track_id][-2] if len(self.track_history[track_id]) > 1 else None

                # Count objects in any polygon
                if len(self.reg_pts) >= 3:
                    is_inside = self.counting_region.contains(Point(track_line[-1]))

                    if prev_position is not None and is_inside and track_id not in self.count_ids:
                        self.count_ids.append(track_id)
                        if (box[0] - prev_position[0]) * (self.counting_region.centroid.x - prev_position[0]) > 0:
                            self.flag = 1
                            self.in_counts += 1
                            self.class_wise_count[self.names[cls]]["IN"] += 1
                            self.send_codeinfo()
                        else:
                            self.flag = 1
                            self.out_counts += 1
                            self.class_wise_count[self.names[cls]]["OUT"] += 1
                            self.send_codeinfo()
                # Count objects using line
                elif len(self.reg_pts) == 2:
                    if prev_position is not None and track_id not in self.count_ids:
                        distance = Point(track_line[-1]).distance(self.counting_region)
                        if distance < self.line_dist_thresh and track_id not in self.count_ids:
                            self.count_ids.append(track_id)
                            if (box[0] - prev_position[0]) * (self.counting_region.centroid.x - prev_position[0]) > 0:
                                self.flag = 1
                                self.inflag=1
                                self.in_counts += 1
                                self.class_wise_count[self.names[cls]]["IN"] += 1
                                self.accessnumber[0] = self.parknumber[0] - self.in_counts + self.out_counts
                                self.accessnumber[1] = self.parknumber[1] - self.in_counts + self.out_counts
                                self.send_codeinfo()
                            else:
                                self.flag = 1
                                self.inflag=0
                                self.out_counts += 1
                                self.class_wise_count[self.names[cls]]["OUT"] += 1
                                self.accessnumber[0] = self.parknumber[0] - self.in_counts + self.out_counts
                                self.accessnumber[1] = self.parknumber[1] - self.in_counts + self.out_counts
                                self.send_codeinfo()

        labels_dict = {}

        for key, value in self.class_wise_count.items():
            if value["IN"] != 0 or value["OUT"] != 0:
                if not self.view_in_counts and not self.view_out_counts:
                    continue
                elif not self.view_in_counts:
                    labels_dict[str.capitalize(key)] = f"OUT {value['OUT']}"
                elif not self.view_out_counts:
                    labels_dict[str.capitalize(key)] = f"IN {value['IN']}"
                else:
                    labels_dict[str(key).capitalize()] = f"IN {value['IN']} OUT {value['OUT']}"

        if labels_dict:
            self.annotator.display_analytics(self.im0, labels_dict, self.count_txt_color, self.count_bg_color, 10)
        self.flag = 0


    def display_frames(self,im0):
        """Displays the current frame with annotations and regions in a window."""
        if self.env_check:
            cv2.namedWindow(self.window_name)
            cv2.imshow(self.window_name, im0)
            # Convert image to JPEG format
            ret, jpeg = cv2.imencode('.jpg',im0)
            if ret:
                jpeg_bytes = jpeg.tobytes()
                base64_encoded_jpeg = base64.b64encode(jpeg_bytes).decode('utf-8')
                self.send_frame_to_frontend(base64_encoded_jpeg)
            # Break Window
            if cv2.waitKey(1) & 0xFF == ord("q"):
                return

    def start_counting(self, im0, tracks,cache_dir,CameraID,flag):
        """
        Main function to start the object counting process.

        Args:
            im0 (ndarray): Current frame from the video stream.
            tracks (list): List of tracks obtained from the object tracking process.
        """
        self.im0 = im0  # store image
        self.extract_and_process_tracks(tracks)  # draw region even if no objects
        self.cache_dir=cache_dir


        if self.view_img:
            self.display_frames(im0)
        return self.im0
    def get_flag(self):#让前端是否现在发出申请进行信息获取
        return self.flag

    def send_frame_to_frontend(self, frame):
        # Placeholder function to simulate sending frame to frontend
        # In practice, this could use a web framework like Flask to send the frame
        pass

    def save_image_to_cache(self):#保存图片到本地缓存
        if not os.path.exists(self.cache_dir):
            os.makedirs(self.cache_dir)
        # 将numpy.ndarray对象转换为PIL.Image对象
        image = Image.fromarray(self.im0)
        image_path = os.path.join(self.cache_dir, f"image_{len(os.listdir(self.cache_dir)) + 1}.jpg")
        image_name=f"image_{len(os.listdir(self.cache_dir)) + 1}.jpg"
        image.save(image_path)
        return image_path,image_name

    # 删除一个图片
    def delete_one_image(self):
        image_files = [f for f in os.listdir(self.cache_dir) if f.endswith(".jpg")]
        if image_files:
            image_to_delete = os.path.join(self.cache_dir, image_files[0])
            os.remove(image_to_delete)
            return True
        return False
    def send_codeinfo(self):#用来判断进出
        # 创建一个窗口并显示图片
        self.incount=self.in_counts
        self.outcount=self.out_counts
        codeOCR.img_path,codeOCR.img_name = self.save_image_to_cache()
        #添加识别图传送给前端
        cv2.imread(codeOCR.img_path)
        # 创建一个窗口并显示图片
        #cv2.imshow('Image', image)
        codeOCR.CarcodeInfo(self.incount, self.outcount, self.inflag, codeOCR.img_name, "001101",200)















