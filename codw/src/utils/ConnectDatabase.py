import datetime
import mysql.connector


class Database:
    def __init__(self, host, user, password, database, port):
        """
        初始化Database对象。

        参数：
        host (str): 数据库主机地址。
        user (str): 数据库用户名。
        password (str): 数据库密码。
        database (str): 数据库名称。
        port (int): 数据库端口号。
        """
        self.host = host
        self.user = user
        self.password = password
        self.database = database
        self.port = port
        self.conn = None

    def connect(self):
        """
        连接到MySQL数据库。

        返回：
        mysql.connector.connection.MySQLConnection or None: 如果连接成功则返回连接对象，否则返回None。
        """
        try:
            self.conn = mysql.connector.connect(
                host=self.host,
                user=self.user,
                port=self.port,
                password=self.password,
                database=self.database,
                connect_timeout=10
            )
            if self.conn.is_connected():
                print("成功连接到MySQL数据库")
            return self.conn
        except mysql.connector.Error as e:
            print(f"连接MySQL数据库时发生错误： {e}")
            return None

    def close_connection(self):
        """
        关闭数据库连接。
        """
        try:
            if self.conn and self.conn.is_connected():
                self.conn.close()
                print("MySQL连接已关闭")
        except mysql.connector.Error as closing_err:
            print(f"关闭MySQL连接时发生错误： {closing_err}")

    def is_inside(self, license_plate):
        """
        检查车辆是否属于本单位。

        参数：
        license_plate (str): 车牌号。

        返回：
        int or None: 如果车辆属于本单位返回1，否则返回0，发生错误返回None。
        """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续

            with self.conn.cursor(dictionary=True) as cursor:
                sql = "SELECT license_plates FROM license_plate WHERE license_plates=%s"
                val = (license_plate,)
                cursor.execute(sql, val)
                code_data = cursor.fetchone()
                self.conn.commit()
                if code_data:
                    return 1  # 车辆属于本单位
                else:
                    return 0  # 车辆不属于本单位

        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()

    def send_code_info(self):
        """
        获取最近10分钟内的识别记录信息。

        返回：
        dict or None: 包含识别记录信息的字典，或者如果发生错误则返回None。
        """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续

            now = datetime.datetime.now()
            minutes_before = (now - datetime.timedelta(seconds=3)).strftime("%Y-%m-%d %H:%M:%S")

            with self.conn.cursor(dictionary=True) as cursor:
                sql = """
                SELECT license_plate, times, image, incount, voice, flag
                FROM recognition_records 
                WHERE times BETWEEN %s AND %s
                ORDER BY times DESC
                LIMIT 1
                """
                val = (minutes_before, now.strftime("%Y-%m-%d %H:%M:%S"))
                cursor.execute(sql, val)
                code_data = cursor.fetchone()
                self.conn.commit()

                if code_data:
                    print(code_data)  # 打印整个字典而不是尝试访问特定键
                else:
                    print("没有找到记录")

                return code_data

        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()
    def add_register(self,license_plates, times, is_inside, flag, tel, name):
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续
            with self.conn.cursor(dictionary=True) as cursor:
                sql = """Insert into recognition_records  (license_plate, times, is_inside, flag,tel, name )
                         VALUES (%s, %s, %s, %s, %s,%s)"""
                print(sql)
                cursor.execute(sql, (license_plates, times, is_inside, flag, tel, name,))
                self.conn.commit()
            return True
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()


    def update_record(self, record_code, license_plates, times, is_inside, flag, tel, name):
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续
            with self.conn.cursor(dictionary=True) as cursor:
                sql = """UPDATE recognition_records 
                               SET license_plate=%s, times=%s, is_inside=%s, flag=%s, tel=%s, name=%s 
                               WHERE record_id=%s"""
                cursor.execute(sql, (license_plates, times, is_inside, flag, tel, name, record_code))
                self.conn.commit()
            return True
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()
    def send_register_info(self):
        """
        获取最近5分钟内的识别记录信息。

        返回：
        dict or None: 包含识别记录信息的字典，或者如果发生错误则返回None。
        """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续
            now = datetime.datetime.now()
            minutes_before = (now - datetime.timedelta(minutes=30)).strftime("%Y-%m-%d %H:%M:%S")
            with self.conn.cursor(dictionary=True) as cursor:
                sql = """
                SELECT record_id,license_plate, times,flag,is_inside,tel,name
                FROM recognition_records 
                WHERE times BETWEEN %s AND %s
                ORDER BY times DESC
                LIMIT 100
                """
                val = (minutes_before, now.strftime("%Y-%m-%d %H:%M:%S"))
                cursor.execute(sql, val)
                code_data = cursor.fetchall()
                self.conn.commit()
                print(code_data)
                return code_data
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()

    def send_oneregister_info(self,carcode):
        """
        获取最近识别记录信息。

        返回：
        dict or None: 包含识别记录信息的字典，或者如果发生错误则返回None。
        """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续

            with self.conn.cursor(dictionary=True) as cursor:
                sql = """
                   SELECT record_id,license_plate, times,flag,is_inside,tel,name
                   FROM recognition_records 
                   WHERE license_plate=%s
                   """
                cursor.execute(sql,(carcode,))
                code_data = cursor.fetchall()
                self.conn.commit()
                print(code_data)
                return code_data
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()
    def delete_register(self, record_id):
        """ 删车牌信息 """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()
                print("成功连接到MySQL数据库")
            with self.conn.cursor(dictionary=True) as cursor:
                sql = """delete from recognition_records  where record_id=%s"""
                cursor.execute(sql, (record_id,))
                if cursor.rowcount > 0:
                    deleteFlag = True
                    print("记录删除成功")
                else:
                    deleteFlag = False
                    print("记录删除失败")
                self.conn.commit()
                return deleteFlag
        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误: {mysql_err}")
        except Exception as ex:
            print(f"异常: {ex}")
        finally:
            if self.conn and self.conn.is_connected():
                try:
                    self.conn.close()
                    print("数据库连接已关闭")
                except mysql.connector.Error as closing_err:
                    print(f"关闭数据库连接时发生错误: {closing_err}")




    def send_cache_info(self):
        """
        获取最近30小时内的识别记录信息。

        返回：
        dict or None: 包含识别记录信息的字典，或者如果发生错误则返回None。
        """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续

            now = datetime.datetime.now()
            thirty_hours_before = (now - datetime.timedelta(hours=30)).strftime("%Y-%m-%d %H:%M:%S")

            with self.conn.cursor(dictionary=True) as cursor:
                sql = """
                SELECT image, voice
                FROM recognition_records 
                WHERE times >= %s
                ORDER BY times
                """
                cursor.execute(sql, (thirty_hours_before,))
                cache_data = cursor.fetchall()

                self.conn.commit()
                return cache_data

        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return None
        except Exception as e:
            print(f"发生错误： {e}")
            return None
        finally:
            self.close_connection()

    def delete_data_info(self):
        """
        删除最近40小时内的识别记录信息。

        返回：
        bool: 删除成功返回True，否则返回False。
        """
        try:
            if not self.conn or not self.conn.is_connected():
                self.connect()  # 确保连接成功再继续

            now = datetime.datetime.now()
            forty_hours_before = (now - datetime.timedelta(hours=40)).strftime("%Y-%m-%d %H:%M:%S")

            with self.conn.cursor() as cursor:
                sql = """
                DELETE FROM recognition_records 
                WHERE times BETWEEN %s AND %s
                """
                cursor.execute(sql, (forty_hours_before, now.strftime("%Y-%m-%d %H:%M:%S")))
                self.conn.commit()
                print("数据记录删除成功！")
                return True

        except mysql.connector.Error as mysql_err:
            print(f"MySQL错误： {mysql_err}")
            return False
        except Exception as e:
            print(f"发生错误： {e}")
            return False
        finally:
            self.close_connection()


# 示例用法
#db = Database(host="localhost", port=3306, database="openaqure", user="hearo", password="ghb754869G.")
#result = db.is_inside('辽F06C06')
#if result == 1:
#print("车辆属于本单位")
#elif result == 0:
#    print("车辆不属于本单位")
#else:
#   print("发生错误或未知情况")
