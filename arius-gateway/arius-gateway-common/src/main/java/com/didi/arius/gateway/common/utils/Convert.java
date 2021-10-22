package com.didi.arius.gateway.common.utils;


import com.didi.arius.gateway.common.metadata.AuthRequest;
import com.didi.arius.gateway.common.metadata.FieldInfo;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.ESSearchRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.Base64;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author weizijun
* @date：2016年9月6日
* 
*/
public class Convert {
	protected static final Logger logger = LoggerFactory.getLogger(Convert.class);
	private static String pattern = ".*\\d\\d-*\\d\\d";
	private static Pattern r = Pattern.compile(pattern);
	
	public static AuthRequest parseAuth(String authentication) {
		AuthRequest auth = new AuthRequest();
		if (!authentication.startsWith("Basic ")) {
			return null;
		}
		
		if (authentication.length() == "Basic ".length()) {
			return null;
		}
		
		String userPasswd;
		try {
			userPasswd = new String(Base64.decode(authentication.substring("Basic ".length()).trim()));
		} catch (Throwable e) {
			return null;
		}
		int pos = userPasswd.indexOf(":");
		if (pos < 0 || pos > userPasswd.length() - 1) {
			return null;
		}
		
		String user = userPasswd.substring(0, pos);
		String pass = userPasswd.substring(pos+1, userPasswd.length());
		
		try {
			int appid = Integer.valueOf(user);
			auth.setAppid(appid);
		} catch (Throwable e) {
			return null;
		}
		
		auth.setAppsecret(pass);
		
		return auth;
	}
	
	static public String listToString(List<String> list, String conjunction)
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
			List<String> includes = new ArrayList<>(2), excludes = new ArrayList<>(2);
			if (sourceContext.isJsonPrimitive()) {
				JsonPrimitive sourceContextPost = sourceContext.getAsJsonPrimitive();
				if (sourceContextPost.isBoolean()) {
					return new FetchSourceContext(sourceContextPost.getAsBoolean());
				} else if (sourceContextPost.isString()) {
					return new FetchSourceContext(sourceContextPost.getAsString());
				}
			} else if (sourceContext.isJsonArray()) {
				includes = new ArrayList<>();
				JsonArray sourceContextPost = sourceContext.getAsJsonArray();
				for (int i = 0; i < sourceContextPost.size(); ++i) {
					includes.add(sourceContextPost.get(i).getAsString());
				}
			} else if (sourceContext.isJsonObject()) {
				JsonObject sourceContextPost = sourceContext.getAsJsonObject();
				JsonElement includesJson = sourceContextPost.get("includes");
				if (includesJson == null) {
					includesJson = sourceContextPost.get("include");
				}

				if (includesJson != null) {
					if (includesJson.isJsonArray()) {
						for (int i = 0; i < includesJson.getAsJsonArray().size(); ++i) {
							includes.add(includesJson.getAsJsonArray().get(i).getAsString());
						}
					} else if (includesJson.isJsonPrimitive()) {
						includes.add(includesJson.getAsString());
					}
				}

				JsonElement excludesJson = sourceContextPost.get("excludes");
				if (excludesJson == null) {
					excludesJson = sourceContextPost.get("exclude");
				}

				if (excludesJson != null) {
					if (excludesJson.isJsonArray()) {
						for (int i = 0; i < excludesJson.getAsJsonArray().size(); ++i) {
							excludes.add(excludesJson.getAsJsonArray().get(i).getAsString());
						}
					} else if (excludesJson.isJsonPrimitive()) {
						excludes.add(excludesJson.getAsString());
					}
				}
			}

			return new FetchSourceContext(includes.toArray(new String[includes.size()]),
			        excludes.toArray(new String[excludes.size()]));
		} catch (Throwable e) {
			return new FetchSourceContext(true);
		}
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
		
		return null;
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
		    
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		    	ip = request.getHeader("X-Forwarded-For");
		    }
		    
		    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
		    	ip = request.getRemoteAddr();
		    }

		    return ip;
		}
	}

	public static String getClientIP(RestRequest request) {
		// kibana不能用X-Forwarded-For，而是直接用kibana部署的服务器ip
		if (request.header("kbn-version") != null) {
			InetSocketAddress address = (InetSocketAddress) request.getRemoteAddress();
			return address.getAddress().getHostAddress();
		} else {
			String ip = request.header("X-Real-IP");

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.header("X-Forwarded-For");
			}

			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				InetSocketAddress address = (InetSocketAddress) request.getRemoteAddress();
				ip = address.getAddress().getHostAddress();
			}

			return ip;
		}
	}
	
	public static void convertIndices(SearchRequest searchRequest) {
		String[] indices = searchRequest.indices();
		if (indices == null) {
			return ;
		}

		boolean changed = false;
		String[] newIndices = new String[indices.length];

		for (int i = 0; i < indices.length; ++i) {
			String index = indices[i];

			if (index == null || index.length() < 5) {
				newIndices[i] = index;
				continue;
			}

			String suffix = index.substring(index.length() - 5, index.length());

			Matcher m = r.matcher(suffix);
			if (m.matches()) {
				String newIndex = index + "*";
				newIndices[i] = newIndex;
				changed = true;
			} else {
				newIndices[i] = index;
			}
		}

		if (changed) {
			if (logger.isDebugEnabled()) {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < newIndices.length; ++i) {
					buffer.append(newIndices[i]);
					buffer.append(",");
				}

				logger.debug("convertIndices||newIndices={}", buffer);
			}
			searchRequest.indices(newIndices);
		}
	}

	public static void convertIndices(ESSearchRequest esSearchRequest) {
    	esSearchRequest.indices(convertIndices(esSearchRequest.indices()));
	}

	public static String[] convertIndices(String[] indices) {
		if (indices == null) {
			return indices;
		}
		
		boolean changed = false;
		String[] newIndices = new String[indices.length];

		for (int i = 0; i < indices.length; ++i) {
			String index = indices[i];
			
			if (index == null || index.length() < 5) {
				newIndices[i] = index;
				continue;
			}
			
			String suffix = index.substring(index.length() - 5, index.length());

			Matcher m = r.matcher(suffix);
			if (m.matches()) {
				String newIndex = index + "*";
				newIndices[i] = newIndex;
				changed = true;
			} else {
				newIndices[i] = index;
			}
		}
		
		if (changed) {
			if (logger.isDebugEnabled()) {
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < newIndices.length; ++i) {
					buffer.append(newIndices[i]);
					buffer.append(",");
				}
				
				logger.debug("convertIndices||newIndices={}", buffer);
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
