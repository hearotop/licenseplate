import mysql

from src.utils.ConnectDatabase import Database

class UserAccess:
    def __init__(self):
        """ 初始化USerAccess类 """
        self.database = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")

    def select_user_info(self,username,password):
        """ 对比信息数据库 """
        try:
            if not self.database.conn or not self.database.conn.is_connected():
                self.database.connect()
                print("成功连接到MySQL数据库")
            with self.database.conn.cursor() as cursor:
                sql = """SELECT * FROM user where username=%s and password=%s"""
                val = (username,password)
                cursor.execute(sql, val)
                user_data = cursor.fetchone()

                self.database.conn.commit()
                print("数据查询成功")
                return user_data

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
def update_user_info(self, username, password):
    """ 对比信息数据库 """
    updateFlag = False
    try:
        if not self.database.conn or not self.database.conn.is_connected():
            self.database.connect()
            print("成功连接到MySQL数据库")
        with self.database.conn.cursor() as cursor:
            sql = """UPDATE user SET password=%s WHERE username=%s"""
            val = (password, username)
            cursor.execute(sql, val)
            if cursor.rowcount > 0:
                updateFlag = True
                print("用户密码更改成功")
            else:
                updateFlag = False
                print("用户密码更改失败")
            self.database.conn.commit()
            return updateFlag
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
    return updateFlag


