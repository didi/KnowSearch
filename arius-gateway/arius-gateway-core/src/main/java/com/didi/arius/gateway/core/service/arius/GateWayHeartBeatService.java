package com.didi.arius.gateway.core.service.arius;

/**
 * 心跳类，定期给admin发送心跳
 */
public interface GateWayHeartBeatService{
	/**
	 * 给admin发送心跳
	 * 从admin获取存活的节点数量，节点数量跟当前保存的节点数量不一致时，更新dsl限流值
	 */
	void resetHeartBeatInfo();
}
