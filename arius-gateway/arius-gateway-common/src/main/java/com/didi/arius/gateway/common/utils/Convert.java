package com.didi.arius.gateway.common.utils;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.Base64;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.didi.arius.gateway.common.metadata.AuthRequest;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
* @author weizijun
* @date：2016年9月6日
* 
*/
public class Convert {

	private Convert(){}

	protected static final Logger logger = LoggerFactory.getLogger(Convert.class);
	private static String pattern = ".*\\d\\d-*\\d\\d";
	private static Pattern r = Pattern.compile(pattern);
	private static final String BASIC = "Basic ";
	private static final String UN_KNOW = "unknown";
	
	public static AuthRequest parseAuth(String authentication) {
		AuthRequest auth = new AuthRequest();
		if (!authentication.startsWith(BASIC)) {
			return null;
		}
		
		if (authentication.length() == BASIC.length()) {
			return null;
		}
		
		String userPasswd;
		try {
			userPasswd = new String(Base64.decode(authentication.substring(BASIC.length()).trim()));
		} catch (Exception e) {
			return null;
		}
		int pos = userPasswd.indexOf(":");
		if (pos < 0 || pos > userPasswd.length() - 1) {
			return null;
		}
		
		String user = userPasswd.substring(0, pos);
		String pass = userPasswd.substring(pos+1, userPasswd.length());
		
		try {
			int appid = Integer.parseInt(user);
			auth.setAppid(appid);
		} catch (Exception e) {
			return null;
		}
		
		auth.setAppsecret(pass);
		
		return auth;
	}

	public static String listToString(List<String> list, String conjunction)
	{
	   StringBuilder sb = new StringBuilder();
	   boolean first = true;
	   for (String item : list)
	   {
	      if (first)
	         first = false;
	      else
	         sb.append(conjunction);
	      sb.append(item);
	   }
	   return sb.toString();
	}
	
	public static FetchSourceContext parseFetchSourceContext(JsonElement sourceContext) {
		try {
			List<String> includes = new ArrayList<>(2);
			List<String> excludes = new ArrayList<>(2);
			if (sourceContext.isJsonPrimitive()) {
				JsonPrimitive sourceContextPost = sourceContext.getAsJsonPrimitive();
				FetchSourceContext fetchContext = getFetchSourceContext(sourceContextPost);
				if (fetchContext != null) return fetchContext;
			} else if (sourceContext.isJsonArray()) {
				includes = new ArrayList<>();
				addInclude(sourceContext, includes);
			} else if (sourceContext.isJsonObject()) {
				addIncludesAndExcludes(sourceContext, includes, excludes);
			}

			return new FetchSourceContext(includes.toArray(new String[includes.size()]),
			        excludes.toArray(new String[excludes.size()]));
		} catch (Exception e) {
			return new FetchSourceContext(true);
		}
	}

	private static void addIncludesAndExcludes(JsonElement sourceContext, List<String> includes, List<String> excludes) {
		JsonObject sourceContextPost = sourceContext.getAsJsonObject();
		JsonElement includesJson = sourceContextPost.get("includes");
		if (includesJson == null) {
			includesJson = sourceContextPost.get("include");
		}

		if (includesJson != null) {
			dealJson(includes, includesJson);
		}

		JsonElement excludesJson = sourceContextPost.get("excludes");
		if (excludesJson == null) {
			excludesJson = sourceContextPost.get("exclude");
		}

		if (excludesJson != null) {
			dealJson(excludes, excludesJson);
		}
	}

	private static void dealJson(List<String> includesOrExcludes, JsonElement includesOrExcludesJson) {
		if (includesOrExcludesJson.isJsonArray()) {
			for (int i = 0; i < includesOrExcludesJson.getAsJsonArray().size(); ++i) {
				includesOrExcludes.add(includesOrExcludesJson.getAsJsonArray().get(i).getAsString());
			}
		} else if (includesOrExcludesJson.isJsonPrimitive()) {
			includesOrExcludes.add(includesOrExcludesJson.getAsString());
		}
	}

