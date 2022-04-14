package com.didi.arius.gateway.common.consts;

import java.util.HashSet;
import java.util.Set;

/**
* @author weizijun
* @date：2016年8月26日
* 
*/
public class RestConsts {

	private RestConsts(){}

	public static final String ATTR_REQUEST_PARAMS = "requestParams";
	public static final String SEARCH_DATE_FROM_PARAMS = "time_from";
	public static final String SEARCH_DATE_TO_PARAMS = "time_to";
	public static final String SOCKET_TIMEOUT_PARAMS = "socket_timeout";
	public static final String DSL_MD5_PARAMS = "dslMd5";

	public static final String GATEWAY_VERSION = "2.5.6";

	public static final String INDEX = "index";
	public static final String STRING_NAME = "string";

	public static final String SCROLL_SPLIT = "!";

	public static final String FIELDS = "fields";

	public static final String SCROLL = "scroll";

	public static final Set<String> DEFAULT_WRITE_ACTION = new HashSet<String>(){{
		add("RestBulkAction");
		add("RestDeleteAction");
		add("RestIndexAction");
		add("RestUpdateAction");
	}};

	public static final String NULL_ACTION = "null";
}
