import ntplib
import requests
from datetime import datetime

class NTPTime:
    def __init__(self):
        self.ntp = ntplib.NTPClient()
        self.ntp_servers = [
    'time.nist.gov',
    'nist1.dc.certifiedtime.com',
    'time-a.nist.gov',
    'time.google.com',
    'time.windows.com',
    'ntp.aliyun.com',
    'ntp1.baidu.com',
    'ntp1.nscx.org',
    'ntp2.nscx.org',
]

        self.current_time = None

    def get_time_from_ntp(self):
        for server in self.ntp_servers:
            try:
                response = self.ntp.request(server)
                return datetime.fromtimestamp(response.tx_time)
            except ntplib.NTPException as e:
                print(f"Could not get time from {server}: {e}")
        return None
    def get_time_from_http(self):
        try:
            response = requests.get('http://worldtimeapi.org/api/timezone/Etc/UTC')
            if response.status_code == 200:
                time_data = response.json()
                return datetime.strptime(time_data['datetime'], "%Y-%m-%dT%H:%M:%S.%fZ")
            else:
                print(f"Failed to get time from HTTP server, status code: {response.status_code}")
        except requests.RequestException as e:
            print(f"HTTP request error: {e}")
        return None
    def get_time(self):
        ntp_time = self.get_time_from_ntp()
        if ntp_time:
            return ntp_time
        else:
            print("Could not get time from NTP servers, trying HTTP server.")
            http_time = self.get_time_from_http()
            if http_time:
                return http_time
            else:
                print("Could not get time from HTTP server either.")
                return None
    def format_time_for_database(self, dt):
        # 星期几的英文到中文的映射字典
        day_of_week_map = {
            'Mon': '周一',
            'Tue': '周二',
            'Wed': '周三',
            'Thu': '周四',
            'Fri': '周五',
            'Sat': '周六',
            'Sun': '周日'
        }
        # 月份的英文到数字的映射字典
        month_map = {
            'Jan': '01', 'Feb': '02', 'Mar': '03', 'Apr': '04',
            'May': '05', 'Jun': '06', 'Jul': '07', 'Aug': '08',
            'Sep': '09', 'Oct': '10', 'Nov': '11', 'Dec': '12'
        }

        # 转换时间格式
        formatted_time = dt.strftime("%a %b %d %H:%M:%S %Y")

        # 将英文星期和月份转换为中文
        for en_day, cn_day in day_of_week_map.items():
            if en_day in formatted_time:
                formatted_time = formatted_time.replace(en_day, cn_day)
                break
        for en_month, num_month in month_map.items():
            if en_month in formatted_time:
                formatted_time = formatted_time.replace(en_month, num_month)
                break

        # 返回格式化后的时间字符串
        return formatted_time

    def cover_database(self):
        current_time = self.get_time()
        if current_time:
            # 获取格式化后的时间字符串（中文星期和月份）
            formatted_time_str = self.format_time_for_database(current_time)
           # print(f"Formatted time for database: {formatted_time_str}")
            # 进一步处理存储到数据库中
            database_format = current_time.strftime("%Y-%m-%d %H:%M:%S")
            #print(f"Database format: {database_format}")
            # 可选返回格式化的时间字符串以供进一步的数据库操作
            return database_format
        else:
            print("Failed to get the current time.")
            return None

# Example usage:
#if __name__ == "__main__":
#   ntp = NTPTime()
#  print(ntp.cover_database())
