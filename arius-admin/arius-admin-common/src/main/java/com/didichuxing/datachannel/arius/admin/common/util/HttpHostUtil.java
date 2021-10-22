package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.tunnel.util.log.util.HostUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

import java.util.ArrayList;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/8/30 上午10:56
 * @Modified By
 */
public class HttpHostUtil {

    public static final String HOST_NAME   = HostUtil.getHostName();

    /**
     * 根据ip地址和端口获取HttpHost
     *
     * @param address
     * @param port
     * @return
     */
    public static HttpHost[] getHttpHostArray(String address, int port) {
        ArrayList<HttpHost> hosts = Lists.newArrayList();
        for (String host : StringUtils.splitByWholeSeparatorPreserveAllTokens(address, COMMA)) {
            hosts.add(new HttpHost(host, port));
        }
        HttpHost[] hostArr = new HttpHost[hosts.size()];
        hostArr = hosts.toArray(hostArr);
        return hostArr;
    }

    /**
     * 根据ip地址和端口获取HttpHost
     *
     * @param address
     * @return
     */
    public static HttpHost[] getHttpHostArray(String address) {
        ArrayList<HttpHost> hosts = Lists.newArrayList();

        if (StringUtils.isBlank(address)) {
            return null;
        }

        String[] httpAddressArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(address, COMMA);
        for (String hostAndPort : httpAddressArray) {
            String[] httpAddressAndPortArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(hostAndPort, ":");
            if (httpAddressAndPortArray != null && httpAddressAndPortArray.length == 2) {
                hosts.add(new HttpHost(httpAddressAndPortArray[0], Integer.valueOf(httpAddressAndPortArray[1])));
            }
        }
        HttpHost[] hostArr = new HttpHost[hosts.size()];
        hostArr = hosts.toArray(hostArr);
        return hostArr;
    }

    /**
     * 获取HttpHost和url base
     *
     * @param url  http://100.69.238.11:8000/arius-admin/admin
     * @return
     */
    public static Tuple<HttpHost, String> getHttpHostAndBaseUrl(String url) {
        int port = 8080;
        String httpHost = "";
        String baseUrl = "";
        // 构建查询客户端
        int portStartIndex = url.lastIndexOf(":");
        if (portStartIndex > 0) {
            httpHost = url.substring(0, portStartIndex).replace("http://", "");
            int portEndIndex = url.substring(portStartIndex).indexOf("/");
            if (portEndIndex > 0) {
                baseUrl = url.substring(portStartIndex + portEndIndex);
                port = Integer.valueOf(url.substring(portStartIndex + 1, portStartIndex + portEndIndex));
            }
        }

        return new Tuple<>(new HttpHost(httpHost, port), baseUrl);
    }

    public static String getPortFromTransportAddress(String transportAddress){
        if(StringUtils.isBlank(transportAddress)){return "";}

        String[] strTemp = transportAddress.split(":");
        if (null == strTemp || 2 != strTemp.length){return "";}

        return strTemp[1];
    }

    public static String getIpFromTransportAddress(String transportAddress){
        if(StringUtils.isBlank(transportAddress)){return "";}

        String[] strTemp = transportAddress.split(":");
        if (null == strTemp || 2 != strTemp.length){return "";}

        return strTemp[0];
    }

}
