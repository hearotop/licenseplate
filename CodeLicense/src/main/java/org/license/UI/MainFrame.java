package org.license.UI;
import org.license.Access.CodeInfoAccess;
import org.license.utils.AudioPlayer;
import org.license.utils.ImageDisplayer;
import org.license.utils.Ntp;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 主界面类，继承自JFrame，用于显示门禁车牌识别系统的主界面和操作。
 */
public class MainFrame extends JFrame {
    private CodeInfoAccess access = new CodeInfoAccess(); // 数据访问对象
    private EmbeddedMediaPlayerComponent mediaPlayerComponent; // 嵌入式视频播放器组件
    private JLabel nowTimeLabel; // 当前时间标签
    private JLabel licenseLabel; // 车牌号标签
    private JLabel timeLabel; // 进出时间标签
    private JLabel carActiveLabel; // 车辆进出状态标签
    private JLabel inimageLabel; // 进入车辆抓拍图像标签
    private final String imgurl = "http://localhost:5000/get_image/"; // 获取图像的URL
    private final Ntp ntp = new Ntp(); // NTP类实例，用于获取网络时间
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss"); // 日期格式
    private BufferedImage img; // 图像缓冲区
    private String VoicePath; // 声音路径
    private String imagePath; // 图像路径
    private JPanel mainPanel; // 主面板
    private JPanel registerPanel;
    private JPanel videoPanel;
    private JPanel panel;
    private  JPanel infoPanel;
    private    JPanel imagePanel;// 登记面板
    private JPanel CardPanel;
    private JPanel userPanel;
    private  JPanel changepasswordPanel;
    private JLabel CountInfo;

