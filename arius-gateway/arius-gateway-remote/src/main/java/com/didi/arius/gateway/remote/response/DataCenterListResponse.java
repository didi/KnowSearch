package com.didi.arius.gateway.remote.response;

import lombok.Data;

import java.util.List;

/**
* @author weizijun
* @date：2016年10月31日
* 
*/
@Data
public class DataCenterListResponse extends BaseAdminResponse {
	private List<DataCenterResponse> data;
}
