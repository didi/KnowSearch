package com.didi.arius.gateway.common.metadata;

import com.didi.arius.gateway.elasticsearch.client.ESClient;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.client.Client;

import java.util.Set;


/**
* @author weizijun
* @date：2016年10月31日
* 集群信息
*/
@Data
@NoArgsConstructor
public class ESCluster {
	/**
	 * 集群名称
	 */
	private String cluster;

	/**
	 * 读请求tcp地址
	 */
	private String readAddress;

	/**
	 * http请求地址
	 */
	private String httpAddress;

	/**
	 * httpWriteAddress
	 */
	private String httpWriteAddress;

	/**
	 * tcp client
	 */
	private Client client;

	/**
	 * http 读client
	 */
	private ESClient esClient;

	/**
	 * http 写client
	 */
	private ESClient esWriteClient;

	/**
	 * 集群类型，INDEX or TRIB
	 */
	private Type type;

	/**
	 * 数据中心
	 */
	private String dataCenter;

	/**
	 * es集群版本号
	 */
	private String esVersion;

	/**
	 * 集群认证信息
	 */
	private String password;

	/**
	 * 读写模式
	 */
	private int runMode;

	/**
	 * 读写分离，指定得action需要用写的action
	 */
	private Set<String> writeAction;

	public enum Type {
		INDEX(0),
		SOURCE(1);
		
		int clusterType;
		
		private Type(int clusterType) {
			this.clusterType = clusterType;
		}

		public static Type integerToType(int code) {
			for (Type type : Type.values()) {
				if (type.clusterType == code) {
					return type;
				}
			}
			return INDEX;
		}
	}
}
