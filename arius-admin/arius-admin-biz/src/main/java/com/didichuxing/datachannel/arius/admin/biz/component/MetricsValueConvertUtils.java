package com.didichuxing.datachannel.arius.admin.biz.component;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.other.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.percentiles.ESPercentilesMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContent;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.MetricsContentCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyNodeMetricsEnum.getPercentMetricsType;

/**
 * Created by linyunan on 2021-09-07
 */
public class MetricsValueConvertUtils {

	private MetricsValueConvertUtils(){}

	/**
	 * uniform cluster overview percentage unit
	 *
	 * @param esClusterOverviewMetricsVO
	 */
	public static void convertClusterOverviewMetricsPercent(ESClusterOverviewMetricsVO esClusterOverviewMetricsVO) {
		List<DiskUsageMetricsVO> diskUsagesMetrics = esClusterOverviewMetricsVO.getDiskUsage();
		if (CollectionUtils.isEmpty(diskUsagesMetrics)) { return;}

		diskUsagesMetrics.forEach(element->{
			element.setAggType(element.getAggType() * 100);
			element.setSt99(element.getSt99() * 100);
			element.setSt95(element.getSt95() * 100);
			element.setSt75(element.getSt75() * 100);
			element.setSt55(element.getSt55() * 100);
		});
	}

	/**
	 * uniform clusterPhy node percentage unit
	 * @param variousLineChartMetrics
	 */
	public static void convertClusterPhyMetricsPercent(List<VariousLineChartMetrics> variousLineChartMetrics) {
		variousLineChartMetrics.parallelStream()
				.filter(element -> getPercentMetricsType().contains(element.getType()))
				.forEach(MetricsValueConvertUtils::convertCell);
	}

	/***************************************************cluster overview**********************************************************/
	public static void doOptimizeForWriteTps(List<WriteTPSMetricsVO> writeTpsList) {
		if (CollectionUtils.isEmpty(writeTpsList)) return;

		for (int i = 0; i < writeTpsList.size(); i++) {
			WriteTPSMetricsVO currentMetrics = writeTpsList.get(i);
			WriteTPSMetricsVO frontMetrics   = null;
			WriteTPSMetricsVO backMetrics    = null;
			WriteTPSMetricsVO backNextMetrics    = null;

			if (i == 0) backMetrics = writeTpsList.get(i + 1);
			else if (i == writeTpsList.size() - 1) frontMetrics = writeTpsList.get(i - 1);
			else {
				if (i != writeTpsList.size() - 2) backNextMetrics = writeTpsList.get(i + 2);
				backMetrics  = writeTpsList.get(i + 1);
				frontMetrics = writeTpsList.get(i - 1);
			}

			if (currentMetrics.getWriteTps() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getWriteTps() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getWriteTps()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getWriteTps(): 0;
				currentMetrics.setWriteTps(compute(currentMetrics.getWriteTps(), frontValue, backValue, backNextValue));
			}
		}
	}

	public static void doOptimizeForReadTps(List<ReadQPSMetricsVO> readTpsList) {
		if (CollectionUtils.isEmpty(readTpsList)) return;

		for (int i = 0; i < readTpsList.size(); i++) {
			ReadQPSMetricsVO currentMetrics = readTpsList.get(i);
			ReadQPSMetricsVO frontMetrics   = null;
			ReadQPSMetricsVO backMetrics    = null;
			ReadQPSMetricsVO backNextMetrics    = null;

			if (i == 0) backMetrics = readTpsList.get(i + 1);
			else if (i == readTpsList.size() - 1) frontMetrics = readTpsList.get(i - 1);
			else {
				if (i != readTpsList.size() - 2) backNextMetrics = readTpsList.get(i + 2);
				backMetrics  = readTpsList.get(i + 1);
				frontMetrics = readTpsList.get(i - 1);
			}

			if (currentMetrics.getReadTps() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getReadTps() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getReadTps()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getReadTps(): 0;
				currentMetrics.setReadTps(compute(currentMetrics.getReadTps(), frontValue, backValue, backNextValue));
			}
		}
	}

