package com.didi.arius.gateway.common.metadata;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.enums.AggsTypeEnum;
import lombok.Data;

/**
 * 聚合查询bucket信息
 */
@Data
public class AggsBukcetInfo {

	/**
	 * bucket的数量
	 */
	private int bucketNumber;

	/**
	 * bucket作为最后一层的bucket数量
	 */
	private int lastBucketNumber;

	/**
	 * bucket使用的内存数量
	 */
	private long memUsed;

	/**
	 * 是否是最后一层
	 */
	private boolean isLastBucket;

	/**
	 * aggs类型
	 */
	private AggsTypeEnum bucketType;
	
	public AggsBukcetInfo() {
		isLastBucket = true;
		bucketType = AggsTypeEnum.BUCKET;
	}
	
	public static AggsBukcetInfo createSingleBucket() {
		AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
		aggsBukcetInfo.setBucketNumber(1);
		aggsBukcetInfo.setLastBucketNumber(1);
		aggsBukcetInfo.setMemUsed(QueryConsts.AGGS_BUCKET_MEM_UNIT);
		aggsBukcetInfo.setLastBucket(true);
		aggsBukcetInfo.setBucketType(AggsTypeEnum.BUCKET);
		
		return aggsBukcetInfo;
	}
	
	public static AggsBukcetInfo createSingleMetrics() {
		AggsBukcetInfo aggsBukcetInfo = new AggsBukcetInfo();
		aggsBukcetInfo.setBucketNumber(1);
		aggsBukcetInfo.setLastBucketNumber(1);
		aggsBukcetInfo.setMemUsed(QueryConsts.AGGS_BUCKET_MEM_UNIT);
		aggsBukcetInfo.setLastBucket(true);
		aggsBukcetInfo.setBucketType(AggsTypeEnum.METRICS);
		
		return aggsBukcetInfo;
	}

	@Override
	public String toString() {
		return "bucketNumber=" + bucketNumber + "||lastBucketNumber=" + lastBucketNumber + "||memUsed=" + memUsed;
	}
}
