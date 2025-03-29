package org.license.Classes;



import java.util.Map;

public class CodeInfo {
    private String codeInfo;
    private String captureTime; // 更改为驼峰命名风格，符合Java命名规范
    private int active;


    public CodeInfo() {
        // 默认构造函数
    }

    public CodeInfo(String codeInfo, String captureTime, int active) {
        this.codeInfo = codeInfo;
        this.captureTime = captureTime;
        this.active = active;
    }

    // Getter和Setter方法
    public String getCodeInfo() {
        return codeInfo;
    }

    public void setCodeInfo(String codeInfo) {
        this.codeInfo = codeInfo;
    }

    public String getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(String captureTime) {
        this.captureTime = captureTime;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}

