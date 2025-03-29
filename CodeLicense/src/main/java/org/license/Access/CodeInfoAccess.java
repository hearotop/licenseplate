package org.license.Access;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * CodeInfoAccess 类用于访问远程接口获取代码信息并处理响应数据。
 */
public class CodeInfoAccess {
    private volatile Map<String, Object> responseInfo; // 响应信息
    private volatile Map<String, Object> processedData = new HashMap<>(); // 处理后的数据
    private volatile Boolean VoiceFlag; // 声音标志
    private volatile Boolean ImageFlag; // 图像标志
    private volatile Boolean flag;
    private volatile Boolean sflag;// 标志

    /**
     * 发送 GET 请求并获取响应数据。
     *
     * @param urlString 请求的URL地址
     * @return 响应数据字符串
     * @throws IOException 发生IO异常时抛出
     */
    private String sendGetRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        if (responseCode != 200) {
            throw new IOException("HTTP 响应状态码: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * 获取代码信息接口的标志信息。
     *
     * @return 标志信息，true 或 false
     * @throws IOException 发生IO异常时抛出
     */
    public Boolean getCodeInfo() throws IOException {
        String url = "http://localhost:5000/get_flag";
        String response = sendGetRequest(url);

        if ("true".equalsIgnoreCase(response.trim())) {
            return true;
        } else if ("false".equalsIgnoreCase(response.trim())) {
            return false;
        } else {
            throw new RuntimeException("意外的响应: " + response);
        }
    }

    /**
     * 获取完整的代码信息。
     *
     * @return 包含代码信息的 Map 对象
     * @throws IOException 发生IO异常时抛出
     */
    public Map<String, Object> getInfo() throws IOException {
        String url = "http://localhost:5000/get_codeinfo?flag="+sFlag();
        String response = sendGetRequest(url);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, Map.class);
    }

    /**
     * 获取图像标志。
     *
     * @return 图像标志，1 或 0
     */
    public int getImageFlag() {
        if (ImageFlag) return 1;
        else return 0;
    }
    public int sFlag() {
        if (sflag) return 1;
        else return 0;
    }

    /**
     * 获取声音标志。
     *
     * @return 声音标志，1 或 0
     */
    public int getVoiceFlag() {
        if (VoiceFlag) return 1;
        else return 0;
    }

    /**
     * 定期执行任务，获取代码信息并处理响应数据。
     */
    public void scheduleTask() {
        try {
            flag = getCodeInfo();
            System.out.println("标志: " + flag);
            if (flag) {
                VoiceFlag = flag;
                ImageFlag = flag;
                sflag=flag;
                // 休眠1秒钟
                TimeUnit.MILLISECONDS.sleep(500);
                responseInfo = getInfo();
                processedData = processResponseInfo();

            }
        } catch (IOException e) {
            System.err.println("执行任务时出错: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 处理响应数据，将其转换为易于访问的格式。
     *
     * @return 处理后的数据的 Map 对象
     */
    public Map<String, Object> processResponseInfo() {
        System.out.println("开始处理响应信息:");
        Map<String, Object> tempProcessedData = new HashMap<>();
        if (responseInfo != null) {
            System.out.println("处理响应信息:");
            for (Map.Entry<String, Object> entry : responseInfo.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if ("flag".equals(key)) {
                    int flag = (int) value;
                    tempProcessedData.put("flag", flag);
                } else if ("image".equals(key)) {
                    String imagePath = (String) value;
                    tempProcessedData.put("image", imagePath);
                } else if ("incount".equals(key)) {
                    int incount = (int) value;
                    tempProcessedData.put("incount", incount);
                } else if ("license_plate".equals(key)) {
                    String licensePlate = (String) value;
                    tempProcessedData.put("license_plate", licensePlate);
                } else if ("times".equals(key)) {
                    String timestamp = (String) value;
                    tempProcessedData.put("times", timestamp);
                } else if ("voice".equals(key)) {
                    String audioPath = (String) value;
                    tempProcessedData.put("voice", audioPath);
                } else {
                    System.out.println("未处理的键: " + key);
                }
            }
            return tempProcessedData;
        } else {
            System.out.println("响应信息为空，无需处理。");
        }
        return null;
    }

    /**
     * 获取处理后的数据。
     *
     * @return 处理后的数据的 Map 对象
     */
    public Map<String, Object> getData() {
        return processedData;
    }

    /**
     * 主函数，用于测试代码。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        CodeInfoAccess access = new CodeInfoAccess();
        access.scheduleTask();
        ScheduledExecutorService mainExecutor = Executors.newScheduledThreadPool(1);
        mainExecutor.scheduleAtFixedRate(() -> {
            Map<String, Object> data = access.getData();
            if (data != null) {
                Integer active = (Integer) data.get("flag");
                String licensePlate = (String) data.get("license_plate");
                String times = (String) data.get("times");
                if (active != null && licensePlate != null && times != null) {
                    System.out.println("Code: " + licensePlate + ", Times: " + times + ", Active: " + active);
                } else {
                    System.out.println("部分数据为空: " +
                            "active=" + active + ", license_plate=" + licensePlate + ", times=" + times);
                }

                // 处理完信息后将标志设为0
                access.setVoiceFlag(0);
                access.setImageFlag(0);
            } else {
                System.out.println("未获取到数据。");
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置声音标志。
     *
     * @param i 声音标志值，1 或 0
     */
    public void setVoiceFlag(int i) {
        VoiceFlag = i == 1;
    }

    /**
     * 设置图像标志。
     *
     * @param i 图像标志值，1 或 0
     */
    public void setImageFlag(int i) {
        ImageFlag = i == 1;
    }

    public void setFlag(int i) {
        flag=i==1;
    }
}
