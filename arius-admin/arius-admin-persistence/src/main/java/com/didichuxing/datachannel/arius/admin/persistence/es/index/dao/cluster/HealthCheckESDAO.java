package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckRecordPo;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class HealthCheckESDAO extends BaseESDAO {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * type名称
     */
    private final static String RECORD_TYPE = "record";
    private final static String ERR_INFO_TYPE = "errInfo";

    @PostConstruct
    public void init(){
        this.indexName = dataCentreUtil.getAriusHealthCheck();
    }

    public boolean batchInsertErrInfo(List<HealthCheckErrInfoPo> infos){
        return updateClient.batchInsert(genCurrentDayIndex(), ERR_INFO_TYPE, infos);
    }

    public boolean batchInsertRecord(List<HealthCheckRecordPo> infos){
        return updateClient.batchInsert(genCurrentDayIndex(), RECORD_TYPE, infos);
    }

    private String genCurrentDayIndex() {
        return indexName + "_" + DateTimeUtil.getFormatDayByOffset(0);
    }

}
