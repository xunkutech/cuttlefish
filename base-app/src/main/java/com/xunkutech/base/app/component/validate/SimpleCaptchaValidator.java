package com.xunkutech.base.app.component.validate;

import com.xunkutech.base.app.util.AesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

@Component
public class SimpleCaptchaValidator extends AbstractValidator {

    static final String FILE_TYPE = "jpeg";

    @Value("#{'${captcha.bg.color:255,255,255}'.split(',')}")
    String[] bgRGB;

    @Value("#{'${captcha.fg.color:141,52,79}'.split(',')}")
    String[] fgRGB;

    @Value("${captcha.padding.top:8}")
    int paddingTop;

    @Value("${captcha.padding.left:8}")
    int paddingLeft;

    @Value("${captcha.code.length:6}")
    int length;

    @Value("${captcha.expire.seconds:600}")
    long expireSeconds;

    public byte[] createCaptchaImage(String captchaCode) {

        Color fg = new Color(Integer.parseInt(fgRGB[0]), Integer.parseInt(fgRGB[1]), Integer.parseInt(fgRGB[2]));
        Color bg = new Color(Integer.parseInt(bgRGB[0]), Integer.parseInt(bgRGB[1]), Integer.parseInt(bgRGB[2]));
        int width = captchaCode.length() * 14 + paddingLeft * 2;
        int height = 16 + paddingTop * 2;
        Font font = new Font("Arial", Font.BOLD, 20);
        BufferedImage cpImg = new BufferedImage(width, height, BufferedImage.OPAQUE);
        Graphics g = cpImg.createGraphics();

        g.setFont(font);
        g.setColor(bg);
        g.fillRect(0, 0, width, height);
        g.setColor(fg);
        g.drawString(captchaCode, paddingLeft, paddingTop + 16);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ImageIO.write(cpImg, FILE_TYPE, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    @Override
    public String newCode() {
        return AesUtils.randomAlphabetCode(length);
    }

    @Override
    protected void checkExpire(Instant timestamp) throws ExpireException {
        if (null != timestamp && timestamp.plusSeconds(expireSeconds).isBefore(Instant.now())) {
            throw new ExpireException();
        }
    }
}
