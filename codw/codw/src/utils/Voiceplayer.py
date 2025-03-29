import asyncio
import aiohttp
import os
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

class VoicePlayer:
    def __init__(self, license_plate):
        """
        初始化AsyncVoicePlayer对象
        参数:
        license_plate (str): 车牌号码，用于生成语音提示信息。
        """
        self.API_KEY = "z2B5jNuVbMOC5fcGBkezhEpw"  # 百度API的API Key
        self.SECRET_KEY = "57anANydp5HeLnws2v8lATzCifCilbrx"  # 百度API的Secret Key
        self.url = "https://tsn.baidu.com/text2audio"  # 百度语音合成API的URL
        self.audio_cachedir = r"/media/hearo/Datas/CarCode/src/Audio_cache"  # 音频缓存目录
        self.license_plate = license_plate  # 车牌号
        self.text1 = '欢迎%s已检测，检测成功！祝您工作愉快！生活幸福'
        self.text = '欢迎%s已检测，检测成功！非本单位车辆，请下车接受登记相关信息，谢谢您的合作！'  # 要转换为语音的文本模板
        self.file = None  # 缓存的音频文件名
        self.counter = self.get_initial_counter()  # 获取初始计数器值，基于现有文件
        self.is_inside = None  # 是否为本单位车辆
        # 确保缓存目录存在且可写
        os.makedirs(self.audio_cachedir, exist_ok=True)
        if not os.access(self.audio_cachedir, os.W_OK):
            raise PermissionError(f"目录没有写权限：{self.audio_cachedir}")

    def get_initial_counter(self):
        """
        获取缓存目录中现有文件的初始计数器值。
        """
        max_counter = 0
        for filename in os.listdir(self.audio_cachedir):
            if filename.startswith("in_audio_") and filename.endswith(".mp3"):
                try:
                    counter = int(filename.split("_")[2].split(".")[0])
                    if counter > max_counter:
                        max_counter = counter
                except ValueError:
                    continue
        return max_counter + 1

    async def cache_audio(self):
        """
        使用百度语音合成API将文本转换为语音并缓存音频文件。
        """
        text = self.get_text()
        filename = self.generate_filename()
        self.file = os.path.basename(filename)
        logging.info(f"文件名称：{self.file}")
        logging.info(f"要转换为语音的文本：{text}")
        payload = {
            'tex': text,
            'tok': await self.get_access_token(),
            'cuid': 'py55nBme18YbpeZ94qzuz7Jz6rUtNNIr',
            'ctp': 1,
            'lan': 'zh',
            'spd': 5,
            'pit': 5,
            'vol': 5,
            'per': 1,
            'aue': 3
        }
        headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': '*/*'
        }
        try:
            async with aiohttp.ClientSession() as session:
                async with session.post(self.url, headers=headers, data=payload) as response:
                    response.raise_for_status()  # 如果HTTP请求错误，抛出异常
                    content = await response.read()
                    with open(filename, "wb") as f:
                        f.write(content)
                    logging.info(f"语音合成音频已缓存至：{filename}")
                    self.counter += 1  # 递增计数器以便下一个文件
        except aiohttp.ClientError as e:
            logging.error(f"从API获取语音合成音频失败。错误：{e}")
        except PermissionError as e:
            logging.error(f"写入文件{filename}时发生权限错误。错误：{e}")
        except Exception as e:
            logging.error(f"发生意外错误：{e}")

    def get_text(self):
        """
        根据是否为本单位车辆返回相应的文本。
        """
        if self.is_inside:
            return self.text1 % self.license_plate
        else:
            return self.text % self.license_plate

    def generate_filename(self):
        """
        生成音频文件名。
        """
        return os.path.join(self.audio_cachedir, f"in_audio_{self.counter}.mp3")

    def get_file(self):
        """
        获取当前缓存的音频文件名。
        """
        return self.file

    async def get_access_token(self):
        """
        获取百度API的访问令牌。
        """
        url = "https://aip.baidubce.com/oauth/2.0/token"
        params = {
            "grant_type": "client_credentials",
            "client_id": self.API_KEY,
            "client_secret": self.SECRET_KEY
        }
        try:
            async with aiohttp.ClientSession() as session:
                async with session.post(url, params=params) as response:
                    response.raise_for_status()  # 如果HTTP请求错误，抛出异常
                    data = await response.json()
                    access_token = data.get("access_token")
                    if access_token:
                        return access_token
                    else:
                        raise ValueError("从响应中获取访问令牌失败。")
        except aiohttp.ClientError as e:
            raise ConnectionError(f"连接到百度API失败。错误：{e}")

    async def delete_audio(self, is_in_audio=True):
        """
        删除缓存的音频文件。

        参数:
        is_in_audio (bool): 如果为True，则删除输入音频文件；如果为False，则删除输出音频文件。
                            默认为True。
        """
        filename = f"in_audio_{self.counter - 1}.mp3" if is_in_audio else f"out_audio_{self.counter - 1}.mp3"
        filepath = os.path.join(self.audio_cachedir, filename)
        try:
            if os.path.exists(filepath):
                os.remove(filepath)
                logging.info(f"{filepath} 已删除。")
            else:
                logging.warning(f"{filepath} 不存在。")
        except PermissionError as e:
            logging.error(f"删除文件{filepath}时发生权限错误。错误：{e}")
        except Exception as e:
            logging.error(f"发生意外错误：{e}")


