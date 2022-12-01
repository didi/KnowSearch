package com.didi.cloud.fastdump.common.client.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didi.cloud.fastdump.common.bean.es.ShardInfo;
import com.didi.cloud.fastdump.common.client.Client;
import com.didi.cloud.fastdump.common.content.ResultType;
import com.didi.cloud.fastdump.common.exception.BaseException;
import com.didi.cloud.fastdump.common.exception.FastDumpOperateException;
import com.didi.cloud.fastdump.common.exception.NotFindESClientException;
import com.didi.cloud.fastdump.common.utils.ListUtils;
import com.didi.cloud.fastdump.common.utils.RetryUtil;

public class ESRestClient implements Client<RestClient> {
    protected static final Logger LOGGER         = LoggerFactory.getLogger(ESRestClient.class);

    private RestClient            client;

    /**
     * es集群连接地址：ip:http-port,ip:http-port,ip:http-port
     */
    private final String          clusterAddress;

    private final String          username;

    private final String          password;

    protected long                connectTimeout = 2 * 60000;

    protected long                socketTimeout  = 2 * 60000;

    public ESRestClient(String clusterAddress, String username, String password) {
        this.clusterAddress = clusterAddress;
        this.username = username;
        this.password = password;
    }

    @Override
    public synchronized RestClient getClient() {
        if (client != null) { return client;}

        try {
            List<HttpHost> httpHosts = new ArrayList<>();
            List<String> clusterAddressList = ListUtils.string2StrList(clusterAddress);
            if (CollectionUtils.isEmpty(clusterAddressList)) {
                throw new IllegalArgumentException(String.format("ES集群地址[%s]为空", clusterAddress));
            }

            for (String clusterAddress : clusterAddressList) {
                String[] split = clusterAddress.split(":");
                String host = split[0];
                int port = Integer.parseInt(split[1]);
                httpHosts.add(new HttpHost(host, port, null));
            }

            RestClientBuilder restClient = RestClient.builder(httpHosts.toArray(new HttpHost[] {}))
                    .setRequestConfigCallback(c -> {
                        c.setConnectTimeout(Math.toIntExact(connectTimeout));
                        c.setSocketTimeout(Math.toIntExact(socketTimeout));
                        return c;
                    }).setHttpClientConfigCallback(c -> {
                        if (null != username && null != password) {
                            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
                            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                            credentialsProvider.setCredentials(AuthScope.ANY, creds);
                            c.setDefaultCredentialsProvider(credentialsProvider);
                        }
                        return c;
                    });
            client = restClient.build();
        } catch (IllegalArgumentException e) {
            LOGGER.error("class=ESRestClient||method=getClient||clusterAddress={}||errMsg={}", clusterAddress, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return client;
    }

    /**
     * 获取shard 信息
     * @param index   index1,index2
     * @return          List<ShardInfo>
     */
    public List<ShardInfo> syncRetryGetShardInfoList(String index,
                                                     int  retryTime,
                                                     long intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryGetShardInfoList",
                retryTime,
                intervalMills,
                () -> {
                    String responseContent = preformRequest("GET", "/_cat/shards/" + index + "?h=index,shard,prirep,id,ip,state");
                    List<ShardInfo> shardInfos = new ArrayList<>();
                    parseContent(responseContent, shardInfo -> {
                        ShardInfo info = new ShardInfo();
                        info.setIndex(shardInfo[0]);
                        info.setShard(shardInfo[1]);
                        info.setPrirep(shardInfo[2]);
                        info.setNodeId(shardInfo[3]);
                        info.setIp(shardInfo[4]);
                        info.setState(shardInfo[5]);
                        shardInfos.add(info);
                    });
                    return shardInfos;
                });
    }

    public String syncRetryGetClusterVersion(int  retryTime,
                                             long intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryGetClusterVersion",
                retryTime,
                intervalMills,
                () -> JSON.parseObject(preformRequest("GET", "/")).getJSONObject("version").getString("number"));
    }

