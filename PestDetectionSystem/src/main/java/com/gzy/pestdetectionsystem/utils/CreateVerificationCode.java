package com.gzy.pestdetectionsystem.utils;

import org.springframework.stereotype.Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Random;

@Component
public class CreateVerificationCode {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 28;
    private static final int CODE_LENGTH = 4;
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public VerificationCodeImage create() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        Graphics2D graphics2D = (Graphics2D) graphics;
        Random random = new Random();

        graphics.setColor(getRandColor(200, 250, random));
        graphics.fillRect(0, 0, WIDTH, HEIGHT);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 19));
        graphics.setColor(getRandColor(180, 200, random));

        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(WIDTH - 1);
            int y = random.nextInt(HEIGHT - 1);
            int x1 = random.nextInt(6) + 1;
            int y1 = random.nextInt(12) + 1;
            BasicStroke stroke = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
            Line2D line = new Line2D.Double(x, y, x1 + x, y1 + y);
            graphics2D.setStroke(stroke);
            graphics2D.draw(line);
        }

        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            String c = String.valueOf(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            code.append(c);
            Color color = new Color(20 + random.nextInt(110), 20 + random.nextInt(110), random.nextInt(110));
            graphics.setColor(color);
            graphics.drawString(c, 19 * i + 19, 19);
        }
        graphics.dispose();
        return new VerificationCodeImage(code.toString(), image);
    }

    private Color getRandColor(int start, int end, Random random) {
        if (start > 255) {
            start = 91;
        }
        if (end > 255) {
            end = 97;
        }
        int r = start + random.nextInt(end - start);
        int g = start + random.nextInt(end - start);
        int b = start + random.nextInt(end - start);
        return new Color(r, g, b);
    }

    public record VerificationCodeImage(String code, BufferedImage image) {
    }
}
