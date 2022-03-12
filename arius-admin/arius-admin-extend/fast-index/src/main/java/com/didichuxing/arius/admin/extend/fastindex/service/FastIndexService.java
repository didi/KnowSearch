package com.didichuxing.arius.admin.extend.fastindex.service;

import com.didichuxing.arius.admin.extend.fastindex.dao.*;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FastIndexService {
    private static final ILog LOGGER = LogFactory.getLog(FastIndexService.class);

    public static final String LUCENE_VERSION_840 = "8.4.0";
    public static final String LUCENE_VERSION_760 = "7.6.0";
    public static final String ES_VERSION_760 = "7.6.0";
    public static final String ES_VERSION_670 = "6.7.0";
    public static final String ES_VERSION_661 = "6.6.1";

    @Autowired
    private FastIndexLoadDataESDAO fastIndexLoadDataESDao;

    @Autowired
    private FastIndexMappingESDAO fastIndexMappingESDao;

    @Autowired
    private FastIndexOpIndexESDAO fastIndexOpIndexESDao;

    @Autowired
    private FastIndexTaskMetricESDAO fastIndexTaskMetricESDao;

    @Autowired
    private FastIndexTemplateConfigESDAO fastIndexTemplateConfigESDao;

    @Autowired
    private ESOpClient                 esOpClient;

    private static final Long DAY_TIME = 24 * 60 * 60 * 1000L;

    private static final int EXPANFACTOR = 20;

    private static final String ES_PATH_FORMAT      = "/data1/es/%s/nodes/0/indices/%s/%d";
    private static final String ES_PATH_FORMAT_661  = "/data1/es/nodes/0/indices/%s/%d";

    private static final String WORK_DIR_FORMAT = "/data1/es/fastIndex/%s_shard%d";

    private static final String SOURCE_STR  = "_source";

    private static final String MSG         = "msg";
    private static final String SUCCESS     = "success";
    private static final String SELF_IP     = "selfIp";
    private static final String DEVICE_NAME = "deviceName";

    private static final long   MAX_COUNT   = 90; // 90分钟

    private static final String CMDB_URL = ;
    private static final String STAR_URL = ;
}
