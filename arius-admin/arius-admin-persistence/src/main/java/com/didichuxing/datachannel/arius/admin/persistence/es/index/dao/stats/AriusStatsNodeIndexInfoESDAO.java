package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AriusStatsNodeIndexInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init(){
        super.indexName   = dataCentreUtil.getAriusStatsNodeIndexInfo();

        register( AriusStatsEnum.NODE_INDEX_INFO,this);
    }
}
