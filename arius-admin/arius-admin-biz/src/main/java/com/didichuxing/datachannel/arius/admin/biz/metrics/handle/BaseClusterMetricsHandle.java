package com.didichuxing.datachannel.arius.admin.biz.metrics.handle;

import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.ALL_CLUSTER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.DEFAULT_TIME_INTERVAL;
import static com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant.MAX_TIME_INTERVAL;

import com.didichuxing.datachannel.arius.admin.biz.component.MetricsValueConvertUtils;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.linechart.VariousLineChartMetrics;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.MetricsVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.metrics.top.VariousLineChartMetricsVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.entity.user.User;
import com.didiglobal.logi.security.service.ProjectService;
import com.didiglobal.logi.security.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 抽象类集群指标处理
 *
 * @author
 * @date 2022/05/24
 */
public abstract class BaseClusterMetricsHandle implements BaseHandle {
    private static final ILog      LOGGER      = LogFactory.getLog(BaseClusterMetricsHandle.class);

    protected static final Integer MAX_TOP_NUM = 20;
    protected static final Integer MIN_TOP_NUM = 5;

    @Autowired
    private ProjectService         projectService;

    @Autowired
    private UserService            userService;

    /**
     * 获取物理集群节点、节点任务、模板或者节点任务的指标信息
     * @param userName 账号
     * @param projectId projectId
     * @param param 物理集群指标信息
     * @return 对应视图板块下的时序指标信息列表
     */
    public Result<List<VariousLineChartMetricsVO>> getClusterPhyRelatedCurveMetrics(MetricsClusterPhyDTO param,
                                                                                    Integer projectId,
                                                                                    String userName) {
        //1. verification
        Result<Void> checkParamResult = checkParamForClusterPhyMetrics(param, projectId, userName);
        if (checkParamResult.failed()) {
            LOGGER.warn("class=ClusterBaseMetricsHandle||method=getClusterPhyMetrics||msg=check param fail");
            return Result.buildFrom(checkParamResult);
        }

        //2. initialization
        init(param);

        //3. get metrics from es engine
        List<VariousLineChartMetrics> variousLineChartMetrics = getAggClusterPhyMetrics(param);

        //4. uniform percentage unit
        MetricsValueConvertUtils.convertClusterPhyMetricsPercent(variousLineChartMetrics);

        //5. optimize query burr
        MetricsValueConvertUtils.doOptimizeQueryBurrForNodeOrIndicesMetrics(variousLineChartMetrics);

        return Result.buildSucc(ConvertUtil.list2List(variousLineChartMetrics, VariousLineChartMetricsVO.class));
    }

    /**
     * 获取当前时刻集群的整体指标,其中包含非曲线数据，例如集群总览视图指标
     * @param param 物理集群指标信息
     * @param projectId projectId
     * @param userName 账号类型
     * @return 当前时刻下的集群整体指标
     */
    public Result<MetricsVO> getOtherClusterPhyRelatedMetricsVO(MetricsClusterPhyDTO param, Integer projectId,
                                                                String userName) {
        //1. verification
        Result<Void> checkParamResult = checkParamForClusterPhyMetrics(param, projectId, userName);
        if (checkParamResult.failed()) {
            LOGGER.warn("class=ClusterBaseMetricsHandle||method=getMetricsVO||msg=check param fail");
            return Result.buildFrom(checkParamResult);
        }

        //2. initialization
        init(param);

        return Result.buildSucc(buildClusterPhyMetricsVO(param));
    }

    private Result<Void> checkParamForClusterPhyMetrics(MetricsClusterPhyDTO param, Integer projectId,
                                                        String userName) {
        Result<Void> checkCommonParam = checkCommonParam(param, projectId, userName);
        if (checkCommonParam.failed()) {
            return checkCommonParam;
        }

        Result<Void> checkSpecialParamResult = checkSpecialParam(param);
        if (checkSpecialParamResult.failed()) {
            return checkSpecialParamResult;
        }

        return Result.buildSucc();
    }

    private Result<Void> checkCommonParam(MetricsClusterPhyDTO param, Integer projectId, String userName) {
        if (null == param) {
            return Result.buildParamIllegal("param is empty");
        }

        if (null == projectId) {
            return Result.buildParamIllegal("projectId is empty");
        }
        final User user = userService.getUserByUserName(userName);
        if (Objects.isNull(user)) {
            return Result.buildParamIllegal("user info is empty");
        }

        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("There is no projectId:%s", projectId));
        }

        return Result.buildSucc();
    }

    private void init(MetricsClusterPhyDTO param) {
        initCommonParam(param);
        initMetricsClusterPhy(param);
    }

    private void initCommonParam(MetricsClusterPhyDTO param) {
        if (AriusObjUtils.isBlack(param.getClusterPhyName())) {
            param.setClusterPhyName(ALL_CLUSTER);
        }

        if (0 == param.getEndTime() || null == param.getEndTime()) {
            param.setEndTime(System.currentTimeMillis());
        }

        if (0 == param.getStartTime() || null == param.getStartTime()) {
            param.setStartTime(param.getEndTime() - DEFAULT_TIME_INTERVAL);
        }

        //防止内存打爆, 触发熔断, 兜底方案, 结束时间近一周
        long intervalTime = param.getEndTime() - param.getStartTime();
        if (intervalTime > MAX_TIME_INTERVAL) {
            param.setStartTime(param.getEndTime() - MAX_TIME_INTERVAL);
        }

        if (null != param.getTopNu()) {
            if (param.getTopNu() <= 0) {
                param.setTopNu(MIN_TOP_NUM);
            }

            if (param.getTopNu() > MAX_TOP_NUM) {
                param.setTopNu(MAX_TOP_NUM);
            }
        }
    }

    /**
     * 构建物理集群的整体指标
     *
     * @param param 物理集群指标
     * @return 集群指标类型视图
     */
    protected MetricsVO buildClusterPhyMetricsVO(MetricsClusterPhyDTO param) {
        return null;
    }

    /**
     * 从ES引擎中获取对应的物理指标类型
     *
     * @param param 物理集群指标
     * @return 对应指标下的时序信息列表
     */
    protected List<VariousLineChartMetrics> getAggClusterPhyMetrics(MetricsClusterPhyDTO param) {
        return new ArrayList<>();
    }

    /**
     * 不同视图间自有的校验规则
     *
     * @param param 物理集群指标
     * @return 校验结果
     */
    protected abstract Result<Void> checkSpecialParam(MetricsClusterPhyDTO param);

    /**
     * 初始化物理指标信息DTO
     *
     * @param param 物理集群指标
     */
    protected abstract void initMetricsClusterPhy(MetricsClusterPhyDTO param);
}