package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @author weizijun
* @date：2016年8月18日
* 
*/
@Data
@NoArgsConstructor
public class AppListResponse extends BaseAdminResponse {
	private List<AppDetailResponse> data;

}
