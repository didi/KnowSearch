package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/1/25 下午4:04
 * @modified By D10865
 */
public class BaseHttpUtil {
    private static final ILog LOGGER = LogFactory.getLog(BaseHttpUtil.class);
    public static final String UTF8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final String GB2312 = "GB2312";
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int SO_TIMEOUT = 20000;
    private static final int MAX_TOTAL = 200;
    private static final int DEFAULT_MAX_PERROUTE = 50;
    private static final HttpClient HTTP_CLIENT;

    public BaseHttpUtil() {
    }

    public static String post(String url, Map<String, Object> params) {
        return post(url, params, (Map)null, (String)null, (String)null);
    }

    public static String postEncode(String url, Map<String, Object> params, String reqEncode, String resEncode) {
        return post(url, params, (Map)null, reqEncode, resEncode);
    }

    public static String post(String url, Map<String, Object> params, Map<String, String> headers, String reqEncode, String resEncode) {
        HttpPost post = new HttpPost(url);
        if(StringUtils.isBlank(reqEncode)) {
            reqEncode = "UTF-8";
        }

        if(StringUtils.isBlank(resEncode)) {
            resEncode = "UTF-8";
        }

        List httpParams = null;
        Iterator var7;
        if(params != null && !params.isEmpty()) {
            httpParams = new ArrayList(params.size());
            var7 = params.entrySet().iterator();

            while(true) {
                Map.Entry e;
                while(var7.hasNext()) {
                    e = (Map.Entry)var7.next();
                    String k = (String)e.getKey();
                    Object v = e.getValue();
                    if(v == null) {
                        httpParams.add(new BasicNameValuePair(k, (String)null));
                    } else if(!v.getClass().isArray()) {
                        httpParams.add(new BasicNameValuePair(k, v.toString()));
                    } else {
                        int len = Array.getLength(v);

                        for(int i = 0; i < len; ++i) {
                            Object element = Array.get(v, i);
                            if(element != null) {
                                httpParams.add(new BasicNameValuePair(k, element.toString()));
                            } else {
                                httpParams.add(new BasicNameValuePair(k, (String)null));
                            }
                        }
                    }
                }

                if(headers != null) {
                    var7 = headers.entrySet().iterator();

                    while(var7.hasNext()) {
                        e = (Map.Entry)var7.next();
                        post.addHeader((String)e.getKey(), (String)e.getValue());
                    }
                }

                try {
                    post.setEntity(new UrlEncodedFormEntity(httpParams, reqEncode));
                    post.getParams().setParameter("http.protocol.cookie-policy", "compatibility");
                    break;
                } catch (UnsupportedEncodingException var20) {
                    throw new RuntimeException("UTF-8 is not surportted", var20);
                }
            }
        }

        var7 = null;

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, resEncode);
        } catch (Exception var18) {
            throw new RuntimeException("error post data to " + url, var18);
        } finally {
            post.releaseConnection();
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("response=" + response);
        }

        return response;
    }

    public static String postForString(String url, String content, Map<String, String> headers) {
        HttpPost post = new HttpPost(url);

        Iterator var4;
        if(StringUtils.isNotBlank(content)) {
            if(headers != null) {
                var4 = headers.entrySet().iterator();

                while(var4.hasNext()) {
                    Map.Entry<String, String> e = (Map.Entry)var4.next();
                    post.addHeader((String)e.getKey(), (String)e.getValue());
                }
            }

            try {
                BasicHttpEntity requestBody = new BasicHttpEntity();
                requestBody.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
                requestBody.setContentLength((long)content.getBytes("UTF-8").length);
                post.setEntity(requestBody);
                post.getParams().setParameter("http.protocol.cookie-policy", "compatibility");
            } catch (UnsupportedEncodingException var12) {
                throw new RuntimeException("UTF-8 is not surportted", var12);
            }
        }

        var4 = null;

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
        } catch (Exception var10) {
            throw new RuntimeException("error post data to " + url, var10);
        } finally {
            post.releaseConnection();
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("response=" + response);
        }

        return response;
    }
    public static String deleteForString(String url, String content, Map<String, String> headers) {
        HttpDelete post = new HttpDelete(url);

        Iterator var4;
        if(StringUtils.isNotBlank(content)) {
            if(headers != null) {
                var4 = headers.entrySet().iterator();

                while(var4.hasNext()) {
                    Map.Entry<String, String> e = (Map.Entry)var4.next();
                    post.addHeader((String)e.getKey(), (String)e.getValue());
                }
            }

            try {
                BasicHttpEntity requestBody = new BasicHttpEntity();
                requestBody.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
                requestBody.setContentLength((long)content.getBytes("UTF-8").length);
               // post.setEntity(requestBody);
                post.getParams().setParameter("http.protocol.cookie-policy", "compatibility");
            } catch (UnsupportedEncodingException var12) {
                throw new RuntimeException("UTF-8 is not surportted", var12);
            }
        }

        var4 = null;

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
        } catch (Exception var10) {
            throw new RuntimeException("error post data to " + url, var10);
        } finally {
            post.releaseConnection();
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("response=" + response);
        }

        return response;
    }

    public static String putForString(String url, String content, Map<String, String> headers) {
        HttpPut post = new HttpPut(url);

        Iterator var4;
        if(StringUtils.isNotBlank(content)) {
            if(headers != null) {
                var4 = headers.entrySet().iterator();

                while(var4.hasNext()) {
                    Map.Entry<String, String> e = (Map.Entry)var4.next();
                    post.addHeader((String)e.getKey(), (String)e.getValue());
                }
            }

            try {
                BasicHttpEntity requestBody = new BasicHttpEntity();
                requestBody.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
                requestBody.setContentLength((long)content.getBytes("UTF-8").length);
                 post.setEntity(requestBody);
                post.getParams().setParameter("http.protocol.cookie-policy", "compatibility");
            } catch (UnsupportedEncodingException var12) {
                throw new RuntimeException("UTF-8 is not surportted", var12);
            }
        }

        var4 = null;

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(post).getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
            EntityUtils.consume(entity);
        } catch (Exception var10) {
            throw new RuntimeException("error post data to " + url, var10);
        } finally {
            post.releaseConnection();
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("response=" + response);
        }

        return response;
    }


    public static String get(String url, Map<String, String> params) {
        Iterator var3;
        if(params != null) {
            StringBuilder builder = (new StringBuilder(url)).append('?');
            var3 = params.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry)var3.next();
                builder.append((String)e.getKey()).append('=').append((String)e.getValue()).append('&');
            }

            url = builder.toString();
        }

        HttpGet get = new HttpGet(url);
        var3 = null;

        String response;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(get).getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception var8) {
            throw new RuntimeException("error post data to " + url, var8);
        } finally {
            get.releaseConnection();
        }

        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("response=" + response);
        }

        return response;
    }

    public static String get(String url, Map<String, String> params, Map<String, String> headers) {
        if (params != null) {
            StringBuilder builder = new StringBuilder(url).append('?');
            for (Map.Entry<String, String> e : params.entrySet()) {
                builder.append(e.getKey()).append('=').append(e.getValue()).append('&');
            }
            url = builder.toString();
        }

        HttpGet get = new HttpGet(url);
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                get.addHeader(e.getKey(), e.getValue());
            }
        }

        String response = null;
        try {
            HttpEntity entity = HTTP_CLIENT.execute(get).getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("error post data to " + url, e);
        } finally {
            get.releaseConnection();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("response=" + response);
        }
        return response;
    }

    public static Header buildHttpHeader(String appid, String passWord) {
        // 构建认证信息的header
        Header header = null;
        try {
            header = new BasicHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", appid, passWord).getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("class=BaseHttpUtil||method=buildHttpHeader||appid={}||passWord={}||errMsg=encoding error",
                    appid, passWord, e);
        }

        return header;
    }

    static {
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);
        HTTP_CLIENT = new DefaultHttpClient(connectionManager);
        HTTP_CLIENT.getParams().setParameter("http.connection.timeout", Integer.valueOf(20000));
        HTTP_CLIENT.getParams().setParameter("http.socket.timeout", Integer.valueOf(20000));
    }
}
