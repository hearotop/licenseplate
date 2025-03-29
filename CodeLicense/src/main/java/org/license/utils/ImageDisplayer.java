package org.license.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;

public class ImageDisplayer {
    public static BufferedImage displayImageFromUrl(String imageUrl, String imagePath, int flag) {
        try {
            URL url = new URL(imageUrl + imagePath + "?flag=" + flag);
            System.out.println(url);
            InputStream inputStream = url.openStream();
            BufferedImage image = ImageIO.read(inputStream);
            return image;
        } catch (Exception e) {
            System.err.println("显示图片时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}