    private JLabel InCountInfo;
    private JLabel OutCountInfo;
    /**
     * 构造方法，初始化主界面窗口和组件。用来工厂提示司机下车进行信息登记，同时做到语音提醒。
     */
    public MainFrame() {
        super("车牌识别门禁播报系统"); // 设置窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置窗口关闭操作
        setSize(1200, 600); // 设置窗口大小
        setJMenuBar(createMenuBar()); // 创建并设置菜单栏
        mainPanel = new JPanel(new BorderLayout()); // 创建主面板
        registerPanel = new RegisterPanel().getRegisterPanel(); // 创建登记面板
        CardPanel= new CardPanel().cardPanel();
        userPanel=new UserPanel().userPanel();
        changepasswordPanel= new ChangePasswordPanel().changePasswordPanel();
        mainPanel.add(createVideoPanel(), BorderLayout.WEST); // 添加视频显示面板
        mainPanel.add(createInfoPanel(), BorderLayout.CENTER); // 添加信息显示面板
        mainPanel.add(createImagePanel(), BorderLayout.EAST); // 添加图像显示面板
        add(mainPanel); // 将主面板添加到窗口中
        setVisible(true); // 设置窗口可见
        setLocationRelativeTo(null); // 设置窗口居中显示
        updateNtpTime(); // 异步获取NTP时间
        // Start a scheduled task to update time every second
        ScheduledExecutorService timeUpdater = Executors.newScheduledThreadPool(1);
        timeUpdater.scheduleAtFixedRate(this::updateTime, 0, 1700, TimeUnit.MILLISECONDS);
    }
    /**
     * 创建菜单栏。
     * @return 创建好的菜单栏对象
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar(); // 创建菜单栏
        JMenuItem fileMenuItem = new JMenuItem("首页"); // 创建菜单项
        fileMenuItem.addActionListener(e -> {
            mainPanel.removeAll(); // 移除主面板的所有组件
            mainPanel.add(createVideoPanel(), BorderLayout.WEST); // 添加视频显示面板
            mainPanel.add(createInfoPanel(), BorderLayout.CENTER); // 添加信息显示面板
            mainPanel.add(createImagePanel(), BorderLayout.EAST); // 添加图像显示面板
            mainPanel.revalidate(); // 重新验证主面板
            mainPanel.repaint(); // 重绘主面板
        });
        JMenuItem registerItem = new JMenuItem("出入登记"); // 登记菜单项
        JMenuItem CardItem = new JMenuItem("车牌录入"); // 登记菜单项
        JMenu UserMenu=new JMenu("系统管理");
        JMenuItem GuestItem=new JMenuItem("人员信息");
        JMenuItem FixPassword=new JMenuItem("修改密码");

        JMenuItem exitItem = new JMenuItem("退出"); // 创建退出菜单项

        UserMenu.add(FixPassword);
        FixPassword.addActionListener(e->
        {
            mainPanel.removeAll();
            mainPanel.add(changepasswordPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });



        UserMenu.add(exitItem);
        menuBar.add(fileMenuItem); // 将菜单添加到菜单栏中
        menuBar.add(CardItem);
     // menuBar.add(GuestItem);
        menuBar.add(registerItem); // 将登记菜单项添加到菜单栏中
        CardItem.addActionListener(e->
        {
          mainPanel.removeAll();
          mainPanel.add(CardPanel, BorderLayout.CENTER);
          mainPanel.revalidate();
          mainPanel.repaint();
        });
        registerItem.addActionListener(e -> {
            mainPanel.removeAll(); // 移除主面板的所有组件
            mainPanel.add(registerPanel,BorderLayout.CENTER); // 添加登记面板到主面板
            mainPanel.revalidate(); // 重新验证主面板
            mainPanel.repaint(); // 重绘主面板
        });
        menuBar.add(UserMenu,BorderLayout.EAST); // 将退出菜单项添加到菜单中
        exitItem.addActionListener(e -> {
            int result=JOptionPane.showConfirmDialog(null,"确定退出吗？","确定", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }

        }); // 添加退出菜单项的点击事件

        GuestItem.addActionListener(e->
        {
            mainPanel.removeAll();
            mainPanel.add(userPanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        return menuBar; // 返回菜单栏对象
    }
    /**
     * 创建视频显示面板。
     * @return 创建好的视频显示面板对象
     */
    private JPanel createVideoPanel() {
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent(); // 创建嵌入式视频播放器组件
        videoPanel = new JPanel(new GridLayout(1, 1)); // 创建视频面板
        videoPanel.add(createLabeledPanel("进入监控", mediaPlayerComponent)); // 添加带标签的视频播放器到视频面板中
        videoPanel.setPreferredSize(new Dimension(300, 600)); // 设置视频面板首选大小
        return videoPanel; // 返回视频面板对象
    }
    /**
     * 创建带标签的面板。
     * @param labelText 标签文本
     * @param component 要包含的组件
     * @return 创建好的带标签的面板对象
     */
    private JPanel createLabeledPanel(String labelText, Component component) {
        panel = new JPanel(new BorderLayout()); // 创建带边界布局的面板
        JLabel label = new JLabel(labelText); // 创建标签
        label.setHorizontalAlignment(SwingConstants.CENTER); // 设置标签居中对齐
        panel.add(label, BorderLayout.NORTH); // 添加标签到面板的北部（上方）
        panel.add(component, BorderLayout.CENTER); // 添加组件到面板的中心
        return panel; // 返回带标签的面板对象
    }
    /**
     * 创建信息显示面板。
     * @return 创建好的信息显示面板对象
     */
    private JPanel createInfoPanel() {
         infoPanel = new JPanel(new BorderLayout()); // 创建信息面板
        infoPanel.setPreferredSize(new Dimension(300, 600)); // 设置信息面板首选大小
        nowTimeLabel = new JLabel("当前时间："); // 创建当前时间标签
        licenseLabel = new JLabel("车牌号: "); // 创建车牌号标签
        carActiveLabel = new JLabel("检测状态：未开始检测"); // 创建车辆进出状态标签
        timeLabel = new JLabel("抓拍时间: "); // 创建进出时间标签
        // 摄像头选择下拉框
        JLabel camerainfo = new JLabel("监控位置：北山监控");

        camerainfo.setPreferredSize(new Dimension(200, 30));
        JPanel CodePanel = new JPanel(new GridLayout(1, 4)); // 创建代码面板
        CodePanel.add(licenseLabel); // 添加车牌号标签到代码面板
        CodePanel.add(carActiveLabel); // 添加车辆状态标签到代码面板
        CodePanel.add(timeLabel); // 添加时间标签到代码面板
        JPanel labelPanel = new JPanel(new GridLayout(4, 1)); // 创建标签面板
        JPanel parkPanel = new JPanel(new GridLayout(2, 2)); // 创建停车面板
        JPanel comboxPanel = new JPanel(new GridLayout(1, 2)); // 创建组合框面板
        JPanel cPanel = new JPanel(new GridLayout(3, 2)); // 创建C面板
        cPanel.add(camerainfo); // 添加摄像头信息到C面板
        cPanel.add(nowTimeLabel); // 添加当前时间标签到C面板
        comboxPanel.add(cPanel); // 添加C面板到组合框面板
        labelPanel.add(comboxPanel); // 添加组合框面板到标签面板
        labelPanel.add(CodePanel); // 添加代码面板到标签面板
        labelPanel.add(parkPanel); // 添加停车面板到标签面板
        infoPanel.add(labelPanel, BorderLayout.CENTER); // 添加标签面板到信息面板中
        JButton beginButton = new JButton("开始检测"); // 创建开始检测按钮
        beginButton.setPreferredSize(new Dimension(150, 50)); // 设置开始检测按钮的首选大小
        beginButton.addActionListener(e -> playVideo("/media/hearo/Datas/CarCode/src/static/video/03.mp4")); // "http://localhost:5000/get_video/in_video?flag=0"添加开始检测按钮的点击事件：播放视频
        beginButton.addActionListener(e -> setActive()); // 添加开始检测按钮的点击事件：设置车辆检测状态为开始
        beginButton.addActionListener(e -> updateInfo()); // 添加开始检测按钮的点击事件：更新信息
        JPanel buttonPanel = new JPanel(); // 创建按钮面板
        buttonPanel.add(beginButton); // 添加开始检测按钮到按钮面板
        infoPanel.add(buttonPanel, BorderLayout.SOUTH); // 添加按钮面板到信息面板的南部（底部）
        return infoPanel; // 返回信息面板对象
    }

