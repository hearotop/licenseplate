package org.license.utils;
import javazoom.jl.player.Player;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

public class AudioPlayer {
    public static void playAudioFromUrl(String audioUrl, String voicepath, int flag) {
        try {

            URL url = new URL(audioUrl + voicepath +"?flag="+flag);
            System.out.println(url);
            InputStream inputStream = new BufferedInputStream(url.openStream());
            Player player = new Player(inputStream);
            player.play();
        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String audioUrl = "http://localhost:5000/get_voice/";
        String voicepath = "out_audio.mp3" + "?flag=";
        int flag = 1;
        playAudioFromUrl(audioUrl, voicepath, flag);
    }
}