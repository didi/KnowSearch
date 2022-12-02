package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.JSONValidator.Type;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.AbstractOpManagerTaskHandler;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import java.util.Collections;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO 注意这里暂时为了完成功能先行这么做，等待后续ES各项操作上线后，需要进行对应的代码优化
 * 抽象类网关任务处理程序
 *
 * @author shizeying
 * @date 2022/11/08
 * @since 0.3.2
 */
@NoArgsConstructor
public abstract class AbstractGatewayTaskHandler extends AbstractOpManagerTaskHandler {
		
		protected final ILog LOGGER = LogFactory.getLog(this.getClass());
		
		
		@Autowired
		protected GatewayClusterManager     gatewayClusterManager;
		@Autowired
		protected GatewayClusterNodeManager gatewayClusterNodeManager;
		
		
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
		
		@Override
		protected List<TupleTwo<String, Integer>> convertFGeneralGroupConfigDTO2IpAndPortTuple(
				List<GeneralGroupConfigDTO> dtos) {
				//TODO 实现抓换
				return Collections.emptyList();
		}
		
		protected String getHttpPort(String fileConfig) {
				return null;
		}
		
}