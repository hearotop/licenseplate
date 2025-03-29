from sympy.parsing.sympy_parser import null

from src.utils.ConnectDatabase import Database


class GuestAccess:
    def __init__(self):
        self.database = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")
        self.data = null

    def select_guest_info(self, guest_id, guest_name):
        conn = self.database.connect()
        if conn:
            cursor = conn.cursor()
            if guest_id == "null" and guest_name == "null":
                cursor.execute("SELECT * FROM guests")
                self.data = cursor.fetchall()
                print(self.data)
            else:
                query = "SELECT * FROM guests WHERE guest_name like %s"
                params = (f'%{guest_name}%',)
                cursor.execute(query, params)
                self.data = cursor.fetchall()

            cursor.close()
            conn.close()
        return self.data

    def update_guest_info(self, guest_id, guest_name):
        conn = self.database.connect()
        if conn:
            cursor = conn.cursor()
            self.data = cursor.execute("update guests set guest_name=%s where guest_id=%s", (guest_name, guest_id))
            conn.commit()
        else:
            print("数据库连接失败")

    def delete_guest_info(self, guest_id):
        conn = self.database.connect()
        if conn:
            cursor = conn.cursor()
            self.data = cursor.execute("delete from guests where guest_id=%s", guest_id)
            conn.commit()

    def add_guest_info(self, guest_id, guest_name):
        conn = self.database.connect()
        if conn:
            cursor = conn.cursor()
            self.data = cursor.execute("insert into guests(guest_id,guest_name) values(%s,%s)", (guest_id, guest_name))
            conn.commit()

    def send_guest_info(self):
        return self.data