	private static void addInclude(JsonElement sourceContext, List<String> includes) {
		JsonArray sourceContextPost = sourceContext.getAsJsonArray();
		for (int i = 0; i < sourceContextPost.size(); ++i) {
			includes.add(sourceContextPost.get(i).getAsString());
		}
	}

	private static FetchSourceContext getFetchSourceContext(JsonPrimitive sourceContextPost) {
		if (sourceContextPost.isBoolean()) {
			return new FetchSourceContext(sourceContextPost.getAsBoolean());
		} else if (sourceContextPost.isString()) {
			return new FetchSourceContext(sourceContextPost.getAsString());
		}
		return null;
	}

	public static String[] parseFields(JsonElement fields) {
		if (fields.isJsonArray()) {
			JsonArray fieldsPost = fields.getAsJsonArray();
			String[] strFields = new String[fieldsPost.size()];
			for (int i = 0 ; i < fieldsPost.size(); ++i) {
				strFields[i] = fieldsPost.get(i).getAsString();
			}
			
			return strFields;
		} else if (fields.isJsonPrimitive()) {
			JsonPrimitive fieldsPost = fields.getAsJsonPrimitive();
			if (fieldsPost.isString()) {
				return new String[]{fieldsPost.getAsString()};
			}
		}
		
		return new String[]{};
	}
	
	public static String getPrefix(String str) {
		return str.length() > 4096 ? str.substring(0, 4095) + "..." : str;
	}
	
    /** 
     *  
     * @功能说明:在日志文件中，打印异常堆栈 
     * @return:String
     */  
    public static String logExceptionStack(Throwable e) {  
        StringWriter errorsWriter = new StringWriter();  
        e.printStackTrace(new PrintWriter(errorsWriter));  
        return getPrefix(errorsWriter.toString());
    }  	
	
	public static String getClientIP(HttpServletRequest request) {
		// kibana不能用X-Forwarded-For，而是直接用kibana部署的服务器ip
		if (request.getHeader("kbn-version") != null) {
			return request.getRemoteAddr();
		} else {
			String ip = request.getHeader("X-Real-IP");
		    
		    if (ipExist(ip)) {
		    	ip = request.getHeader("X-Forwarded-For");
		    }
		    
		    if (ipExist(ip)) {
		    	ip = request.getRemoteAddr();
		    }

		    return ip;
		}
	}

	private static boolean ipExist(String ip) {
		return ip == null || ip.length() == 0 || UN_KNOW.equalsIgnoreCase(ip);
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
	
	public static void convertIndices(SearchRequest searchRequest) {
    	searchRequest.indices(convertIndices(searchRequest.indices()));
	}

	public static void convertIndices(ESSearchRequest esSearchRequest) {
    	esSearchRequest.indices(convertIndices(esSearchRequest.indices()));
	}

	/**
	 * 将具体索引带上*，支持访问带版本索引数据
	 *
	 * @param indices 索引
	 * @return {@link String[]}
	 */
	public static String[] convertIndices(String[] indices) {
		if (indices == null) {
			return indices;
		}

		boolean changed = false;
		String[] newIndices = new String[indices.length];

		for (int i = 0; i < indices.length; ++i) {
			String index = indices[i];
            if (StringUtils.isNotBlank(index) && !index.endsWith("*")) {
				String newIndex = index + "*";
				newIndices[i] = newIndex;
				changed = true;
			} else {
				newIndices[i] = index;
			}
		}

		if (changed) {
			if (logger.isDebugEnabled()) {
				logger.debug("convertIndices||newIndices={}", StringUtils.join(newIndices, ","));
			}
			return newIndices;
		} else {
			return indices;
		}
	}
	
	public static FieldInfo fieldInfoMerge(FieldInfo oldFieldInfo, FieldInfo newFieldInfo) {
		if (newFieldInfo.getCardinality() > oldFieldInfo.getCardinality()) {
			return newFieldInfo;
		} else {
			return oldFieldInfo;
		}
	}
	
	public static String getHostName() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			return "localhost";
		}
		String hostName = addr.getHostName();
        if (hostName != null) {
            hostName = hostName.replace(".diditaxi.com", "");
        } else {
            hostName = "localhost";
        }
        
        return hostName;  
	}

}
