package org.license.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RegisterInfo {
    private String RegisterUrl="http://localhost:5000/get_registerinfo";
    private  String CarCode;
    private  URL url;
    public String getCarcode()
    {
        return CarCode;
    }
    public void setCarCode(String carcode)
    {
        this.CarCode=carcode;
    }
    public boolean registerinfo(String licensePlate, String times, Integer isInside,Integer flag, String tel,String name) {
        try {
            String encodedLicensePlate = URLEncoder.encode(licensePlate, StandardCharsets.UTF_8.toString());
            String encodedTimes = URLEncoder.encode(times, StandardCharsets.UTF_8.toString());
            String encodedTel = URLEncoder.encode(tel, StandardCharsets.UTF_8.toString());
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
            String addregisterUrl = String.format("http://localhost:5000/add_registerinfo?license_plates=%s&times=%s&isinside=%d&flag=%d&tel=%s&name=%s",
                    encodedLicensePlate, encodedTimes, isInside, flag, encodedTel, encodedName);
            System.out.println(addregisterUrl);
            URL url = new URL(addregisterUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean updatatFlag = jsonResponse.getBoolean("addFlag");
                return updatatFlag;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public JSONObject getregistertable() {
        try {
            if (CarCode != null && CarCode.length() > 0) {
                url = new URL(RegisterUrl + "?carcode=" + URLEncoder.encode(CarCode, StandardCharsets.UTF_8.toString()));
            } else {
                url = new URL(RegisterUrl);
            }
            System.out.println(url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response from server: " + response.toString());
                JSONArray jsonArray = new JSONArray(response.toString());
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("data", jsonArray);
                return jsonResponse;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public  boolean deleteRegister(int recordId)
    {
        try {
            // 对carcode参数进行URL编码
            String deleteCarUrl = "http://localhost:5000/delete_registerinfo?"+ "record_id=" + recordId;
            System.out.println(deleteCarUrl);
            URL url = new URL(deleteCarUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // 打印服务器响应以进行调试
                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean updatatFlag = jsonResponse.getBoolean("deleteflag"); // 假设JSON响应中包含一个布尔字段"updatatFlag"
                return updatatFlag;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }


    public boolean updateregisterinfo(int record_code, String license_plates, String times, int is_inside, int flag, String tel, String name) {
        try {
            String encodedLicensePlates = URLEncoder.encode(license_plates, StandardCharsets.UTF_8.toString());
            String encodedTimes = URLEncoder.encode(times, StandardCharsets.UTF_8.toString());
            String encodedTel = URLEncoder.encode(tel, StandardCharsets.UTF_8.toString());
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());

            String UpCarUrl = "http://localhost:5000/update_registerinfo?"
                    + "record_code=" + record_code
                    + "&license_plates=" + encodedLicensePlates
                    + "&times=" + encodedTimes
                    + "&is_inside=" + is_inside
                    + "&flag=" + flag
                    + "&tel=" + encodedTel
                    + "&name=" + encodedName;

            URL url = new URL(UpCarUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean updateFlag = jsonResponse.getBoolean("updateFlag");
                return updateFlag;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
