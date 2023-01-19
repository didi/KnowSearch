package com.didiglobal.logi.op.manager.infrastructure.common.properties;

import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


/**
 * @author cjm
 */

@Data
@Validated
@ConfigurationProperties("spring.op-manager")
public class OpManagerProper {
		
		
		/**
		 * 数据库信息
		 */
		@NotEmpty(message = "配置文件配置必须要配置 [op-manager.username] 属性")
		private String username;
		
		/**
		 * 数据库信息
		 */
		@NotEmpty(message = "配置文件配置必须要配置 [op-manager.password] 属性")
		private String password;
		
		/**
		 * 数据库信息
		 */
		@NotEmpty(message = "配置文件配置必须要配置 [op-manager.url] 属性")
		private String url;
		
		/**
		 * 数据库信息
		 */
		@NotEmpty(message = "配置文件配置必须要配置 [op-manager.driver-class-name] 属性")
		private String driverClassName;
		@NotEmpty(message = "配置文件配置必须要配置 [op-manager.data-source-name] 属性")
		private String dataSourceName;
		
		/**
		 * 初始大小
		 */
		private int initialSize             = 10;
		/**
		 * 查询超时
		 */
		private int validationQueryTimeout  = 5000;
		/**
		 * 事务查询超时
		 */
		private int transactionQueryTimeout = 60000;
		
		private int     minIdle                       = 10;
		private int     maxActive                     = 30;
		private int     maxWait                       = 60000;
		private Boolean keepAlive                     = true;
		private int     timeBetweenEvictionRunsMillis = 1000;
		private int     minEvictableIdleTimeMillis    = 300000;
		private Boolean defaultAutoCommit             = true;
		private String  validationQuery;
		private Boolean testWhileIdle                 = true;
		private Boolean testOnReturn                  = false;
		private Boolean testOnBorrow                  = true;
		private Boolean logAbandoned                  = true;
		private Boolean poolPreparedStatements        = true;
		private int     maxOpenPreparedStatements     = 50;
		private String  filters;
}