    public String syncRetryGetClusterName(int  retryTime,
                                          long intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryGetClusterName",
                retryTime,
                intervalMills,
                () -> JSON.parseObject(preformRequest("GET", "/")).getString("cluster_name"));
    }

    public String syncRetryBulkWrite(String content) throws IOException {
        if (StringUtils.isBlank(content)) { return null;}

        HttpEntity entity    = new NStringEntity(content, ContentType.APPLICATION_JSON);
        RestClient client    = this.getClient();
        Request    request   = new Request("post", "/_bulk");
        request.setEntity(entity);
        Response response = client.performRequest(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * 获取index 信息
     * @param index   index1,index2
     * @return
     */
    public String syncRetryGetIndexUuid(String index,
                                        int    retryTime,
                                        long   intervalMills){
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryGetIndexUuid",
                retryTime,
                intervalMills,
                () -> JSON.parseObject(preformRequest( "GET", "/" + index + "/_settings")).getJSONObject(index).
                        getJSONObject("settings").getJSONObject("index").getString("uuid"));
    }

    public JSONObject syncRetryGetNodeStateJsonObj(int    retryTime,
                                                   long   intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryGetNodeStateJsonObj",
                retryTime,
                intervalMills,
                () -> JSON.parseObject(preformRequest("GET", "/_nodes/stats/fs")).getJSONObject("nodes")
        );
    }

    public List<String> syncRetryGetIndexTypeList(String index,
                                                  int    retryTime,
                                                  long   intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryGetIndexTypeList",
                retryTime,
                intervalMills,
                ()-> {
                    String responseContent = preformRequest("GET", "/" + index + "/_mapping");
                    Object[] mappings = JSON.parseObject(responseContent).getJSONObject(index).getJSONObject("mappings").keySet().toArray();

                    List<String> list = new ArrayList<>();
                    for (Object mapping : mappings) {
                        String type = mapping.toString();
                        if (!"properties".equals(type)) { list.add(type);}
                    }
                    return list;
                });
    }

    public boolean syncRetryCheckIndexExist(String index,
                                            int    retryTime,
                                            long   intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryCheckIndexExist",
                retryTime,
                intervalMills,
                ()->{
                    RestClient client = this.getClient();
                    if (null == client) {
                        throw new NotFindESClientException(String.format("获取ES集群[%s]Client失败, 请确认集群是否正常", clusterAddress));
                    }

                    Response nodeResponse;
                    try {
                        Request request = new Request("HEAD", index);
                        nodeResponse    = client.performRequest(request);
                    } catch (IOException e) {
                        throw new BaseException(String.format("请求ES集群[%s]失败, 请确认集群是否正常", clusterAddress), ResultType.FAIL);
                    }
                    return nodeResponse.getStatusLine().toString().contains("200");
                }
        );
    }

    public boolean syncRetryCheckTemplateExist(String template,
                                               int    retryTime,
                                               long   intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncRetryCheckTemplateExist",
                retryTime,
                intervalMills,
                () -> {
                    RestClient client = this.getClient();
                    if (null == client) {
                        throw new NotFindESClientException(String.format("获取ES集群[%s]Client失败, 请确认集群是否正常", clusterAddress));
                    }

                    Response nodeResponse;
                    try {
                        Request request = new Request("HEAD", "/_template/" + template);
                        nodeResponse = client.performRequest(request);
                    } catch (IOException e) {
                        throw new BaseException(String.format("请求ES集群[%s]失败, 请确认集群是否正常", clusterAddress), ResultType.FAIL);
                    }

                    return nodeResponse.getStatusLine().toString().contains("200");
                }
        );
    }

    public List<String> syncRetryGetIndicesByTemplate(String template,
                                                      int    retryTime,
                                                      long   intervalMills) {
        return new ArrayList<>(syncRetryGetIndicesHealthMap(template + "*", retryTime, intervalMills).keySet());
    }

    public Map<String/*indexName*/, String/*health red/green/yellow*/> syncRetryGetIndicesHealthMap(String indices,
                                                                                                    int    retryTime,
                                                                                                    long   intervalMills) {
        return RetryUtil.retryWhenNullOrExceptionAndFailedThrowRuntimeException(
                "syncGetIndicesHealthMap",
                retryTime,
                intervalMills,
                () -> {
                    String responseContent = preformRequest("GET", "_cat/indices/" + indices + "?&h=index,health");
                    Map<String, String> index2HealthMap = new HashMap<>();
                    parseContent(responseContent, healthInfo -> {
                        index2HealthMap.put(healthInfo[0], healthInfo[1]);
                    });
                    return index2HealthMap;
                }
        );
    }

    /**
     * flush 落盘segment
     * @param indices
     * @throws Exception
     */
    public void syncRetryFlushIndices(String indices,
                                      int    retryTime,
                                      long   intervalMills) {
        RetryUtil.retryWhenExceptionAndFailedThrowRuntimeException(
                "syncRetryFlushIndices",
                retryTime,
                intervalMills,
                () -> {
                    String content = preformRequest("POST", "/" + indices + "/_flush");
                    if (0 < JSON.parseObject(content).getJSONObject("_shards").getInteger("failed")) {
                        throw new FastDumpOperateException("flush failed " + content);
                    }
                    return null;
                }
        );
    }

    private String preformRequest(String method, String endpoint) throws NotFindESClientException, IOException {
        RestClient client = this.getClient();
        if (null == client) {
            throw new NotFindESClientException(String.format("获取ES集群[%s]Client失败, 请确认集群是否正常", clusterAddress));
        }

        Response nodeResponse;
        try {
            nodeResponse = client.performRequest(new Request(method, endpoint));
        } catch (IOException e) {
            throw new NotFindESClientException(String.format("请求ES集群[%s]失败, 请确认集群是否正常", clusterAddress));
        }
        return EntityUtils.toString(nodeResponse.getEntity());
    }

    private static void parseContent(String content, Consumer<String[]> consumer) {
        for (String s : content.split("\n")) {
            // 过滤未分配shard
            if (s.contains("UNASSIGNED")) {
                continue;
            }
            if (!"".equals(s)) {
                String[] indexInfoStr = s.split("\\s+");
                consumer.accept(indexInfoStr);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (null != client) {
            client.close();
        }
    }

    public static void main(String[] args) {
        ESRestClient esRestClient = new ESRestClient("127.0.0.1:9200", null, null);
        String s = esRestClient.syncRetryGetIndexUuid("fast-dump-es-7.6.0-7.6.0_2022-09-14", 3, 3000);
        List<String> strings = esRestClient.syncRetryGetIndexTypeList("fast-dump-es-7.6.0-7.6.0_2022-09-14", 3, 3000);
        esRestClient.syncRetryCheckTemplateExist("fast-dump-es-7.6.0-7.6.0",3 ,3000);
        System.out.println();
    }
}
