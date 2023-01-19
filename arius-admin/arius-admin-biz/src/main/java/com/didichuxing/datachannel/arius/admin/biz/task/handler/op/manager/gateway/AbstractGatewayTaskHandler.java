package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.gateway;

import static java.util.regex.Pattern.compile;

import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.alibaba.fastjson.JSONValidator.Type;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterManager;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.AbstractOpManagerTaskHandler;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.google.common.collect.Lists;

import lombok.NoArgsConstructor;

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
						return false;
				}
				JSONObject jsonObject = JSON.parseObject(fileConfig);
				return jsonObject.values().stream().map(String::valueOf)
						.anyMatch(i -> StringUtils.countMatches(i,"gateway.httpTransport.port=")>0);
		}
		
		protected String  getHttpPort(String fileConfig) {
				// 获取端口号
				final String portStr =
						JSON.parseObject(fileConfig).values().stream().map(String::valueOf)
								.filter(i -> StringUtils.countMatches(i,"gateway.httpTransport.port=")>0)
								.findFirst().get();
				final Matcher matcher = compile("gateway.httpTransport.port=\\d+").matcher(portStr);
				if (matcher.find()) {
						final Matcher portMat = compile("\\d+").matcher(
								matcher.group());
						if (portMat.find()) {
								return portMat.group(0);
						}
				}
				return "0";
		}
		
		@Override
		protected List<TupleTwo<String, Integer>> convertFGeneralGroupConfigDTO2IpAndPortTuple(
				List<GeneralGroupConfigDTO> dtos) {
				List<TupleTwo<String, Integer>> tupleTwos = Lists.newArrayList();
				for (GeneralGroupConfigDTO dto : dtos) {
						final String hosts            = dto.getHosts();
						final JSONObject processNumConfigJson = JSON.parseObject(dto.getProcessNumConfig());
						final String fileConfig       = dto.getFileConfig();
						// 获取端口号
						final Integer port = Integer.parseInt(getHttpPort(fileConfig));
						if (port != 0) {
								final List<String> hostsList = Lists.newArrayList(StringUtils.split(hosts, ","));
								for (String host : hostsList) {
										// 提取 host 对应的进程数
										final Integer processNum = processNumConfigJson.getInteger(host);
										for (Integer i = 0; i < processNum; i++) {
												tupleTwos.add(Tuples.of(host, port + i));
										}
								}
						}
				}
				return tupleTwos;
		}
		
		
		
}