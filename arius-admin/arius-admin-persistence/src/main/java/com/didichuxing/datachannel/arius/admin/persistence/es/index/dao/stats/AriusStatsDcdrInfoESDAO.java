package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats;

import com.didichuxing.datachannel.arius.admin.common.constant.AriusStatsEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AriusStatsDcdrInfoESDAO extends BaseAriusStatsESDAO {

    @PostConstruct
    public void init() {
        super.indexName = dataCentreUtil.getAriusStatsDcdrInfo();

        BaseAriusStatsESDAO.register(AriusStatsEnum.DCDR_INFO, this);
    }
}
