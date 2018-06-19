package com.safechat.safechat.encryptor;

/**
 * Created by Sephiroth on 17/5/14.
 */

import javax.crypto.Mac;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class FileUtil {

    public static byte[] file2Byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            int length = (int) file.length();
            if (length > Integer.MAX_VALUE) {
                //JOptionPane.showMessageDialog(null, "File is too big");
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
                byte[] b = new byte[length];
                int n;
                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                fis.close();
                bos.close();
                buffer = bos.toByteArray();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static File byte2File(byte[] input, String path) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(input);
            fos.flush();
            bos.flush();
            fos.close();
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static boolean checkMac(Key k, byte[] fi, byte[] macCode1) {
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(k);
            byte[] macCode2 = mac.doFinal(fi);
            boolean flag = true;
            for (int i = 0; i < macCode1.length && i < macCode2.length; i++) {
                if (macCode1[i] != macCode2[i]) {
                    flag = false;
                    break;
                }
            }
            return flag;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();

            return false;
        }
    }


    public static String generateString(int length) {
        final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }

    public static byte[] arraycat(byte[] buf1, byte[] buf2) {
        byte[] bufret = null;
        int len1 = 0;
        int len2 = 0;
        if (buf1 != null)
            len1 = buf1.length;
        if (buf2 != null)
            len2 = buf2.length;
        if (len1 + len2 > 0)
            bufret = new byte[len1 + len2];
        if (len1 > 0)
            System.arraycopy(buf1, 0, bufret, 0, len1);
        if (len2 > 0)
            System.arraycopy(buf2, 0, bufret, len1, len2);
        return bufret;
    }

    public static byte[] deleteHeader(byte[] file, int i) {
        if (file.length - i > 0) {
            byte[] result = new byte[file.length - i];
            for (int index = 0; i < file.length; i++, index++) {
                result[index] = file[i];
            }
            return result;
        } else {
            return null;
        }

    }
}

