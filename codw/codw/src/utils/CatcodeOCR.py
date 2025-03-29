import base64
import aiohttp
import aiofiles
from src.Access.CodeAccess import CodeAccess
from src.utils.ConnectDatabase import Database
from src.utils.Voiceplayer import VoicePlayer
import asyncio

db = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")

class CatcodeOCR:
    def __init__(self):
        """ 初始化CatcodeOCR类 """
        self.img_path = "/media/hearo/Datas/CarCode/src/image_cache"  # 图片路径
        self.access_token = "24.05884c12ca1302f2845ecbf849cba5a5.2592000.1734838803.282335-81441825"
        self.request_url = "https://aip.baidubce.com/rest/2.0/ocr/v1/license_plate"
        self.img_name = None  # 图片名称

    async def OCR(self):
        """ 使用百度OCR识别车牌 """
        async with aiofiles.open(self.img_path, 'rb') as f:
            img = base64.b64encode(await f.read())
        params = {"image": img.decode('utf-8')}
        url = f"{self.request_url}?access_token={self.access_token}"
        headers = {'content-type': 'application/x-www-form-urlencoded'}
        async with aiohttp.ClientSession() as session:
            async with session.post(url, data=params, headers=headers) as response:
                if response.status == 200:
                    json_data = await response.json()
                    print(json_data)
                    return json_data
                else:
                    print(f"请求失败，状态码: {response.status}")
                    return None

    async def CarcodeInfo(self, incount, outcount, flag, img, cameraid, parknum):
        """ 获取车牌信息并将其插入到数据库 """
        json_data = await self.OCR()
        if json_data and "words_result" in json_data:
            car_info = {}
            licese = CodeAccess()
            licese.outcount = outcount
            licese.incount = incount
            licese.license_plate = car_info["number"] = json_data["words_result"].get("number")
            player = VoicePlayer(licese.license_plate)
            player.is_inside = db.is_inside(licese.license_plate)

            await player.cache_audio()
            print(f"缓存的音频文件名：{player.get_file()}")

            licese.image = await self.convert_image_to_binary()
            voice = player.file
            licese.insert_license_plate(flag, img, cameraid, parknum, voice, player.is_inside)

    async def convert_image_to_binary(self):
        """ 将图片转换为二进制数据 """
        async with aiofiles.open(self.img_path, 'rb') as file:
            binary_data = await file.read()
        return binary_data

