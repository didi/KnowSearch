package com.didichuxing.datachannel.arius.admin.common.constant.cluster;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;

/**
 * 插件健康枚举
 *
 * @author shizeying
 * @date 2022/11/16
 * @since 0.3.2
 */
public enum PluginHealthEnum {
		/**
		 * green
		 */
		GREEN(0, "green"),
		
		/**
		 * yellow
		 */
		YELLOW(1, "yellow"),
		
		/**
		 * red
		 */
		RED(2, "red"),
		
		/**
		 * 未知
		 */
		UNKNOWN(-1, "unknown");
		
		PluginHealthEnum(Integer code, String desc) {
				this.code = code;
				this.desc = desc;
		}
		
		public static PluginHealthEnum valueOf(Integer code) {
				if (YELLOW.getCode().equals(code)) {
						return YELLOW;
				} else if (RED.getCode().equals(code)) {
						return RED;
				} else if (GREEN.getCode().equals(code)) {
						return GREEN;
				}
				return UNKNOWN;
		}
		
		public static PluginHealthEnum valuesOf(String desc) {
				if (AriusObjUtils.isBlack(desc) || UNKNOWN.getDesc().equals(desc)) {
						return UNKNOWN;
				}
				
				if (YELLOW.getDesc().equals(desc)) {
						return YELLOW;
				} else if (RED.getDesc().equals(desc)) {
						return RED;
				} else if (GREEN.getDesc().equals(desc)) {
						return GREEN;
				}
				return UNKNOWN;
		}
		
		public Integer getCode() {
				return code;
		}
		
		public String getDesc() {
				return desc;
		}
		
		private Integer code;
		private String  desc;
		
		public static boolean isExitByCode(Integer code) {
				if (null == code) {
						return false;
				}
				
				for (PluginHealthEnum value : PluginHealthEnum.values()) {
						if (code.equals(value.getCode())) {
								return true;
						}
				}
				
				return false;
		}
}