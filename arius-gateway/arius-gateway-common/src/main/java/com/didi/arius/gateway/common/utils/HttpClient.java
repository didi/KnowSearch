package com.didi.arius.gateway.common.utils;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * http 客户端
 * 
 * @author moyunfeng
 *
 */
public final class HttpClient {

	private static Logger LOG = LoggerFactory.getLogger("thirdReq");
	private static CloseableHttpClient HTTPCLIENT = HttpClients.createDefault();

	private HttpClient() {

	}

	public static <T> T forward(String url, String method, String bodyform,
			Map<String, String> headerParams,
			Map<String, String[]> requestParams, Class<T> clazz)
			throws ElasticsearchException {
		LOG.info("请求地址：" + url);
		LOG.info("请求method:" + method);
		LOG.info("请求参数:" + requestParams);
		LOG.info("请求头部参数:" + headerParams);
		LOG.info("请求body:" + bodyform);

		T result = null;
		String content;
		try {
			content = forward(url, method, bodyform, headerParams,
					requestParams);
		} catch (Exception e) {
			LOG.error("调用错误", e);
//			throw new ElasticsearchException("调用地址：" + url + "发生错误，请求参数："
//					+ requestParams, e);
			content = String.format("{\"code\":-1,\"message\":\"请求失败, url=%s, params=%s, header=%s\"}", url, JSON.toJSONString(requestParams), JSON.toJSONString(headerParams));
		}
		LOG.info("请求结果：" + content);
		try {
			if (content != null && !content.isEmpty()) {
				result = JSON.parseObject(content, clazz);
			}
		} catch (Exception e) {
			result = JSON.parseObject("{\"code\":-1,\"message\":\"请求失败\"}", clazz);
		}

		return result;
	}

	public static String forward(String url, String method, String bodyform,
			Map<String, String> headerParams,
			Map<String, String[]> requestParams) throws IOException {
		if (requestParams != null && requestParams.size() > 0) {
			url += "?";
			for (Map.Entry<String, String[]> e : requestParams.entrySet()) {
				url += (e.getKey() + "=" + e.getValue()[0] + "&");
			}
			url = url.substring(0, url.length() - 1);
		}

		HttpRequestBase request = null;
		switch (method.toUpperCase()) {
		case "GET":
			request = new HttpGet(url);
			break;
		case "DELETE":
			request = new HttpDelete(url);
			break;
		case "POST":
			request = new HttpPost(url);
			break;
		case "PUT":
			request = new HttpPut(url);
			break;
		default:
			LOG.error("错误的请求method： " + method);
			return null;
		}

		request.addHeader("content-type", "application/json;charset=UTF-8");
		if (bodyform != null) {
			if (request instanceof HttpEntityEnclosingRequestBase) {
				((HttpEntityEnclosingRequestBase) request)
						.setEntity(new StringEntity(bodyform, "UTF-8"));
			}
		}

		if (headerParams != null && headerParams.size() > 0) {
			for (Map.Entry<String, String> e : headerParams.entrySet()) {
				request.setHeader(e.getKey(), e.getValue());
			}
		}

		String repons = null;
		try {
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(QueryConsts.DEFAULT_HTTP_TIMEOUT).setConnectTimeout(QueryConsts.DEFAULT_HTTP_TIMEOUT).build();//设置请求和
			request.setConfig(requestConfig);
			
			HttpResponse httpResponse = HTTPCLIENT.execute(request);
			HttpEntity httpEntity = httpResponse.getEntity();
			repons = EntityUtils.toString(httpEntity);// 取出应答字符串
		} catch (IOException e) {
			LOG.error("转发请求失败", e);
			throw e;
		}

		return repons;
	}
}
