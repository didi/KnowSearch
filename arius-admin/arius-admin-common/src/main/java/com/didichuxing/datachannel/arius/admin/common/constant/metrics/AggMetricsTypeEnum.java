package com.didichuxing.datachannel.arius.admin.common.constant.metrics;

/**
 * Created by linyunan on 2021-08-04
 */
public enum AggMetricsTypeEnum {
	/*** 未知*/
	UNKNOWN(-1, ""),
	MAX(1, "max"),
	MIN(2, "min"),
	AVG(3, "avg"),
	SUM(4, "sum");

	AggMetricsTypeEnum(int code, String type) {
		this.code = code;
		this.type = type;
	}

	private int    code;

	private String type;

	public int getCode() {
		return code;
	}

	public String getType() {
		return type;
	}

	/**
	 * 默认code == null 选max
	 */
	public static String valueTypeOfCode(Integer code) {
		if (null == code) {
			return AggMetricsTypeEnum.MAX.getType();
		}
		for (AggMetricsTypeEnum typeEnum : AggMetricsTypeEnum.values()) {
			if (code.equals(typeEnum.getCode())) {
				return typeEnum.getType();
			}
		}

		return null;
	}

	public static String valueCodeOfType(String type) {
		if (null == type) {
			return AggMetricsTypeEnum.MAX.getType();
		}
		for (AggMetricsTypeEnum typeEnum : AggMetricsTypeEnum.values()) {
			if (type.equals(typeEnum.getType())) {
				return typeEnum.getType();
			}
		}

		return null;
	}
}
