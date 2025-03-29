package org.license;

import org.license.UI.LoginPanel;

import org.license.UI.MainFrame;
import org.license.utils.FetchInfo;

import javax.swing.*;

/**
 * 主程序入口类
 */
public class Main {

    /**
     * 主函数，程序入口点
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        // 设置VLC库路径，这里是为了指定VLC播放器的安装路径
        final String vlcLibraryPath = "/usr/lib/vlc"; // 替换为VLC实际安装路径
        System.setProperty("播放器地址：", vlcLibraryPath); // 设置系统属性，指定VLC库路径
        final String url = "localhost"; // 检测网络连接的目标地址
        Boolean isPing = FetchInfo.PingTest(url); // 检测目标地址的网络连通性
        if (!isPing) {
            JOptionPane.showMessageDialog(null, "网络连接失败"); // 如果网络连接失败，显示消息对话框提示用户
        }
        else {
            SwingUtilities.invokeLater(() -> {
            LoginPanel loginWindow = new LoginPanel();
            });
        }

    }
}
