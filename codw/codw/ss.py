import base64
import os
import threading
from PIL import Image
from shapely.geometry import LineString, Point, Polygon
import cv2
from ultralytics.solutions.solutions import BaseSolution
from ultralytics.utils.plotting import Annotator, colors
from src.utils.CatcodeOCR import CatcodeOCR
import asyncio

codeOCR = CatcodeOCR()

class ObjectCounter(BaseSolution):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.in_count = 0
        self.out_count = 0
        self.counted_ids = []
        self.classwise_counts = {}
        self.region_initialized = False
        self.show_in = self.CFG["show_in"]
        self.show_out = self.CFG["show_out"]
        self.cache_dir = None
        self.im0 = None
        self.view_in_counts = True
        self.view_out_counts = False
        self.annotator = None
        self.inflag = None
        self.in_counts = 0
        self.out_counts = 0

    def count_objects(self, track_line, box, track_id, prev_position, cls):
        if prev_position is None or track_id in self.counted_ids:
            return
        centroid = self.r_s.centroid
        dx = (box[0] - prev_position[0]) * (centroid.x - prev_position[0])
        dy = (box[1] - prev_position[1]) * (centroid.y - prev_position[1])
        if len(self.region) >= 3 and self.r_s.contains(Point(track_line[-1])):
            self.counted_ids.append(track_id)
            if dx > 0:
                self.flag = 1
                self.in_counts += 1
                self.classwise_counts[self.names[cls]]["IN"] += 1
                self.send_codeinfo()
            else:
                self.flag = 1
                self.out_counts += 1
                self.classwise_counts[self.names[cls]]["OUT"] += 1
                self.send_codeinfo()
                self.out_count += 1
        elif len(self.region) < 3 and LineString([prev_position, box[:2]]).intersects(self.r_s):
            self.counted_ids.append(track_id)
            if dx > 0 and dy > 0:
                self.flag = 1
                self.inflag = 1
                self.in_counts += 1
                self.classwise_counts[self.names[cls]]["IN"] += 1
                self.send_codeinfo()
            else:
                self.flag = 1
                self.inflag = 0
                self.out_counts += 1
                self.classwise_counts[self.names[cls]]["OUT"] += 1
                self.send_codeinfo()
        self.flag = 0

    def store_classwise_counts(self, cls):
        if self.names[cls] not in self.classwise_counts:
            self.classwise_counts[self.names[cls]] = {"IN": 0, "OUT": 0}

    def display_counts(self, im0):
        labels_dict = {
            str.capitalize(key): f"{'IN ' + str(value['IN']) if self.show_in else ''} "
            f"{'OUT ' + str(value['OUT']) if self.show_out else ''}".strip()
            for key, value in self.classwise_counts.items()
            if value["IN"] != 0 or value["OUT"] != 0
        }
        if labels_dict:
            self.annotator.display_analytics(im0, labels_dict, (104, 31, 17), (255, 255, 255), 10)

    def count(self, im0, cache_dir):
        self.im0 = im0
        if not self.region_initialized:
            self.initialize_region()
            self.region_initialized = True
        self.annotator = Annotator(im0, line_width=self.line_width)
        self.extract_tracks(im0)
        self.annotator.draw_region(reg_pts=self.region, color=(104, 0, 123), thickness=self.line_width * 2)
        for box, track_id, cls in zip(self.boxes, self.track_ids, self.clss):
            self.annotator.box_label(box, label=self.names[cls], color=colors(cls, True))
            self.store_tracking_history(track_id, box)
            self.store_classwise_counts(cls)
            self.annotator.draw_centroid_and_tracks(self.track_line, color=colors(int(cls), True), track_thickness=self.line_width)
            prev_position = None
            if len(self.track_history[track_id]) > 1:
                prev_position = self.track_history[track_id][-2]
            self.count_objects(self.track_line, box, track_id, prev_position, cls)
        self.display_counts(im0)
        self.display_output(im0)
        self.cache_dir = cache_dir
        return self.im0

    def get_flag(self):
        return self.flag

    def save_image_to_cache(self):
        if not os.path.exists(self.cache_dir):
            os.makedirs(self.cache_dir)
        image = Image.fromarray(self.im0)
        image_path = os.path.join(self.cache_dir, f"image_{len(os.listdir(self.cache_dir)) + 1}.jpg")
        image_name = f"image_{len(os.listdir(self.cache_dir)) + 1}.jpg"
        image.save(image_path)
        return image_path, image_name

    def delete_one_image(self):
        image_files = [f for f in os.listdir(self.cache_dir) if f.endswith(".jpg")]
        if image_files:
            image_to_delete = os.path.join(self.cache_dir, image_files[0])
            os.remove(image_to_delete)
            return True
        return False

    def send_codeinfo(self):
        self.incount = self.in_counts
        self.outcount = self.out_counts
        codeOCR.img_path, codeOCR.img_name = self.save_image_to_cache()
        asyncio.run(codeOCR.CarcodeInfo(self.incount, self.outcount, self.inflag, codeOCR.img_name, "001101", 200))





