package cn.com.dyg.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientUtil {
    public static String sendPost(String jsonString, CloseableHttpClient httpClient, String url) {

        CloseableHttpResponse response = null;
        try {
            HttpPost post = new HttpPost(url);
            StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            response = httpClient.execute(post);
            HttpEntity en = response.getEntity();
            String content = EntityUtils.toString(en, "utf-8");
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                HttpClientUtils.closeQuietly(response);
            }
        }
        return null;
    }
}
