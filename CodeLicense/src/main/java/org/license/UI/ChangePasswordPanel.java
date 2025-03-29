package org.license.UI;

import org.license.Classes.User;
import org.license.utils.UpdataAuth;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChangePasswordPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton submitButton;
    private JPanel change;

    public JPanel changePasswordPanel() {
        change = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加用户名标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        change.add(new JLabel("用户名："), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        change.add(usernameField, gbc);

        // 添加密码标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 1;
        change.add(new JLabel("密码："), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        change.add(passwordField, gbc);

        // 添加确认密码标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 2;
        change.add(new JLabel("确认密码："), gbc);

        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        change.add(confirmPasswordField, gbc);

        // 提交按钮
        submitButton = new JButton("提交");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                if (password.equals(confirmPassword)) {
                    boolean isAuthenticated = updataauthenticateUser(username, password);
                    if (isAuthenticated) {
                        JOptionPane.showMessageDialog(null, "密码更改成功！");
                    } else {
                        JOptionPane.showMessageDialog(null, "密码更改失败！");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "两次输入的密码不一致，请重新输入！");
                }
            }
        });
        change.add(submitButton, gbc);

        return change;
    }

    private boolean updataauthenticateUser(String username, String password) {
        // 实现用户验证逻辑
        User user = new User(username, password);
        UpdataAuth updataAuth = new UpdataAuth(username, password);
        return updataAuth.updataauth();
    }
}
