package com.didichuxing.datachannel.arius.admin.rest.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * 比较杂的工具类
 *
 * @author wangxiaoxue
 * @version $Id: URLHelper.java, v 0.1 2015年7月16日 上午10:44:10 Administrator Exp $
 */
public class URLHelper {

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return fixIp(ip);
    }

    /**
     * x-forwarded-for nginx多层ip处理
     * @param ip
     * @return
     */
    private static String fixIp(String ip) {
        String realIp = ip;
        if (StringUtils.isNotBlank(ip) && ip.contains(",")) {
            String[] ipArray = ip.split(",");
            realIp = ipArray[ipArray.length - 1];
        }
        return realIp != null ? realIp.trim() : null;
    }

    public static void main(String[] args) {
        System.out.println(fixIp("123.123.123.123,321,192.121.121.121"));
    }
}
