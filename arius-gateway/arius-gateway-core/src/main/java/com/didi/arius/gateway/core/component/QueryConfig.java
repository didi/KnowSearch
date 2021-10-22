package com.didi.arius.gateway.core.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.Semaphore;

/**
* @author fitz
* @date：2021年5月31日
* 
*/
@Configuration
@Getter
public class QueryConfig {

	@Value("${gateway.queryConfig.searchSlowlogThresholdMills}")
	private long searchSlowlogThresholdMills;
	
	@Value("${gateway.queryConfig.requestSlowlogThresholdMills}")
	private long requestSlowlogThresholdMills;
	
	@Value("${gateway.queryConfig.dslMaxLength}")
	private int dslMaxLength;

	@Value("${gateway.queryConfig.tcpQueryLimit}")
	private int tcpQueryLimit;

	@Value("${gateway.queryConfig.httpQueryLimit}")
	private int httpQueryLimit;

	@Value("${gateway.queryConfig.kibanaSearchUri}")
	private String kibanaSearchUri;

	@Value("${gateway.queryConfig.maxAggsBuckets}")
	private int maxAggsBuckets;
	
	@Value("${gateway.queryConfig.maxAggsMemUsed}")
	private long maxAggsMemUsed;

	@Value("${gateway.queryConfig.connectESTime}")
	private String connectESTime;
	
	@Value("${gateway.queryConfig.clientWorkerCount}")
	private String clientWorkerCount;

	@Value("${gateway.queryConfig.fetchTimeout}")
	private long fetchTimeout;
	
	@Value("${gateway.queryConfig.dslQPSLimit}")
	private double dslQPSLimit;
	
	@Value("${gateway.queryConfig.maxHttpResponseLength}")
	private int maxHttpResponseLength;

	@Value("${gateway.queryConfig.checkForbidden}")
	private boolean checkForbidden;

	@Value("${gateway.queryConfig.esSocketTimeout}")
	private int esSocketTimeout;

	@Value("${elasticsearch.admin.cluster.name}")
	private String adminClusterName;

	@Value("${gateway.queryConfig.runMode}")
	private String runMode;

	@Value("${gateway.queryConfig.maxByteIn}")
	private long maxByteIn;

	private Semaphore httpSemaphore;

	private Semaphore tcpSemaphore;

	@PostConstruct
	public void init() {
		httpSemaphore = new Semaphore(httpQueryLimit);

		tcpSemaphore = new Semaphore(tcpQueryLimit);
	}
}
