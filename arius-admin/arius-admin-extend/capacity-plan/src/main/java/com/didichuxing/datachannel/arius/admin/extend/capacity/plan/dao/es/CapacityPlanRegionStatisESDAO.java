package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao.es;

import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.po.CapacityPlanRegionStatisESPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class CapacityPlanRegionStatisESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusTemplateCapacityPlanRegionStatis();
    }

    /**
     * 插入记录
     * @param statisESPOS 插入内容
     * @return true/false
     */
    public boolean batchInsert(List<CapacityPlanRegionStatisESPO> statisESPOS) {
        return updateClient.batchInsert(indexName, AdminConstant.DEFAULT_TYPE, statisESPOS);
    }
}
