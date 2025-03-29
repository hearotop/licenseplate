package org.license.utils;
import java.io.IOException;
import java.net.InetAddress;
public class FetchInfo {
    public static boolean PingTest(String Url)
    {
        try {
            InetAddress address = InetAddress.getByName(Url);
            boolean isReachable = address.isReachable(5000); // 设置超时时间为5000毫秒
            if (isReachable) {
                System.out.println("网络连接成功");
                return true;
            } else {
                System.out.println("网络连接失败");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

