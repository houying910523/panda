package com.mv.data.panda.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * author: houying
 * date  : 16-11-3
 * desc  :
 */
public final class HttpUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static PoolingHttpClientConnectionManager cm;
    static {
        cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(8);
    }

    public static class HttpResult {
        private String content;
        private int statusCode;

        public HttpResult(String content, int statusCode) {
            this.content = content;
            this.statusCode = statusCode;
        }

        public String getContent() {
            return content;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static HttpResult doGet(String url) throws IOException {
        CloseableHttpResponse response = null;
        try {
            HttpClientBuilder builder = HttpClients.custom()
                    .setConnectionManager(cm);
                    //.setProxy(new HttpHost("127.0.0.1", 8085));

            CloseableHttpClient client = builder.build();
            HttpGet get = new HttpGet(url);
            response = client.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                return new HttpResult(EntityUtils.toString(response.getEntity()), 200);
            } else {
                return new HttpResult(null, response.getStatusLine().getStatusCode());
            }
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
                response.close();
            }
        }
    }
}