	public static void doOptimizeForShardNu(List<ShardInfoMetricsVO> shardNuList) {
		if (CollectionUtils.isEmpty(shardNuList)) return;

		for (int i = 0; i < shardNuList.size(); i++) {
			ShardInfoMetricsVO currentMetrics = shardNuList.get(i);
			ShardInfoMetricsVO frontMetrics   = null;
			ShardInfoMetricsVO backMetrics    = null;
			ShardInfoMetricsVO backNextMetrics    = null;

			if (i == 0) backMetrics = shardNuList.get(i + 1);
			else if (i == shardNuList.size() - 1) frontMetrics = shardNuList.get(i - 1);
			else {
				if (i != shardNuList.size() - 2) backNextMetrics = shardNuList.get(i + 2);
				backMetrics  = shardNuList.get(i + 1);
				frontMetrics = shardNuList.get(i - 1);
			}

			if (currentMetrics.getUnAssignedShards() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getUnAssignedShards() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getUnAssignedShards()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getUnAssignedShards(): 0;
				currentMetrics.setUnAssignedShards((long) compute(currentMetrics.getUnAssignedShards(), frontValue, backValue, backNextValue));
			}

			if (currentMetrics.getShardNu() <= 0) {
				double frontValue = null != frontMetrics ?  frontMetrics.getShardNu() : 0;
				double backValue  = null != backMetrics  ?  backMetrics.getShardNu()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getShardNu(): 0;
				currentMetrics.setShardNu((long) compute(currentMetrics.getShardNu(), frontValue, backValue, backNextValue));
			}
		}
	}

	public static void doOptimizeForDiskInfo(List<DiskInfoMetricsVO> diskInfoList) {
		if (CollectionUtils.isEmpty(diskInfoList)) return;

		for (int i = 0; i < diskInfoList.size(); i++) {
			DiskInfoMetricsVO currentMetrics = diskInfoList.get(i);
			DiskInfoMetricsVO frontMetrics       = null;
			DiskInfoMetricsVO backMetrics        = null;
			DiskInfoMetricsVO backNextMetrics    = null;

			if (i == 0) backMetrics = diskInfoList.get(i + 1);
			else if (i == diskInfoList.size() - 1) frontMetrics = diskInfoList.get(i - 1);
			else {
				if (i != diskInfoList.size() - 2) backNextMetrics = diskInfoList.get(i + 2);
				backMetrics  = diskInfoList.get(i + 1);
				frontMetrics = diskInfoList.get(i - 1);
			}

			if (currentMetrics.getFreeStoreSize() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getFreeStoreSize() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getFreeStoreSize()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getFreeStoreSize(): 0;
				currentMetrics.setFreeStoreSize(compute(currentMetrics.getFreeStoreSize(), frontValue, backValue, backNextValue));
			}

			if (currentMetrics.getStoreSize() <= 0) {
				double frontValue = null != frontMetrics ?  frontMetrics.getStoreSize() : 0;
				double backValue  = null != backMetrics  ?  backMetrics.getStoreSize()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getStoreSize(): 0;
				currentMetrics.setStoreSize(compute(currentMetrics.getStoreSize(), frontValue, backValue, backNextValue));
			}

			if (currentMetrics.getTotalStoreSize() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getTotalStoreSize() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getTotalStoreSize()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getTotalStoreSize(): 0;
				currentMetrics.setTotalStoreSize(compute(currentMetrics.getTotalStoreSize(), frontValue, backValue, backNextValue));
			}
		}
	}

