package com.didi.cloud.fastdump.common.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.elasticsearch.rest.RestRequest;

public class HostUtil {

	private static final String UN_KNOW = "unknown";

	private HostUtil(){}
	
	public static String getHostName() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			return "localhost";
		}

		return addr.getHostName();
	}

	public static String getMyIP() throws IOException {
		String url = "http://ip.chinaz.com/getip.aspx";
		try (InputStream is = new URL(url).openStream()) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			String msg = sb.toString();
			String[] msgs = msg.split(",");
			String[] ips = msgs[0].split(":");
			return ips[1].substring(1, ips[1].length() - 1);
		}
	}

	public static String getClientIP(RestRequest request) {
		// kibana不能用X-Forwarded-For，而是直接用kibana部署的服务器ip
		if (request.header("kbn-version") != null) {
			InetSocketAddress address = (InetSocketAddress) request.getRemoteAddress();
			return address.getAddress().getHostAddress();
		} else {
			String ip = request.header("X-Real-IP");

			if (ipExist(ip)) {
				ip = request.header("X-Forwarded-For");
			}

			if (ipExist(ip)) {
				InetSocketAddress address = (InetSocketAddress) request.getRemoteAddress();
				ip = address.getAddress().getHostAddress();
			}

			return ip;
		}
	}

	private static boolean ipExist(String ip) {
		return ip == null || ip.length() == 0 || UN_KNOW.equalsIgnoreCase(ip);
	}

}
