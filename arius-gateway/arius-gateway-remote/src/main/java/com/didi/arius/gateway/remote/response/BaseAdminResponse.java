package com.didi.arius.gateway.remote.response;

import lombok.Data;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
@Data
public class BaseAdminResponse {
	protected int code;
	protected String message;
	private String version;
}
