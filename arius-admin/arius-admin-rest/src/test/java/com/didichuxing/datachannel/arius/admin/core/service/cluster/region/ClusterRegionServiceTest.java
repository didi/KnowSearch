package com.didichuxing.datachannel.arius.admin.core.service.cluster.region;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.region.ClusterRegionDAO;
import com.google.common.collect.Lists;

@Transactional
@Rollback
public class ClusterRegionServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ClusterRegionDAO clusterRegionDAO;

    @MockBean
    private ClusterLogicService clusterLogicService;

    @MockBean
    private ClusterPhyService esClusterPhyService;

    @MockBean
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    private static final String clusterName = "wpk";

    @BeforeEach
    public void mockRules() {
        Mockito.when(esClusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(new ClusterPhy());
        Mockito.when(indexTemplatePhyService.listByRegionId(Mockito.anyInt()).getData()).thenReturn(Collections.singletonList(new IndexTemplatePhy()));
    }

    @Test
    public void listClusterRegionsByLogicIdsTest() {
        List<ClusterRegion> clusterRegions = clusterRegionService.getClusterRegionsByLogicIds(Lists.newArrayList(621L,453L, 451L));
        Assertions.assertNotNull(clusterRegions);
    }
}