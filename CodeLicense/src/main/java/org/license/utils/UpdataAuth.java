package org.license.utils;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UpdataAuth {
    private String UserUrl="http://localhost:5000/get_userinfo?";
    private String username;
    private String password;

    public UpdataAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public String getUsername() {
        return username;
    }
    public String  encryptPassword(String password) {
        try {
            // 创建MessageDigest实例并指定MD5算法
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 更新摘要，传入密码的字节数组
            md.update(password.getBytes());

            // 计算消息摘要并获取字节数组
            byte[] digestBytes = md.digest();

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digestBytes) {
                hexString.append(String.format("%02x", b));
            }

            // 返回加密后的MD5值
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
    public boolean updataauth() {
        UserUrl="http://localhost:5000/update_userinfo?"+"username="+username+"&password="+encryptPassword(password);
        try {
            URL url = new URL(UserUrl);
            System.out.println(url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode==HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // Print the response for debugging
                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean isAuthenticated = jsonResponse.getBoolean("updataFlag"); // 假设JSON响应中包含一个布尔字段"authenticated"
                return isAuthenticated;
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
