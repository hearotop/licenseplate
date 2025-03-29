
from flask import Response, Flask, jsonify, request, send_file
# 引入自定义模块
from datetime import datetime
from src.Access.CodeAccess import CodeAccess
from src.Access.GuestAccess import GuestAccess
from src.Access.UserAccess import UserAccess
from src.utils.ConnectDatabase import Database


# 初始化Flask应用
app = Flask(__name__)
# 初始化数据库连接
database = Database(host="localhost", port=3306, database="openaqure", user="root", password="ghb754869G.")
# 初始化CodeAccess对象
codeAccess = CodeAccess()
userAccess=UserAccess()
guestAccess=GuestAccess()
# 定义路由：根据flag参数返回视频流
@app.route('/get_userinfo')
def AuthentFlag():
    username=request.args.get("username")
    password=request.args.get("password")
    user=userAccess.select_user_info(username,password)
    if user is None:
        authentFlag=False
        print(authentFlag)
        return jsonify(authentFlag=authentFlag),404
    else:
        authentFlag=True
        print(authentFlag)
        return jsonify(authentFlag=authentFlag),200
@app.route('/update_userinfo')
def update_user():
    username=request.args.get("username")
    password=request.args.get("password")
    user=userAccess.select_user_info(username,password)
    if user is None:
        updatatFlag=False
        print( updatatFlag)
        return jsonify( updatatFlag= updatatFlag),404
    else:
        updatatFlag=True
        print(updatatFlag)
        return jsonify(updatatFlag=updatatFlag),200
@app.route('/get_carcode')
def send_Carcode():
    carcode = request.args.get("carcode")
    guestName = request.args.get("guest_name")

    if carcode:
        data = codeAccess.select_one_carcode(carcode)
    elif guestName:
        data = codeAccess.select_one_carcode(guestName)
    else:
        data = codeAccess.select_carcode()

    if data is None:
        return jsonify({"message": "No data found"}), 404
    else:
        return jsonify(data), 200


@app.route('/get_video/in_video')
def index():
    flag = request.args.get('flag')  # 获取URL参数中的flag
    if flag is None:
        return jsonify({"error": "缺少 flag 参数"}), 400
    try:
        flag = int(flag)  # 转换为整数
        if flag == 0:
            return 200

        elif flag == 1:
            return 200

        else:
            return jsonify({"error": "不支持的 flag 参数"}), 400
    except ValueError:
        return jsonify({"error": "flag 参数必须是整数"}), 400
# 定义路由：返回前端是否可以获取信息的标志
@app.route('/get_flag')
def send_flag():

  flag =1
  if flag == 0: flag = False
  if flag == 1: flag = True
  return jsonify(flag), 200
# 定义路由：返回数据库中的CodeInfo信息
@app.route('/get_codeinfo')
def send_codeinfo():
    dataflag=int(request.args.get("flag"))
    if dataflag == 1:
        data = database.send_code_info()
        print(data)
        return jsonify(data), 200
    else:
        return 404
@app.route('/get_guestinfo')
def send_guestinfo():
    guest_id=request.args.get("guestid")
    guest_name=request.args.get("guestname")
    data=guestAccess.select_guest_info(guest_id,guest_name)

    print(data)
    return jsonify(data), 200







@app.route('/update_carinfo', methods=['GET'])
def update_carinfo():
    try:
        carcode = request.args.get("carcode")
        car_id = request.args.get("car_id")

        if not carcode or not car_id:
            return jsonify({"error": "缺少carcode或car_id参数"}), 400

        # Decode the URL-encoded carcode
        #carcode = urllib.parse.unquote(carcode)

        print(f"收到的car_id: {car_id}")
        print(f"收到的carcode: {carcode}")

        updateFlag = codeAccess.update_carcode(carcode, car_id)

        return jsonify({"updateFlag": updateFlag}), 200 if updateFlag else 500

    except Exception as ex:
        print(f"发生异常: {ex}")
        return jsonify({"error": "发生错误", "details": str(ex)}), 500


# 定义路由：根据flag参数返回图像数据
@app.route('/get_image/<filename>')
def get_image(filename):
    imgflag = int(request.args.get("flag"))
    print(type(imgflag))
    print(filename)
    print(imgflag)
    if imgflag == 1:
        try:
            filepath = r"/media/hearo/Datas/CarCode/src/image_cache/" + filename
            print(filepath)
            return send_file(filepath, as_attachment=True), 200  # 设置为附件形式发送，用户可以选择保存位置
        except Exception as e:
            return str(e), 500  # 发生错误时返回错误信息和状态码
    else:
        return "ImgFlag 值为 0 不受支持。", 400  # 返回一个错误信息和状态码400表示客户端错误
@app.route('/add_carinfo')
def add_carinfo():
    carcode = request.args.get("carcode")
    GuestId = request.args.get("guest_id")
    print(carcode)
    addflag=codeAccess.insert_carcode(carcode,GuestId)
    if (addflag):
        return jsonify({"addFlag": True}),200
    else:
        return jsonify({"addFlag": False}),404

