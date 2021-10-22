package com.didi.arius.gateway.rest.http;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

/**
* @author weizijun
* @date：2016年8月16日
* 
*/
public interface IRestHandler {
	public void dispatchRequest(RestRequest request, RestChannel channel);
}
