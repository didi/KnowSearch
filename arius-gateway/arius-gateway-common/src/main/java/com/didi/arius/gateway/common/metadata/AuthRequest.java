package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
@Data
@NoArgsConstructor
public class AuthRequest {
	private int appid;
	private String appsecret;

}
