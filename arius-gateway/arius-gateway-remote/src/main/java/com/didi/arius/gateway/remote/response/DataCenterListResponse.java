package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @author weizijun
* @date：2016年10月31日
* 
*/
@Data
@NoArgsConstructor
public class DataCenterListResponse extends BaseAdminResponse {
	private List<DataCenterResponse> data;
}
