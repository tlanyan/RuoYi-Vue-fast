package com.ruoyi.common.utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.util.Base64;

public class GAuthUtil {
    public static String generateSecretKey() {
        var gauth = new GoogleAuthenticator();
        var key = gauth.createCredentials();
        return key.getKey();
    }

    public static int generateCode(String secretKey) {
        var gauth = new GoogleAuthenticator();
        return gauth.getTotpPassword(secretKey);
    }

    /**
     * 生成 Google Authenticator Key Uri
     *
     * <p>
     * Google Authenticator 规定的 Key Uri 格式:
     * otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}
     * </p>
     * <p>
     * <a href=
     * "https://github.com/google/google-authenticator/wiki/Key-Uri-Format">https://github.com/google/google-authenticator/wiki/Key-Uri-Format</a>
     * </p>
     * <p>
     * 参数需要进行 url 编码 +号需要替换成%20
     * </p>
     *
     * @param secret  密钥 使用 generateSecretKey 方法生成
     * @param account 用户账户 如: example@domain.com
     * @param issuer  服务名称 如: Google,GitHub
     */
    public static String getGoogleAuthenticatorBarCode(String secret, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secret, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 将图片文件转换成base64字符串
     *
     * @param bytes
     * @return java.lang.String
     */
    private static String imageToBase64(byte[] bytes) {
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 生成二维码（文件），返回图片的base64
     *
     * @param barCode Google Authenticator Key Uri
     * @param outPath 输出地址
     * @param width   宽度
     * @param height  高度
     * @throws WriterException
     * @throws IOException
     */
    public static String createQRCode(String barCode, String outPath, int width, int height)
            throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCode, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream bof = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bof);
        String base64 = imageToBase64(bof.toByteArray());
        try (FileOutputStream out = new FileOutputStream(outPath)) {
            MatrixToImageWriter.writeToStream(matrix, "png", out);

            var file = new File(outPath);
            if (file.exists()) {
                file.delete();
            }
        }
        return base64;
    }

    public static boolean verifyCode(String secretKey, int code) {
        var config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder().setWindowSize(5).build();

        var gauth = new GoogleAuthenticator(config);
        return gauth.authorize(secretKey, code);
    }

    public static void main(String[] args) {
        var secretKey = generateSecretKey();
        var code = generateCode(secretKey);
        System.out.println("secretKey: " + secretKey);
        System.out.println("code: " + code);
        System.out.println("verifyCode: " + verifyCode(secretKey, code));
        System.out.println("verifyCode + 1: " + verifyCode(secretKey, code + 1));

        var barCode = getGoogleAuthenticatorBarCode(secretKey, "admin", "XPay");
        System.out.println("barCode: " + barCode);
        try {
            var qrCode = createQRCode(barCode, "test.png", 200, 200);
            System.out.println("qrCode: " + qrCode);
            System.out.println("qrCode: " + qrCode.length());
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}
