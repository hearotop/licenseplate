import datetime
import os
from src.utils.ConnectDatabase import Database


class DeleteCache:
    def __init__(self):
        self.cache = {}
        self.deleted = False
        self.deleted_count = 0
        self.delete_Image = r"C:src/image_cache/"
        self.deleted_Audio = r"src/Audio_cache/"
        self.db = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")
    def delete_cache(self, filepath, filename):
        filepath = os.path.join(filepath, filename)
        try:
            if os.path.exists(filepath):
                os.remove(filepath)
                print(f"{filepath} 已删除。")
                self.db.delete_data_info()
            else:
                print(f"{filepath} 不存在。")
        except PermissionError:
            print(f"没有权限删除 {filepath}。")

    def delete_filename(self):
        data_cache = self.db.send_cache_info()
        return data_cache















