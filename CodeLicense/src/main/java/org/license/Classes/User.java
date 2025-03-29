package org.license.Classes;

public class User {
    private String username;
    private String password;

    // 构造函数
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 验证用户信息的方法


}