    /**
     * 创建图像显示面板。
     * @return 创建好的图像显示面板对象
     */
    private JPanel createImagePanel() {
        inimageLabel = new JLabel("车辆抓拍");
        inimageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inimageLabel.setOpaque(true); // 设置为不透明，以便设置背景色
        inimageLabel.setBackground(Color.WHITE);
        imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(inimageLabel, BorderLayout.CENTER); // 修改为添加到中心位置，以充满面板
        imagePanel.setBackground(Color.BLACK);
        imagePanel.setPreferredSize(new Dimension(300, 600));
        return imagePanel;
    }

    /**
     * 设置车辆检测状态。
     */
    private void setActive() {
        int flag = 1; // 模拟检测状态
        if (flag == 1)
            carActiveLabel.setText("检测状态：已开始检测"); // 设置车辆状态标签文本为已开始检测
    }
    /**
     * 更新当前时间标签。
     */
    private void updateTime() {
        String currentTime = sdf.format(new Date()); // 格式化当前时间
        nowTimeLabel.setText("当前时间: " + currentTime); // 设置当前时间标签文本
    }

    /**
     * 播放视频。
     * @param inmediaUrl 视频URL
     */
    private void playVideo(String inmediaUrl) {
        new Thread(() -> mediaPlayerComponent.mediaPlayer().media().play(inmediaUrl)).start(); // 在新线程中播放视频
    }
    /**
     * 更新信息。
     * 定时从数据访问对象获取数据，并更新UI显示。
     */
    private void updateInfo() {
        ScheduledExecutorService mainExecutor = Executors.newScheduledThreadPool(1); // 创建定时任务执行器
        mainExecutor.scheduleAtFixedRate(() -> {
            try {
                access.scheduleTask(); // 调用数据访问对象的任务调度方法
                Map<String, Object> data = access.getData(); // 获取数据

                SwingUtilities.invokeLater(() -> {
                    if (data != null) {
                        Integer active = (Integer) data.get("flag"); // 获取状态标志
                        String license_plate = (String) data.get("license_plate"); // 获取车牌号
                        String times = (String) data.get("times"); // 获取时间字符串



                        String imagePath = (String) data.get("image"); // 获取图像路径
                        VoicePath= (String) data.get("voice"); // 获取声音路径

                        if (active != null && license_plate != null && times != null) { // 如果获取到完整的数据
                            licenseLabel.setText("车牌号: " + license_plate);
                            timeLabel.setText("检测时间: " + times);// 设置车牌号标签文本
                            PlayerVoice(access.getVoiceFlag());
                            BufferedImage img = ImageDisplayer.displayImageFromUrl(imgurl, imagePath, access.getImageFlag()); // 从URL显示图像
                            ImageIcon scaledIcon = getScaledImageIcon(img, inimageLabel.getWidth(), inimageLabel.getHeight()); // 获取缩放后的图像
                            updateImage(scaledIcon); // 更新图像显示
                        } else {
                            // 处理数据不完整的情况
                            System.out.println("部分数据为空: active=" + active + ", license_plate=" + license_plate + ", times=" + times);
                            PlayerVoice(0); // 播放默认声音
                            BufferedImage img = ImageDisplayer.displayImageFromUrl(imgurl, imagePath, 0); // 从URL显示默认图像
                            ImageIcon scaledIcon = getScaledImageIcon(img, inimageLabel.getWidth(), 0); // 获取缩放后的图像
                            updateImage(scaledIcon); // 更新图像显示
                        }
                    } else {
                        // 处理未获取到数据的情况
                        System.out.println("未获取到数据。");
                    }

                    // 处理完信息后将标志设为0
                    access.setVoiceFlag(0);
                    access.setImageFlag(0);
                    access.setFlag(0);
                });
            } catch (Exception e) {
                e.printStackTrace(); // 输出异常信息
            }
        }, 0, 3000, TimeUnit.MILLISECONDS); // 初始延迟0秒，每1.6秒执行一次
    }