@app.route('/add_registerinfo', methods=['GET', 'POST'])
def add_registerinfo():
    try:
        license_plates = request.args.get('license_plates')
        times = request.args.get('times').strip()
        is_inside = request.args.get('isinside')
        flag = request.args.get('flag')
        tel = request.args.get('tel')
        name = request.args.get('name')

        # Check for missing parameters
        if None in [license_plates, times, is_inside, flag, tel, name]:
            return jsonify({"error": "缺少必要参数"}), 400
        # Convert is_inside and flag to integers
        is_inside = int(is_inside)
        flag = int(flag)

        update_success = database.add_register(license_plates, times, is_inside, flag, tel, name)

        if update_success:
            return jsonify({"addFlag": True})
        else:
            return jsonify({"addFlag": False}), 500
    except ValueError:
        return jsonify({"error": "参数格式错误"}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/update_registerinfo', methods=['GET', 'POST'])
def update_registerinfo():
    try:
        if request.method == 'POST':
            record_code = request.form.get('record_code') or request.args.get('record_code')
            license_plates = request.form.get('license_plates') or request.args.get('license_plates')
            times = request.form.get('times') or request.args.get('times')
            is_inside = request.form.get('is_inside') or request.args.get('is_inside')
            flag = request.form.get('flag') or request.args.get('flag')
            tel = request.form.get('tel') or request.args.get('tel')
            name = request.form.get('name') or request.args.get('name')
        else:
            record_code = request.args.get('record_code')
            license_plates = request.args.get('license_plates')
            times = request.args.get('times')
            is_inside = request.args.get('is_inside')
            flag = request.args.get('flag')
            tel = request.args.get('tel')
            name = request.args.get('name')

        if None in [record_code, license_plates, times, is_inside, flag, tel, name]:
            return jsonify({"error": "缺少必要参数"}), 400

        # 将is_inside和flag转换为整数
        is_inside = int(is_inside)
        flag = int(flag)
        update_success = database.update_record(record_code, license_plates, times, is_inside, flag, tel, name)

        if update_success:
            return jsonify({"updateFlag": True})
        else:
            return jsonify({"updateFlag": False}), 500
    except ValueError:
        return jsonify({"error": "参数格式错误"}), 400
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/delete_carinfo')
def delete_carinfo():
    car_id = request.args.get("car_id")
    deleteflag=codeAccess.delete_carcode(car_id)
    if (deleteflag):
        return jsonify({"deleteflag": True}),200
    else:
        return jsonify({"deleteflag": False}),404

@app.route('/delete_registerinfo')
def delete_registerinfo():
    record_id = request.args.get("record_id")
    deleteflag=database.delete_register(record_id)
    if (deleteflag):
        return jsonify({"deleteflag": True}),200
    else:
        return jsonify({"deleteflag": False}),404
@app.route('/get_registerinfo')
def get_register_info():
    carcode = request.args.get("carcode")
    if carcode is None:
        data = database.send_register_info()
        if data is None:
            return jsonify(None), 501
        else:
            FixRegisterData(data)
            return jsonify(data), 200

    else:
        data = database.send_oneregister_info(carcode)
        if data is None:
            return jsonify(None), 501
        else:
            FixRegisterData(data)
            return jsonify(data), 200



# 定义路由：根据flag参数返回语音数据
@app.route('/get_voice/<filename>')
def get_voice(filename):
    voiceflag = int(request.args.get("flag"))
    print(type(voiceflag))
    print(filename)
    print(voiceflag)
    if voiceflag == 1:
        try:
            filepath = r"/media/hearo/Datas/CarCode/src/Audio_cache/" + filename
            print(filepath)
            return send_file(filepath, as_attachment=True), 200  # 设置为附件形式发送，用户可以选择保存位置
        except Exception as e:
            return str(e), 500  # 发生错误时返回错误信息和状态码
    else:
        return "VoiceFlag 值为 0 不受支持。", 400  # 返回一个错误信息和状态码400表示客户端错误
# 定义函数：开始视频处理

def FixRegisterData(data):
    for i in data:
        # 检查键 'times' 是否存在且其值是否为 datetime 对象
        if 'times' in i and isinstance(i['times'], datetime):
            i['times'] = i['times'].strftime('%Y-%m-%d %H:%M:%S')

        # 检查键 'flag' 是否存在并转换值
        if 'flag' in i:
            if i['flag'] == 1:
                i['flag'] = '进'
            elif i['flag'] == 0:
                i['flag'] = '出'

        # 检查键 'is_inside' 是否存在并转换值
        if 'is_inside' in i:
            if i['is_inside'] == 1:
                i['is_inside'] = '是'
            elif i['is_inside'] == 0:
                i['is_inside'] = '否'

    return data

if __name__ == "__main__":
    try:
        """ 启动进程读取和处理帧 """
        app.run(host='0.0.0.0', port=5000, debug=True)
    except KeyboardInterrupt:
        print("检测到键盘中断。停止视频处理。")
        #video_thread.join()  # 等待视频处理线程结束
    except Exception as e:
        print(f"发生意外错误: {e}")