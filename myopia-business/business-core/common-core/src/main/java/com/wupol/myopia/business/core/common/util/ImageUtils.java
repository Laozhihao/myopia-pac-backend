package com.wupol.myopia.business.core.common.util;


import cn.hutool.core.codec.Base64;
import com.wupol.myopia.base.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片文件工具类
 *
 * @author Simple4H
 */
public class ImageUtils {
    /**
     * 图片转换成base64格式
     *
     * @param imageUrl 图片URL
     * @return 图片base64编码
     */
    public static String imageToBase64(String imageUrl) {
        try {
            BufferedImage image;
            URL url = new URL(imageUrl);
            image = ImageIO.read(url);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            String type = StringUtils.substring(imageUrl, imageUrl.lastIndexOf(".") + 1);
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            String imageString = Base64.encode(imageBytes);
            bos.close();
            return imageString;
        } catch (Exception ignored) {
            throw new BusinessException("图片转换成Base64异样");
        }
    }

    /**
     * 批量图片转换成base64格式
     *
     * @param imageUrls 图片URL
     * @return 图片base64编码
     */
    public static List<String> batchImageToBase64(List<String> imageUrls) {
        List<String> base64List = new ArrayList<>();
        imageUrls.forEach(i -> base64List.add(imageToBase64(i)));
        return base64List;
    }
}