package com.didi.arius.gateway.common.metadata;

import com.didi.arius.gateway.elasticsearch.client.ESClient;
import lombok.Data;
import org.elasticsearch.client.Client;


/**
* @author weizijun
* @date：2016年10月31日
* 集群信息
*/
@Data
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
	 * http client
	 */
	private ESClient esClient;

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

	public static enum Type {
		INDEX(0),
		SOURCE(1);
		
		int type;
		
		private Type(int type) {
			this.type = type;
		}

		public static Type IntegerToType(int code) {
			for (Type type : Type.values()) {
				if (type.type == code) {
					return type;
				}
			}
			return INDEX;
		}
	}
}
