package com.didichuxing.datachannel.arius.admin.metadata.job.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.dsl.DslTemplateESDAO;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import org.elasticsearch.common.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * @Author: D10865
 * @Description: 删除过期查询模板数据的handler
 * @Date: Create on 2018/6/15 下午12:43
 * @Modified By
 * <p>
 */
@Component
public class DslTemplateDelExpiredJob extends AbstractMetaDataJob {

    /**
     * 操作dsl template 索引
     */
    @Autowired
    private DslTemplateESDAO dslTemplateEsDao;

    /**
     * 处理任务
     *
     * @param params
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=DslTemplateDelExpiredJob||method=handleJobTask||params={}", params);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("get not use dsl");

        List<DslTemplatePO> deleteDslTemplatePOList = dslTemplateEsDao.getExpiredAndWillDeleteDslTemplate();
        boolean operatorDeleteResult = dslTemplateEsDao.deleteExpiredDslTemplate();

        String cost = stopWatch.stop().toString();

        LOGGER.info("class=DslTemplateDelExpiredJob||method=handleJobTask||size={}||result={}||cost={}",
                deleteDslTemplatePOList.size(), operatorDeleteResult, cost);

        return JOB_SUCCESS;
    }
}
