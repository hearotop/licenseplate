package org.license.UI;
import org.license.Classes.User;
import org.license.utils.LoginAuth;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
public class LoginPanel {
    private JFrame frame;
    private User user;
    public LoginPanel() {
        frame = new JFrame("登录窗口");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        placeComponents(panel);
        frame.setLocationRelativeTo(null); // 设置窗口居中显示
        frame.setVisible(true);
    }
    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("用户名：");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        // 添加键盘事件监听器，限制只能输入英文字符
        userText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetterOrDigit(c)) {
                    e.consume();
                }
            }
        });

        JLabel passwordLabel = new JLabel("密   码：");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);
        JButton loginButton = new JButton("登录");

        // 添加键盘事件监听器，限制只能输入英文字符
        passwordText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetterOrDigit(c)) {
                    e.consume();
                }
            }
                                        @Override
                                        public void keyPressed(KeyEvent e) {
                                            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                                loginButton.doClick();  // 当按下Enter键时，自动触发登录按钮
                                            }
                                        }
        }

        );

        JCheckBox showPassword = new JCheckBox("显示密码");
        showPassword.setBounds(100, 80, 165, 25);
        panel.add(showPassword);
        showPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPassword.isSelected()) {
                    passwordText.setEchoChar((char) 0);
                } else {
                    passwordText.setEchoChar('*');
                }

            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout()); // 修改布局管理器为FlowLayout

        buttonPanel.add(loginButton); // 将loginButton添加到buttonPanel中
        buttonPanel.setBounds(100, 110, 165, 35);
        panel.add(buttonPanel); // 将buttonPanel添加到panel中

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                System.out.println(username);
                System.out.println(password);

                // 在这里实现与后端的通信，验证用户信息
                boolean isAuthenticated = authenticateUser(username, password);
                if (isAuthenticated) {
                    // 验证成功，创建并显示主界面
                    SwingUtilities.invokeLater(() -> {
                        MainFrame mainFrame = new MainFrame();
                        mainFrame.setVisible(true);
                    });
                    frame.dispose(); // 取消显示登录窗口
                } else {
                    // 验证失败，提示用户错误信息
                    JOptionPane.showMessageDialog(frame, "用户名或密码错误，请重新输入！", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    private boolean authenticateUser(String username, String password) {
        // 实现用户验证逻辑
         user = new User(username, password);
        LoginAuth loginAuth = new LoginAuth(username, password);
        Boolean is_logoin=loginAuth.authenticate();

        return  is_logoin; // 示例验证逻辑
    }
}
