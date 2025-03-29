package org.license.utils;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class Ntp {
    // NTP 服务器列表
    private final List<String> NTPservers;

    public Ntp() {
        // 初始化 NTP 服务器列表
        NTPservers = new ArrayList<>();
        NTPservers.add("pool.ntp.org"); // 全球NTP服务器池
        NTPservers.add("time.windows.com"); // 微软提供的NTP服务器
        NTPservers.add("time.nist.gov"); // 美国国家标准与技术研究所提供的NTP服务器
        NTPservers.add("time.apple.com"); // 苹果提供的NTP服务器
        NTPservers.add("time.google.com"); // 谷歌提供的NTP服务器
        NTPservers.add("cn.pool.ntp.org"); // 中国NTP服务器池
        // 添加额外的备用服务器
        NTPservers.add("ntp.aliyun.com"); // 阿里云提供的NTP服务器
        NTPservers.add("time.cloudflare.com"); // Cloudflare提供的NTP服务器
    }

    public String getNTPTime() {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(10000);
        try {
            client.open();
            for (String ntpServer : NTPservers) {
                try {
                    InetAddress hostAddr = InetAddress.getByName(ntpServer);
                    TimeInfo info = client.getTime(hostAddr);
                    info.computeDetails(); // 计算时间
                    Date date = new Date(info.getMessage().getTransmitTimeStamp().getTime());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINESE);
                    return sdf.format(date);
                } catch (IOException e) {
                    // 如果获取某个服务器时间失败，尝试下一个服务器
                    continue;
                }
            }
            return "无法获取时间";
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }
}
