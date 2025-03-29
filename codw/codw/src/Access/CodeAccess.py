import datetime
import mysql.connector
from src.utils.ConnectDatabase import Database

class CodeAccess:
    def __init__(self):
        """ 初始化CodeAccess类 """
        self.license_plate = None
        self.current_time = None
        self.incount = None
        self.outcount = None
        self.resu = None
        self.database = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")

    def insert_license_plate(self, flag, img_path, cameraid, parknumber, voice,is_inside):
        """ 插入车牌识别记录到数据库 """
        flag = 1 if flag else 0
        now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        self.current_time = now
        result = {}
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            with self.database.conn.cursor() as cursor:
                sql = """
                INSERT INTO recognition_records (
                    license_plate, times, image, incount, voice, outcount, flag, CameraID, parknumber,is_inside
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s,%s)
                """
                val = (
                    self.license_plate, self.current_time, img_path, self.incount,
                    voice, self.outcount, flag, cameraid, parknumber,is_inside
                )
                cursor.execute(sql, val)
                self.database.conn.commit()
                print("数据插入成功")
                result['number'] = self.license_plate
                result['current_time'] = self.current_time
                self.resu = self.current_time
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
            result['error'] = f"MySQL错误: {mysql_err}"
        except Exception as ex:
            print(f"异常: {ex}")
            result['error'] = f"异常: {ex}"
        finally:
            if self.database.conn and self.database.conn.is_connected():
                try:
                    self.database.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")
                    result['error'] = f"关闭数据库连接时发生错误: {closing_err}"

        return result

    def select_one_carcode(self,carcode):
        """ 获取车牌信息 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            with self.database.conn.cursor(dictionary=True) as cursor:
                sql = """
               SELECT 
    lp.car_id as car_id,
    lp.license_plates as license_plates,
    g.guest_id as guest_id ,
    g.guest_name as guest_name
FROM 
    license_plate lp
INNER JOIN 
    guests g ON lp.guest_id = g.guest_id
                WHERE 
                    lp.license_plates = %s OR g.guest_name like %s
                """
                val = (carcode, "%"+carcode+"%")
                cursor.execute(sql,val)
                code_data = cursor.fetchall()
                self.database.conn.commit()
                print("数据查询成功")
                return code_data
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
        except Exception as ex:
            print(f"异常: {ex}")
        finally:
            if self.database.conn and self.database.conn.is_connected():
                try:
                    self.database.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")
    def select_carcode(self):
        """ 获取车牌信息 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            with self.database.conn.cursor(dictionary=True) as cursor:
                sql = """SELECT 
    lp.car_id as car_id,
    lp.license_plates as license_plates,
    g.guest_id as guest_id ,
    g.guest_name as guest_name
FROM 
    license_plate lp
INNER JOIN 
    guests g ON lp.guest_id = g.guest_id"""
                cursor.execute(sql)
                code_data = cursor.fetchall()
                self.database.conn.commit()
                print("数据查询成功")
                return code_data
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
        except Exception as ex:
            print(f"异常: {ex}")
        finally:
            if self.database.conn and self.database.conn.is_connected():
                try:
                    self.database.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")

    def update_carcode(self, newcarcode, car_id):
        """ 更新车牌信息 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")

            with self.database.conn.cursor(dictionary=True) as cursor:
                sql = """UPDATE license_plate SET license_plates=%s WHERE car_id=%s"""
                cursor.execute(sql, (newcarcode, car_id))
                updateFlag = cursor.rowcount > 0
                self.database.conn.commit()
                print("车牌更新成功" if updateFlag else "车牌更新失败")
                return updateFlag

        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
            return False  # 在捕获MySQL错误时返回False
        except Exception as ex:
            print(f"异常: {ex}")
            return False  # 在捕获一般异常时返回False
        finally:
            if self.database.conn and self.database.conn.is_connected():
                try:
                    self.database.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")

    def insert_carcode(self, carcode,GuestId):
        """ 更新车牌信息 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            with self.database.conn.cursor(dictionary=True) as cursor:
                sql = """insert into  license_plate (license_plates,guest_id) VALUES(%s,%s)"""
                cursor.execute(sql,(carcode,int(GuestId)))
                if cursor.rowcount > 0:
                    insertFlag = True
                    print("车牌插入成功")
                else:
                    insertFlag = False
                    print("车牌插入失败")
                self.database.conn.commit()
                return insertFlag
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
        except Exception as ex:
            print(f"异常: {ex}")
        finally:
            if self.database.conn and self.database.conn.is_connected():
                try:
                    self.database.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")

    def delete_carcode(self, car_id):
        """ 删车牌信息 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            with self.database.conn.cursor(dictionary=True) as cursor:
                sql = """delete from license_plate where car_id=%s"""
                cursor.execute(sql, (car_id,))
                if cursor.rowcount > 0:
                    deleteFlag = True
                    print("车牌删除成功")
                else:
                    deleteFlag = False
                    print("车牌删除失败")
                self.database.conn.commit()
                return deleteFlag
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
        except Exception as ex:
            print(f"异常: {ex}")
        finally:
            if self.database.conn and self.database.conn.is_connected():
                try:
                    self.database.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")


