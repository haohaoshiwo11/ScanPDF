package cn.com.dyg.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 获取访问需要的token,并放入缓存中，90分钟后失效
 *
 * @author ruoweiqing
 */
public class AttachTokenUtil {


    private ConcurrentHashMap<String, String> tokenMap = new ConcurrentHashMap<String, String>();

    private volatile static AttachTokenUtil instance;

    private AttachTokenUtil() {
    }

    public static AttachTokenUtil getInstance() {
        if (instance == null) {
            synchronized (AttachTokenUtil.class) {
                if (instance == null) {
                    instance = new AttachTokenUtil();
                }
            }
        }
        return instance;
    }

    static {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {

                AttachTokenUtil.getInstance().clearCache();

            }
        }, 60 * 90, 60 * 90, TimeUnit.SECONDS);
    }

    public String queryToken(String file_url, String appid, String secret) throws IOException, URISyntaxException {
        String tokenString = tokenMap.get("token");
        if (tokenString == null) {
            synchronized (AttachTokenUtil.class) {
                String tokenString2 = tokenMap.get("token");
                if (tokenString2 == null) {
                    String newToken = requestToken(file_url, appid, secret);
                    tokenMap.put("token", newToken);
                    return newToken;
                }
                return tokenString2;
            }
        }
        return tokenString;
    }

    private String requestToken(String file_url, String appid, String secret) throws URISyntaxException, IOException {

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        URIBuilder builder = null;
        HttpGet httpGet = null;
        try {
            client = HttpClients.createDefault();
            builder = new URIBuilder(file_url + "/api/auth/token");

            builder.addParameter("appid", appid);
            builder.addParameter("secret", secret);


            httpGet = new HttpGet(builder.build());
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity, "utf-8");

            JSONObject jsonVO = JSON.parseObject(content);
            JSONObject data = (JSONObject) jsonVO.get("data");
            String code = (String) data.get("access_token");
            return code;
        } finally {
            if (response != null) {
                response.close();
            }
            if (client != null) {
                client.close();
            }
            client = null;
            builder = null;
            httpGet = null;
        }

    }

    private void clearCache() {
        tokenMap.clear();
    }

}
