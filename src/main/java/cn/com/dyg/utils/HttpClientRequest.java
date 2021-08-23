package cn.com.dyg.utils;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

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
        //DefaultConnectionKeepAliveStrategy 默认实现
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                Args.notNull(response, "HTTP response");
                final HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    final HeaderElement he = it.nextElement();
                    final String param = he.getName();
                    final String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (final NumberFormatException ignore) {
                        }
                    }
                }
                return 1;
            }

        };

        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).setExpectContinueEnabled(true).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST)).setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).setConnectionRequestTimeout(30 * 1000).setConnectTimeout(30 * 1000).setSocketTimeout(30 * 1000).build();
        httpClient = HttpClients.custom().setConnectionManager(pcm).setKeepAliveStrategy(myStrategy).setDefaultRequestConfig(requestConfig).build();

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
