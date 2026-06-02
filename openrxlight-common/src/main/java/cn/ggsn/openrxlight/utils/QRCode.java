package cn.ggsn.openrxlight.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public class QRCode {
    public static final String FORMAT = "jpg";
    public static final String CONTENT_TYPE = "image/jpeg";
    private static final String CHARSET = "utf-8";
    private static final int LOGO_MAX_WIDTH = 50;
    private static final int LOGO_MAX_HEIGHT = 50;
    private static final int QR_CODE_MAX_WIDTH = 500;
    private static final int QR_CODE_MAX_HEIGHT = 500;

    public static BufferedImage createQRCode(String content, int width, int height)
            throws WriterException, IOException {
        if (width > QR_CODE_MAX_WIDTH) {
            width = QR_CODE_MAX_WIDTH;
        }
        if (height > QR_CODE_MAX_HEIGHT) {
            height = QR_CODE_MAX_HEIGHT;
        }
        // 二维码参数设置
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        // 安全等级，最高h
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 编码设置
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        // 设置margin=0-10
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }

    public static void insertLogo(BufferedImage source,
            InputStream logo,
            int sourceWidth,
            int sourceHeight,
            int logoWidth,
            int logoHeight) throws IOException {
        Image src = ImageIO.read(logo);
        if (logoWidth > LOGO_MAX_WIDTH) {
            logoWidth = LOGO_MAX_WIDTH;
        }
        if (logoHeight > LOGO_MAX_HEIGHT) {
            logoHeight = LOGO_MAX_HEIGHT;
        }
        Image instance = src.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
        BufferedImage tag = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(instance, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        src = instance;
        // 插入LOGO
        Graphics2D graph = source.createGraphics();
        int x = (sourceWidth - logoWidth) / 2;
        int y = (sourceHeight - logoHeight) / 2;
        graph.drawImage(src, x, y, logoWidth, logoHeight, null);
        Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoHeight, 6, 6);
        graph.setStroke(new BasicStroke(3f));
        graph.draw(shape);
        graph.dispose();
    }
}