	public static void doOptimizeForSendTransSize(List<SendTransMetricsVO> sendTransSizeList) {
		if (CollectionUtils.isEmpty(sendTransSizeList)) return;

		for (int i = 0; i < sendTransSizeList.size(); i++) {
			SendTransMetricsVO currentMetrics = sendTransSizeList.get(i);
			SendTransMetricsVO frontMetrics   = null;
			SendTransMetricsVO backMetrics    = null;
			SendTransMetricsVO backNextMetrics    = null;

			if (i == 0) backMetrics = sendTransSizeList.get(i + 1);
			else if (i == sendTransSizeList.size() - 1) frontMetrics = sendTransSizeList.get(i - 1);
			else {
				if (i != sendTransSizeList.size() - 2) backNextMetrics = sendTransSizeList.get(i + 2);
				backMetrics  = sendTransSizeList.get(i + 1);
				frontMetrics = sendTransSizeList.get(i - 1);
			}

			if (currentMetrics.getSendTransSize() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getSendTransSize() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getSendTransSize()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getSendTransSize(): 0;
				currentMetrics.setSendTransSize(compute(currentMetrics.getSendTransSize(), frontValue, backValue, backNextValue));
			}
		}
	}

	public static void doOptimizeForRecvTransSize(List<RecvTransMetricsVO> recvTransSizeList) {
		if (CollectionUtils.isEmpty(recvTransSizeList)) return;

		for (int i = 0; i < recvTransSizeList.size(); i++) {
			RecvTransMetricsVO currentMetrics = recvTransSizeList.get(i);
			RecvTransMetricsVO frontMetrics   = null;
			RecvTransMetricsVO backMetrics    = null;
			RecvTransMetricsVO backNextMetrics    = null;

			if (i == 0) backMetrics = recvTransSizeList.get(i + 1);
			else if (i == recvTransSizeList.size() - 1) frontMetrics = recvTransSizeList.get(i - 1);
			else {
				if (i != recvTransSizeList.size() - 2) backNextMetrics = recvTransSizeList.get(i + 2);
				backMetrics  = recvTransSizeList.get(i + 1);
				frontMetrics = recvTransSizeList.get(i - 1);
			}

			if (currentMetrics.getRecvTransSize() <= 0) {
				double frontValue = null != frontMetrics ? frontMetrics.getRecvTransSize() : 0;
				double backValue  = null != backMetrics  ? backMetrics.getRecvTransSize()  : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getRecvTransSize(): 0;
				currentMetrics.setRecvTransSize(compute(currentMetrics.getRecvTransSize(), frontValue, backValue, backNextValue));
			}
		}
	}

	public static void doOptimizeForPercentiles(List<? extends ESPercentilesMetricsVO> percentilesMetricsVOList) {
		if (CollectionUtils.isEmpty(percentilesMetricsVOList)) return;

		for (int i = 0; i < percentilesMetricsVOList.size(); i++) {
			ESPercentilesMetricsVO currentMetrics = percentilesMetricsVOList.get(i);
			ESPercentilesMetricsVO frontMetrics   = null;
			ESPercentilesMetricsVO backMetrics    = null;
			// The forward second time point of the current time slice is still zero
			ESPercentilesMetricsVO backNextMetrics= null;

			if (i == 0) backMetrics = percentilesMetricsVOList.get(i + 1);
			else if (i == percentilesMetricsVOList.size() - 1) frontMetrics = percentilesMetricsVOList.get(i - 1);
			else {
				if (i != percentilesMetricsVOList.size() - 2) backNextMetrics = percentilesMetricsVOList.get(i + 2);
				backMetrics  = percentilesMetricsVOList.get(i + 1);
				frontMetrics = percentilesMetricsVOList.get(i - 1);
			}

			//compute avgst
			if (currentMetrics.getAggType() <= 0) {
				double frontValue     = null != frontMetrics    ?  frontMetrics.getAggType()   : 0;
				double backValue      = null != backMetrics     ?  backMetrics.getAggType()    : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getAggType(): 0;
				currentMetrics.setAggType(compute(currentMetrics.getAggType(), frontValue, backValue, backNextValue));
			}

			//compute 99st
			if (currentMetrics.getSt99() <= 0) {
				double frontValue     = null != frontMetrics    ?  frontMetrics.getSt99()   : 0;
				double backValue      = null != backMetrics     ?  backMetrics.getSt99()    : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getSt99(): 0;
				currentMetrics.setSt99(compute(currentMetrics.getSt99(), frontValue, backValue, backNextValue));
			}

			//compute 95st
			if (currentMetrics.getSt95() <= 0) {
				double frontValue     = null != frontMetrics    ?  frontMetrics.getSt95()   : 0;
				double backValue      = null != backMetrics     ?  backMetrics.getSt95()    : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getSt95(): 0;
				currentMetrics.setSt95(compute(currentMetrics.getSt95(), frontValue, backValue, backNextValue));
			}

			//compute 75st
			if (currentMetrics.getSt75() <= 0) {
				double frontValue     = null != frontMetrics    ?  frontMetrics.getSt75()   : 0;
				double backValue      = null != backMetrics     ?  backMetrics.getSt75()    : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getSt75(): 0;
				currentMetrics.setSt75(compute(currentMetrics.getSt75(), frontValue, backValue, backNextValue));
			}

			//compute 55st
			if (currentMetrics.getSt55() <= 0) {
				double frontValue     = null != frontMetrics    ?  frontMetrics.getSt55()   : 0;
				double backValue      = null != backMetrics     ?  backMetrics.getSt55()    : 0;
				double backNextValue  = null != backNextMetrics ?  backNextMetrics.getSt55(): 0;
				currentMetrics.setSt55(compute(currentMetrics.getSt55(), frontValue, backValue, backNextValue));
			}
		}
	}

