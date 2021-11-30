package com.didi.arius.gateway.remote.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @author weizijun
* @date：2017年2月13日
* 
*/
@Data
@NoArgsConstructor
public class TemplateInfoResponse {
	private int id;
	private String expression;
	private List<AliasesInfoResponse> aliases;
	private DataCenterResponse dataCluster;
	private int version;
}
