package cn.com.dyg.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
/*
 * AES算法的加密和解密
 * KEY和IV会生成一个密钥，这个密钥可对其他数据进行加密和解密
 * KEY、IV、PASSWORD可随意更改
 */

public class AesCBCUtil {
    public static final String PASSWORD = "nawsw533";
    public static final String KEY = "abcdefgabcdefg12";
    public static final String IV = "abcdefgabcdefg12";

    private static String byteArrayToHexString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();
        //遍历
        for(byte b : byteArray){//16次
            //取出每一个byte类型，进行转换
            String hex = byteToHexString(b);
            //将转换后的值放入StringBuffer中
            sb.append(hex);
        }
        return sb.toString();
    }
    /**
     * 将byte转在16进制字符串
     */
    public static String byteToHexString(byte b) {//-31转成e1，10转成0a，。。。
        //将byte类型赋给int类型
        int n = b;
        //如果n是负数
        if(n < 0){
            //转正数
            //-31的16进制数，等价于求225的16进制数
            n = 256 + n;
        }
        //商(14)，数组的下标
        int d1 = n / 16;
        //余(1)，数组的下标
        int d2 = n % 16;
        //通过下标取值
        return hex[d1] + hex[d2];
    }
    public static String[] hex = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};

    public static String byteToString(byte[] byte1) {
        return new String(byte1);
    }

    //加密
    public static byte[] AES_CBC_Encrypt(byte[] content, byte[] keyBytes, byte[] iv) {

        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            System.out.println("exception:" + e.toString());
        }
        return null;
    }
    //解密
    public static byte[] AES_CBC_Decrypt(byte[] content, byte[] keyBytes, byte[] iv) {

        try {
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            // TODO Auto-generated catchblock
            System.out.println("exception:" + e.toString());
        }
        return null;
    }

    /**
     * 字符串装换成base64
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return Base64.decodeBase64(key.getBytes());
    }

    /**
     * 二进制装换成base64
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return new String(Base64.encodeBase64(key));
    }
}
