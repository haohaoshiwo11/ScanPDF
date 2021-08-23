package cn.com.dyg.utils;

import org.apache.http.Consts;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

public class HttpClientRequest {
    private volatile static HttpClientRequest instance;
    private CloseableHttpClient httpClient;

    private HttpClientRequest() {

        //添加连接参数
        ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).build();
        //添加socket参数
        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        //配置连接池管理器
        PoolingHttpClientConnectionManager pcm = new PoolingHttpClientConnectionManager();
        // 设置最大连接数
        pcm.setMaxTotal(10);
        // 设置每个连接的路由数
        pcm.setDefaultMaxPerRoute(5);
        //设置连接信息
        pcm.setDefaultConnectionConfig(connectionConfig);
        //设置socket信息
        pcm.setDefaultSocketConfig(socketConfig);
        //设置全局请求配置,包括Cookie规范,HTTP认证,超时
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setExpectContinueEnabled(true).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST)).setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).setConnectionRequestTimeout(30 * 1000).setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).build();
        httpClient = HttpClients.custom().setConnectionManager(pcm).setDefaultRequestConfig(requestConfig).build();

    }

    public static HttpClientRequest newInstance() {
        if (instance == null) {
            synchronized (HttpClientRequest.class) {
                if (instance == null) {
                    instance = new HttpClientRequest();
                }
            }
        }
        return instance;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
}
