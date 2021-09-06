package cn.com.dyg.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class AttachServiceUtil {
    public static String upload(File file, CloseableHttpClient httpClient, String file_url, String appid, String secret) {

        String base64;
        InputStream in = null;
        CloseableHttpResponse response = null;
        byte[] bytes = null;
        Map<String, Object> map = null;
        HttpPost httpPost = null;
        StringEntity s = null;
        try {
            in = new FileInputStream(file);
            bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = Base64.encodeBase64String(bytes);
            String contentType = new MimetypesFileTypeMap().getContentType(file);
            String fileName = file.getName();

            map = new HashMap<>();
            map.put("contentType", contentType);
            map.put("fielData", base64);
            map.put("fileName", fileName);
            String jsonString = JSON.toJSONString(map);

            String token = AttachTokenUtil.getInstance().queryToken(file_url, appid, secret);
            String url = file_url + "/api/file/upload" + "?access_token=" + token;
            httpPost = new HttpPost(url);

            s = new StringEntity(jsonString, "UTF-8");
            s.setContentType("application/json");
            httpPost.setEntity(s);
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String jsonResult = EntityUtils.toString(entity, "utf-8");
            JSONObject jsonObject = JSON.parseObject(jsonResult);
            String fileId = (String) jsonObject.get("data");
            return fileId;


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (response != null) {
                HttpClientUtils.closeQuietly(response);
            }
            bytes = null;
            map = null;
            httpPost = null;
            s = null;
        }
        return null;
    }
}
