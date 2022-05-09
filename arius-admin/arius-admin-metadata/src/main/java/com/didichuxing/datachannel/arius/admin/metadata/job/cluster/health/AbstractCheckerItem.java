package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.health;

import com.alibaba.fastjson.JSON;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfoWithLogic;
import com.didichuxing.datachannel.arius.admin.common.constant.ResultLevel;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckRecordPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckWhiteListPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.cluster.HealthCheckESDAO;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.bean.po.health.HealthCheckErrInfoPO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractCheckerItem implements ICheckerItem{
    protected final ILog logger = LogFactory.getLog(AbstractCheckerItem.class);

    private ClusterHealthCheckJobConfig clusterHealthCheckJobConfig;

    private ResultLevel resultLevel;

    private Timestamp startTime;

    private List<HealthCheckErrInfoPO> checkErrInfoPos = new ArrayList<>();

    private HealthCheckRecordPO checkRecordPo;

    protected AbstractCheckerItem(){}

    @Override
    public void exec(ClusterHealthCheckJobConfig checkJobConfig) {
        clusterHealthCheckJobConfig = checkJobConfig;

        try {
            startTime       = new Timestamp(System.currentTimeMillis());
            checkErrInfoPos = execCheckRecordErrInfo();
            resultLevel     = genResultLevel();
            checkRecordPo   = genHealthCheckRecordPo();

            saveHealthCheckResutl(checkRecordPo, checkErrInfoPos);
        } catch (Exception e) {
            logger.error("class=AbstractCheckerItem||method=exec||msg=execInternal error", e);
            resultLevel = ResultLevel.ERROR;
        }

        if(isLogOpen()){
            logger.info("class=AbstractCheckerItem||method=exec||check={}||resultLevel={}||checkRecordPo={}||checkErrInfoPos={}",
                    getType().getName(), resultLevel, JSON.toJSONString(checkRecordPo), JSON.toJSONString(checkErrInfoPos));
        }
    }

    /**************************************** protected methods ****************************************/
    protected Timestamp getStartTime(){
        return startTime;
    }

    protected ClusterHealthCheckJobConfig getClusterHealthCheckJobConfig(){
        return clusterHealthCheckJobConfig;
    }

    protected abstract long  getCheckerTotalNu();

    protected abstract List<HealthCheckErrInfoPO> execCheckRecordErrInfo();

    protected String[] getLevelConfig(){
        return new String[]{};
    }

    protected long getErrorSize(){
        return checkErrInfoPos.size();
    }

    protected ResultLevel genResultLevel(){
        double badPercent = 0.0;
        long checkSize = getCheckerTotalNu();
        if (checkSize != 0) {
            badPercent = (double) getErrorSize() / checkSize;
        }
        return ResultLevel.getResultlevel(getLevelConfig(), badPercent);
    }

    protected HealthCheckRecordPO genHealthCheckRecordPoInner(HealthCheckRecordPO healthCheckRecordPo){
        return healthCheckRecordPo;
    }

    protected boolean iskibanaIndex(String index){
        return index.startsWith(".marvel-es") || index.startsWith(".kibana");
    }

    protected boolean isWhiteIndex(String index){
        List<HealthCheckWhiteListPO> healthCheckWhiteListPOS = getClusterHealthCheckJobConfig().getHealthCheckWhiteListPOS();
        for(HealthCheckWhiteListPO whiteListPo : healthCheckWhiteListPOS){
            String cluster   = getClusterHealthCheckJobConfig().getClusterName();
            int    chekcType = getType().getCode();

            if(StringUtils.isEmpty(index)){
                if(cluster.equals(whiteListPo.getCluster())
                        && chekcType == whiteListPo.getCheckType()){
                    return true;
                }
            }else {
                if(cluster.equals(whiteListPo.getCluster())
                        && chekcType == whiteListPo.getCheckType()
                        && index.equals(whiteListPo.getTemplate())){
                    return true;
                }
            }
        }

        return false;
    }

    protected IndexTemplatePhyInfoWithLogic getIndexTemplateByIndex(String index){
        Map<String, IndexTemplatePhyInfoWithLogic> indexTemplateMap = clusterHealthCheckJobConfig.getIndexTemplateMap();
        IndexTemplatePhyInfoWithLogic indexTemplate = null;
        for(Map.Entry<String, IndexTemplatePhyInfoWithLogic> entry : indexTemplateMap.entrySet()){
            String template = entry.getKey();
            if (index.startsWith(template)) {
                indexTemplate = indexTemplateMap.get(template);
                if(null != indexTemplate && null != indexTemplate.getLogicTemplate()){
                    String version    = String.valueOf(indexTemplate.getVersion());
                    String indexName  = indexTemplate.getExpression().replace("*", "") + indexTemplate.getLogicTemplate().getDateFormat();
                    if (
                            ("0".equals(version) && index.length() == indexName.length()) ||
                                    (index.length() >= indexName.length() && index.length() <= indexName.length() + version.length() + 2)
                    )
                        break;
                }
            }
        }

        return indexTemplate;
    }

    /**************************************** private methods ****************************************/
    private HealthCheckRecordPO genHealthCheckRecordPo(){
        int    result   = resultLevel.getCode();
        int    typeId   = getType().getCode();
        long   errCount = getErrorSize();
        String typeName = getType().getName();
        String cluster  = clusterHealthCheckJobConfig.getClusterName();

        long checkSize = getCheckerTotalNu();
        checkSize = (0 == checkSize) ? 1 : checkSize;

        HealthCheckRecordPO healthCheckRecord = new HealthCheckRecordPO();
        healthCheckRecord.setTypeId(typeId);
        healthCheckRecord.setTypeName(typeName);
        healthCheckRecord.setCluster(cluster);
        healthCheckRecord.setBeginTime(getStartTime());
        healthCheckRecord.setEndTime(new Timestamp(System.currentTimeMillis()));
        healthCheckRecord.setResult(result);
        healthCheckRecord.setErrRate((int) (1.0000 * errCount / checkSize * 100));
        healthCheckRecord.setCheckCount(checkSize);
        healthCheckRecord.setErrCount(errCount);

        return genHealthCheckRecordPoInner(healthCheckRecord);
    }

    private void saveHealthCheckResutl(HealthCheckRecordPO healthCheckRecord, List<HealthCheckErrInfoPO> healthCheckErrInfos){
        HealthCheckESDAO healthCheckEsDao = clusterHealthCheckJobConfig.getHealthCheckEsDao();

        boolean  errInsertSuccess    = healthCheckEsDao.batchInsertErrInfo(healthCheckErrInfos);
        boolean  recordInsertSuccess = healthCheckEsDao.batchInsertRecord(Arrays.asList(healthCheckRecord));

        if(!errInsertSuccess || !recordInsertSuccess){
            logger.error("class=AbstractCheckerItem||method=saveHealthCheckResutl||errInsertSuccess={}||recordInsertSuccess={}",
                    errInsertSuccess, recordInsertSuccess);
        }
    }

    private boolean isLogOpen(){
        return !EnvUtil.isOnline();
    }
}
