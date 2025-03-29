import mysql.connector
from src.utils.ConnectDatabase import Database  # 引入自定义的Database类

class CameraAccess:
    def __init__(self, CameraId):
        """ 初始化CameraAccess类，设置摄像头ID """
        self.CameraID = CameraId
        self.videopath = None
        self.database = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")

    def connect_to_database(self):
        """ 连接到数据库，如果连接已经存在且有效，则直接返回连接 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            return self.database.conn
        except mysql.connector.Error as mysql_err:
            print("MySQL错误:", mysql_err)
        except Exception as e:
            print("发生错误:", e)
        return None

    def get_video_path(self):
        """ 获取摄像头的视频地址 """
        conn = self.connect_to_database()
        if conn is None:
            print("数据库连接失败")
            return None

        try:
            with conn.cursor(dictionary=True) as cursor:
                # 查询摄像头视频地址的SQL语句
                sql = "SELECT VideoAdress FROM cameraadip WHERE CameraID = %s"
                cursor.execute(sql, (self.CameraID,))  # 执行SQL语句，传递摄像头ID作为参数
                data = cursor.fetchone()

                if data:
                    self.videopath = data["VideoAdress"]
                    print("视频地址:", self.videopath)

                conn.commit()  # 提交事务
                return self.videopath
        except mysql.connector.Error as mysql_err:
            print("MySQL错误:", mysql_err)
        except Exception as e:
            print("发生错误:", e)
        finally:
            try:
                if conn and conn.is_connected():
                    conn.close()  # 关闭数据库连接
                    print("数据库连接已关闭")
            except mysql.connector.Error as closing_err:
                print("关闭数据库连接时发生错误:", closing_err)

        return None




