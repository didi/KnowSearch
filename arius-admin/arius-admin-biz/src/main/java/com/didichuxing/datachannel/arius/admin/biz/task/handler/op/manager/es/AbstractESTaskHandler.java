package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.JSONValidator.Type;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.AbstractOpManagerTaskHandler;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象类 es-task 处理程序
 *
 * @author shizeying
 * @date 2022/11/15
 * @since 0.3.2
 */
@NoArgsConstructor
public abstract class AbstractESTaskHandler extends AbstractOpManagerTaskHandler {
		
		protected final ILog              LOGGER = LogFactory.getLog(this.getClass());
		@Autowired
		protected       ClusterPhyManager clusterPhyManager;
		
		/**
		 * > 检查配置文件中是否存在配置端口
		 * <blockquote><pre>
		 * http.port: 8080
		 * http:
		 *      port: 8080
		 * </pre></blockquote>
		 *
		 * @param fileConfig 配置文件内容
		 * @return 布尔值
		 */
		@Override
		protected boolean checkPort(String fileConfig) {
				final JSONValidator from = JSONValidator.from(fileConfig);
				// 如果传入的 json 是不正确的
				if (!(from.validate() && from.getType().equals(Type.Object))) {
						return true;
				}
				JSONObject jsonObject = JSON.parseObject(fileConfig);
				return jsonObject.values().stream().map(String::valueOf)
						.noneMatch(i -> i.matches("http.port:\\s*\\d*") ||
								i.matches("http:\\\\n\\s*port:\\s*\\d*"));
		}
		
}