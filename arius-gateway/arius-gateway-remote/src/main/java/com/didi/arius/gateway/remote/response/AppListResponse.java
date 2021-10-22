package com.didi.arius.gateway.remote.response;

import lombok.Data;

import java.util.List;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
@Data
public class AppListResponse extends BaseAdminResponse {
	private List<AppDetailResponse> data;

}
