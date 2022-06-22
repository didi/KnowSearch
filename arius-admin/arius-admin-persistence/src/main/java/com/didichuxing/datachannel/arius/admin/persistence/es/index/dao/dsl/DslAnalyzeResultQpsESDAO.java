package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.DslBase;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslAnalyzeResultQpsPO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@NoArgsConstructor
public class DslAnalyzeResultQpsESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * type名称
     */
    private String typeName = "type";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusDslAnalyzeResult();
    }

    /**
     * 批量保存projectId 查询模板qps信息到es
     *
     * @param projectIdTemplateQpsInfoList
     * @return
     */
    public boolean bathInsert(List<DslAnalyzeResultQpsPO> projectIdTemplateQpsInfoList) {

        return updateClient.batchInsert( EnvUtil.getWriteIndexNameByEnv(this.indexName), typeName, projectIdTemplateQpsInfoList);
    }

    /**
     * 获取最大查询qps根据projectid和templatemd5
     *
     * @param dslBase
     * @return
     */
    public DslAnalyzeResultQpsPO getMaxProjectIdTemplateQpsInfoByProjectIdTemplateMd5(DslBase dslBase) {
        if (null == dslBase) {
            return null;
        }

        String dsl = dslLoaderUtil.getFormatDslByFileName(DslsConstant.GET_MAX_QPS_BY_APPID_DSLTEMPLATE, dslBase.getProjectId(), dslBase.getDslTemplateMd5());

        return gatewayClient.performRequestAndTakeFirst(indexName, typeName, dsl, DslAnalyzeResultQpsPO.class);
    }
}