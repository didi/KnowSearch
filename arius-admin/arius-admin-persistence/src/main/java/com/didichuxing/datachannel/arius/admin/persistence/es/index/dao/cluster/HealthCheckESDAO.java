package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckRecordPO;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@NoArgsConstructor
public class HealthCheckESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * type名称
     */
    private static final String RECORD_TYPE = "record";
    private static final String ERR_INFO_TYPE = "errInfo";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusHealthCheck();
    }

    public boolean batchInsertErrInfo(List<HealthCheckErrInfoPO> infos){
        return updateClient.batchInsert(genCurrentDayIndex(), ERR_INFO_TYPE, infos);
    }

    public boolean batchInsertRecord(List<HealthCheckRecordPO> infos){
        return updateClient.batchInsert(genCurrentDayIndex(), RECORD_TYPE, infos);
    }

    private String genCurrentDayIndex() {
        return indexName + "_" + DateTimeUtil.getFormatDayByOffset(0);
    }

}
