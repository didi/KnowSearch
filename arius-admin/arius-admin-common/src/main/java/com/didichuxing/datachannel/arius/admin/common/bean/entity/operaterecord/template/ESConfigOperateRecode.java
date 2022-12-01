package com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum.valueOf;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord.Builder;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.DiffUtil;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.github.difflib.text.DiffRow;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ESConfigOperateRecode {
    public static final String DELIMITER = "\n";
    public static final String STR_EMPTY = "";
    /**
     * {@linkplain EsConfigActionEnum#getDesc()}
     */
    private String             operationType;
    /**
     * {@linkplain  ESConfig#getConfigData()} ()}
     */
    private String             source;
    /**
     * {@linkplain  ESConfig#getConfigData()} ()}
     */
    private String             target;

    /**
     * {@linkplain  ESConfig#getTypeName()}
     */
    private String             typeName;
    /**
     * {@linkplain  ESConfig#getEnginName()} ()}
     */
    private String             enginName;
    /**
     * {@linkplain  ESConfig#getVersionConfig()} ()}
     */
    private Integer            versionConfig;
    /**
     * 对比结果列表
     */
    private List<DiffRow>      diffResultList;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public static final BiFunction<ESConfig, Function<Long, ESConfig>, TupleTwo</*source*/ESConfig, /*target*/ESConfig>> sourceAndTargetConfigFunc = (esConfig,
                                                                                                                                                      getEsConfigByIdFunc) -> {
        final Long id = esConfig.getId();
        //新增
        if (Objects.isNull(id)) {
            return Tuples.of(null, esConfig);
        }
        final ESConfig oldEsConfig = getEsConfigByIdFunc.apply(id);
        return Tuples.of(esConfig, oldEsConfig);

    };

    public static ESConfigOperateRecode sourceTargetDiff(TupleTwo</*source*/ESConfig, /*target*/ESConfig> sourceTargetTuple,
                                                         Integer actionType) {
        final EsConfigActionEnum esConfigActionEnum = valueOf(actionType);
        String source = Optional.ofNullable(sourceTargetTuple.v1).map(ESConfig::getConfigData).orElse(STR_EMPTY);
        String target = Optional.ofNullable(sourceTargetTuple.v2).map(ESConfig::getConfigData).orElse(STR_EMPTY);
        final List<DiffRow> diffResultList = DiffUtil.diffRowsByString(source, target, DELIMITER);
        final String typeName = Optional.ofNullable(sourceTargetTuple.v1).map(ESConfig::getTypeName).orElse(STR_EMPTY);
        final String enginName = Optional.ofNullable(sourceTargetTuple.v1).map(ESConfig::getEnginName)
            .orElse(STR_EMPTY);
        final Integer versionConfig = Optional.ofNullable(sourceTargetTuple.v1).map(ESConfig::getVersionConfig)
            .orElse(0);

        return new ESConfigOperateRecode(esConfigActionEnum.getDesc(), source, target, typeName, enginName,
            versionConfig, diffResultList);

    }

    public static OperateRecord buildESConfigOperateRecode(String operationUser,
                                                           Function<Integer, ProjectBriefVO> getProjectByIdFunc,
                                                           Integer projectId,
                                                           ESConfigOperateRecode esConfigOperateRecode, Object bizId

    ) {

        return new Builder().userOperation(operationUser).project(getProjectByIdFunc.apply(projectId))
            .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_CONF_FILE_CHANGE)

            .content(esConfigOperateRecode.toString()).bizId(bizId).buildDefaultManualTrigger();
    }
}