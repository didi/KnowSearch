package com.didi.arius.gateway.elasticsearch.client;


import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.*;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.transport.TransportAddress;

import com.didi.arius.gateway.elasticsearch.client.model.*;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class ESClient extends ESAbstractClient {
    protected static final ILog logger = LogFactory.getLog(ESClient.class);

    private List<TransportAddress> tas = new ArrayList<>();
    private List<HttpHost> nodes = new ArrayList<>();

    private RestClient restClient;
    private List<Header> headers = new ArrayList<>();
    private String uriPrefix = null;

    public static final String DEFAULT_ES_VERSION = "2.3.3";

    private String esVersion = DEFAULT_ES_VERSION;

    private String clusterName;

    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 1000;
    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 30000;
    public static final int DEFAULT_MAX_RETRY_TIMEOUT_MILLIS = DEFAULT_SOCKET_TIMEOUT_MILLIS;
    public static final int DEFAULT_MAX_RETRY_COUNT = 3;
    public static final int DEFAULT_MAX_CONN_PER_ROUTE = 10;
    public static final int DEFAULT_MAX_CONN_TOTAL = 30;

    private int maxRetryTimeout = DEFAULT_MAX_RETRY_TIMEOUT_MILLIS;

    private int max_conn_per_router = DEFAULT_MAX_CONN_PER_ROUTE;
    private int max_conn_total = DEFAULT_MAX_CONN_TOTAL;

    private int connect_timeout_millis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private int socket_timeout_millis = DEFAULT_SOCKET_TIMEOUT_MILLIS;

    private final AtomicReference<Boolean> running;

    public ESClient(String clusterName, String version) {
        this();
        this.clusterName = clusterName;
        this.esVersion = version;
    }

    public ESClient() {
        running = new AtomicReference<>(false);

    }

    public ESClient addTransportAddress(TransportAddress transportAddress) {
        tas.add(transportAddress);
        return this;
    }

    public ESClient addTransportAddresses(TransportAddress... transportAddress) {
        for(TransportAddress ta : transportAddress) {
            addTransportAddress(ta);
        }
        return this;
    }

    public ESClient addHttpHost(String host, int port) {
        nodes.add(new HttpHost(host, port));
        return this;
    }

    @Deprecated
    public ESClient setHeader(Header header) {
        return addHeader(header);
    }

    public ESClient addHeader(Header header) {
        this.headers.add(header);
        return this;
    }

    public ESClient setHeaders(List<Header> headers) {
        this.headers = headers;
        return this;
    }

    public ESClient setUriPrefix(String prefix) {
        this.uriPrefix = prefix;
        return this;
    }

    /**
     * 设置认证信息
     * @param password 校验码
     * @return ESClient
     */
    public ESClient setBasicAuth(String password) {
        String encode = Base64.getEncoder().encodeToString(String.format("%s", password).getBytes(StandardCharsets.UTF_8));
        Header header = new BasicHeader("Authorization", "Basic " + encode);
        this.headers.add(header);
        return this;
    }

    public void start() {
        reset();
    }

    private void reset() {
        logger.info(String.format("reset client, cluster=%s", clusterName));

        for (TransportAddress ta : tas) {
            nodes.add(new HttpHost(ta.getAddress(), ta.getPort()));
        }

        HttpHost[] hostArr = new HttpHost[nodes.size()];

        restClient = RestClient.builder(nodes.toArray(hostArr))
                .setRequestConfigCallback((requestConfigBuilder) -> {
                    requestConfigBuilder.setSocketTimeout(socket_timeout_millis);
                    requestConfigBuilder.setConnectTimeout(connect_timeout_millis);
                    return requestConfigBuilder;
                })
                .setHttpClientConfigCallback((httpClientBuilder) -> {
                    httpClientBuilder.setMaxConnPerRoute(max_conn_per_router);
                    httpClientBuilder.setMaxConnTotal(max_conn_total);

                    return httpClientBuilder;
                })
                .setMaxRetryTimeoutMillis(maxRetryTimeout)
                .build();

        running.set(true);
    }

    @Override
    protected <Request extends ActionRequest,
               Response extends ActionResponse,
               RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>>
    void doExecute(Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {

        try {
            if (running.get().equals(false)) {
                throw new IllegalStateException("client not running");
            }

            ESActionRequest req = (ESActionRequest) request;
            RestRequest rr = req.buildRequest(headers);
            rr.addEndpointPrefix(uriPrefix);

            restClient.performRequestAsync(rr.buildRequest(), new ResponseListener() {
                @Override
                public void onSuccess(org.elasticsearch.client.Response response) {
                    try {
                        if (req.checkResponse(response)) {
                            RestResponse restResponse = new RestResponse(response);
                            ESActionResponse tr = req.buildResponse(restResponse);

                            listener.onResponse((Response) tr);
                        } else {
                            throw new ResponseException(response);
                        }
                    } catch (Exception e) {
                        listener.onFailure(e);
                    }

                }

                @Override
                public void onFailure(Exception e) {
                    listener.onFailure(e);
                }
            });
        } catch (Exception t) {
            listener.onFailure(t);
            //todo：zqr
            /*if (restClient != null && !restClient.isRunning()) {
                if (running.compareAndSet(true, false)) {
                    try {
                        restClient.close();
                        reset();
                    } catch (IOException e) {
                        // pass
                        logger.error("restClient stop error", e);
                    }
                }
            }*/
        }
    }


    @Override
    public void close() {
        try {
            restClient.close();
            running.set(false);
        } catch (IOException e) {
            logger.error("restClient close error", e);
        }
    }

    public String getEsVersion() {
        return esVersion;
    }

    public void setEsVersion(String esVersion) {
        this.esVersion = esVersion;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setMax_conn_per_router(int max_conn_per_router) {
        this.max_conn_per_router = max_conn_per_router;
    }

    public void setMax_conn_total(int max_conn_total) {
        this.max_conn_total = max_conn_total;
    }

    public void setConnect_timeout_millis(int connect_timeout_millis) {
        this.connect_timeout_millis = connect_timeout_millis;
    }

    public void setSocket_timeout_millis(int socket_timeout_millis) {
        this.socket_timeout_millis = socket_timeout_millis;
    }

    public List<HttpHost> getNodes() {
        return nodes;
    }

    public boolean isActualRunning() {
        try {
            Field field = restClient.getClass().getDeclaredField("client");
            field.setAccessible(true);
            CloseableHttpAsyncClient httpAsyncClient = (CloseableHttpAsyncClient) field.get(restClient);
            return httpAsyncClient.isRunning();
        } catch (Exception e) {
            logger.warn("get running status error.", e);
            return true;
        }
    }

}