    /**
     * 获取缩放后的图像图标。
     * @param img 原始图像
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩放后的图像图标
     */
    private ImageIcon getScaledImageIcon(BufferedImage img, int maxWidth, int maxHeight) {
        int imgWidth = img.getWidth(); // 获取原始图像宽度
        int imgHeight = img.getHeight(); // 获取原始图像高度
        float widthRatio = (float) maxWidth / imgWidth; // 宽度比例
        float heightRatio = (float) maxHeight / imgHeight; // 高度比例
        float ratio = Math.min(widthRatio, heightRatio); // 获取最小比例
        int newWidth = (int) (imgWidth * ratio); // 计算新宽度
        int newHeight = (int) (imgHeight * ratio); // 计算新高度
        Image scaledImage = img.getScaledInstance(newWidth,newHeight, Image.SCALE_SMOOTH); // 获取缩放后的图像
        return new ImageIcon(scaledImage); // 返回缩放后的图像图标
    }
    /**
     * 更新图像显示。
     * @param imageIcon 要显示的图像图标
     */
    private void updateImage(ImageIcon imageIcon) {
        inimageLabel.setIcon(imageIcon);
        imagePanel.add(inimageLabel, BorderLayout.CENTER);
    }
    /**
     * 播放声音。
     * @param VoiceFlag 声音标志
     */
    private void PlayerVoice(int VoiceFlag) {
        String voiceUrl = "http://localhost:5000/get_voice/"; // 声音URL
        AudioPlayer.playAudioFromUrl(voiceUrl, VoicePath, VoiceFlag);// 从URL播放声音
    }

    /**
     * 更新NTP时间。
     */
    private void updateNtpTime() {
        SwingWorker<String, Void> ntpWorker = new SwingWorker<String, Void>() { // 创建SwingWorker对象
            @Override
            protected String doInBackground() throws Exception {
                return ntp.getNTPTime(); // 后台获取NTP时间
            }
            @Override
            protected void done() {
                try {
                    String ntpTime = get(); // 获取NTP时间
                    nowTimeLabel.setText("当前时间: " + ntpTime); // 设置当前时间标签文本为获取到的NTP时间
                } catch (Exception e) {
                    updateTime(); // 如果获取NTP时间失败，则显示本地时间
                }
            }
        };
        ntpWorker.execute(); // 执行SwingWorker任务
    }
}