	/***************************************************cluster node*************************************************/
	public static void doOptimizeQueryBurrForNodeOrIndicesMetrics(List<VariousLineChartMetrics> variousLineChartMetrics) {
		if (CollectionUtils.isEmpty(variousLineChartMetrics)) return;
		//multiple view
		variousLineChartMetrics.parallelStream().forEach(variousLineChartMetric-> {
			List<MetricsContent> metricsContentList = variousLineChartMetric.getMetricsContents();
			if (CollectionUtils.isEmpty(metricsContentList)) return;
			//multiple line
			for (MetricsContent metricsContent : metricsContentList) {

				//multiple port
				List<MetricsContentCell> metricsContentCellList = metricsContent.getMetricsContentCells();
				if (CollectionUtils.isEmpty(metricsContentCellList)) return;

				for (int i = 0; i < metricsContentCellList.size(); i++) {
					compensateAbnormalValue(metricsContentCellList, i);
				}
			}
		});
	}

	private static void compensateAbnormalValue(List<MetricsContentCell> metricsContentCellList, int i) {
		MetricsContentCell currentMetrics = metricsContentCellList.get(i);
		double frontValue = i == 0 ? 0 : metricsContentCellList.get(i - 1).getValue();
		double backValue = i == metricsContentCellList.size() - 1 ? 0 : metricsContentCellList.get(i + 1).getValue();
		double compensateValue;

		if (i == 0) {
			// 处理头部掉底
			compensateValue = backValue;
		} else if (i == metricsContentCellList.size() - 1) {
			// 处理尾部掉底
			compensateValue = frontValue;
		} else {
			// 处理中部掉底
			compensateValue = Math.min(frontValue, backValue);
		}

		if (currentMetrics.getValue() <= 0) {
			currentMetrics.setValue(compensateValue);
		}
	}

	/***************************************************cluster indices*************************************************/
	private static double compute(double currentValue, double frontValue, double backValue, double backNextValue) {
		if (0 >= backNextValue) return 0;

		double tempMax;
		double finalMax;
		if (0 >= frontValue) tempMax = Math.max(currentValue, 0);
		else tempMax = Math.max(currentValue, frontValue);

		if (0 >= backValue) finalMax = Math.max(tempMax, 0);
		else finalMax = Math.max(tempMax, backValue);

		return finalMax;
	}

	private static void convertCell(VariousLineChartMetrics variousLineChartMetrics) {
        List<MetricsContent> metricsContents = variousLineChartMetrics.getMetricsContents();
        metricsContents.forEach(metricsContent ->
				metricsContent.getMetricsContentCells().forEach(cell -> cell.setValue(cell.getValue() * 100)));
	}
}