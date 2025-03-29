package org.license.utils;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GetGuestInfo {
    private URL url;
    private String guestUrl;
    private String gusetName="";
    private int guestId=0;

    public GetGuestInfo() {
        this.guestUrl = "http://localhost:5000/get_guestinfo";
    }




    public void setGusetName(String gusetName) {
        this.gusetName = gusetName;
    }
    public String getGuestName() {
        return gusetName;
    }

    public int getGuestId() {
        return guestId;
    }
    public JSONObject getguesttable() {
        try {
            URL url = null;
            if (guestId == 0 && gusetName.isEmpty()) {
                url = new URL(guestUrl + "?guestname=null&guestid=null");
            } 
            if(gusetName!=null && !gusetName.isEmpty()){
                url = new URL(guestUrl + "?guestname=" +URLEncoder.encode(gusetName, StandardCharsets.UTF_8));
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response from server: " + response.toString());

                JSONArray jsonArray = new JSONArray(response.toString());
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("data", jsonArray);
                return jsonResponse;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean addguestinfo(String carcode, int guestId) {
        try {
            String encodedCarcode = URLEncoder.encode(carcode, StandardCharsets.UTF_8.toString());
            String encodedGuestId = URLEncoder.encode(String.valueOf(guestId), StandardCharsets.UTF_8.toString());
            String addCarUrl = "http://localhost:5000/add_carinfo?guest_name=" + encodedCarcode + "&guest_id=" + encodedGuestId;
            System.out.println(addCarUrl);
            URL url = new URL(addCarUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean addFlag = jsonResponse.getBoolean("addFlag");
                return addFlag;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteGuestInfo(int guestId) {
        try {
            String deleteCarUrl = "http://localhost:5000/delete_carinfo?guest_id=" + guestId;
            System.out.println(deleteCarUrl);
            URL url = new URL(deleteCarUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean updatatFlag = jsonResponse.getBoolean("deleteflag");
                return updatatFlag;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateGuestInfo(String guestName, int guestId) {
        try {
            String encodedCarcode = URLEncoder.encode(guestName, StandardCharsets.UTF_8.toString());
            String UpCarUrl = "http://localhost:5000/update_carinfo?guest_name=" + encodedCarcode + "&guest_id=" + guestId;
            System.out.println(UpCarUrl);
            URL url = new URL(UpCarUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("Response from server: " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean updatatFlag = jsonResponse.getBoolean("updateFlag");
                return updatatFlag;
            } else {
                System.err.println("HTTP error code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error Information for User: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
