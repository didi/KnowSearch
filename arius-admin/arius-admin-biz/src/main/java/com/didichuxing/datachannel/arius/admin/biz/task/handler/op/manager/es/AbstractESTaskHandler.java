package com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.es;

import static java.util.regex.Pattern.compile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.task.handler.op.manager.AbstractOpManagerTaskHandler;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.logi.op.manager.interfaces.dto.general.GeneralGroupConfigDTO;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
			
				return StringUtils.isNotBlank(getHttpPort(fileConfig));
		}
		@Override
		protected List<TupleTwo<String, Integer>> convertFGeneralGroupConfigDTO2IpAndPortTuple(
				List<GeneralGroupConfigDTO> dtos) {
				return Optional.ofNullable(dtos).orElse(Collections.emptyList()).stream()
						.map(i -> hostsConvertsIpAndPortList(i.getHosts(),
								getPortByHttpPort(getHttpPort(i.getFileConfig())),i.getProcessNumConfig()))
						.flatMap(Collection::stream)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
		}
		
		/**
		 * 它接受一个字符串并返回一个字符串
		 *
		 * @param fileConfig 文件的内容。
		 * @return http 服务器的端口号。
		 */
		protected String getHttpPort(String fileConfig) {
				Matcher matcher = compile("http.port:\\s+\\d+").matcher(fileConfig);
				if (matcher.find()) {
						return matcher.group();
				}
				Matcher matcher2 = compile("http:\\\\n\\s*port:\\s*\\d*").matcher(fileConfig);
				if (matcher2.find()) {
						return matcher2.group();
				}
				return null;
		}
		/**
		 * > 它接受一个字符串，并返回它在该字符串中找到的第一个数字
		 *
		 * @param httpPort 应用程序运行的端口。
		 * @return httpPort 字符串的端口号。
		 */
		private Integer getPortByHttpPort(String httpPort){
				Matcher matcher = compile("\\d+").matcher(httpPort);
				if (matcher.find()) {
						return Integer.parseInt(matcher.group());
				}
				return null;
		}
		
		/**
		 * 它将逗号分隔的主机列表和端口转换为元组列表。
		 *
		 * @param hosts            Redis 服务器的 IP 地址。
		 * @param port             Redis 服务器的端口号。
		 * @param processNumConfig
		 * @return 元组列表。
		 */
		private List<TupleTwo<String, Integer>> hostsConvertsIpAndPortList(String hosts, Integer port,
				String processNumConfig) {
				List<TupleTwo<String, Integer>> ipPort               = Lists.newArrayList();
				final JSONObject                processNumConfigJson = JSON.parseObject(processNumConfig);
				for (String host : StringUtils.split(hosts, ",")) {
						// 提取 host 对应的进程数
						final Integer processNum = processNumConfigJson.getInteger(host);
						for (Integer i = 0; i < processNum; i++) {
								ipPort.add(Tuples.of(host, port + i));
						}
				}
				return ipPort;
		}
		
}