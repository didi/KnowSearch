package com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateNotifyESPO;
import com.didichuxing.datachannel.arius.admin.persistence.es.BaseESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslsConstant;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class TemplateNotifyDAO extends BaseESDAO {

    private String index;
    private String type = "type";

    @PostConstruct
    public void init(){
        this.index = dataCentreUtil.getAriusTemplateQutoaNotiryRecord();
    }

    public List<TemplateNotifyESPO> getByLogicTemplIdAndRate(Integer logicTemplateId, String zeroDate, Integer ratio){
        String realDsl   = dslLoaderUtil.getFormatDslByFileName( DslsConstant.GET_BY_LOGIC_TEMPLATE_AND_RATE, logicTemplateId, zeroDate, ratio);
        String realIndex = genIndexNameByDate(index);

        List<TemplateNotifyESPO> templateNotifyESPOList = gatewayClient.performRequest(realIndex, type, realDsl, TemplateNotifyESPO.class);

        LOGGER.info("class=UpdateClient||method=getByLogicTemplIdAndRate||logicTemplateId={}||zeroDate={}||ratio={}||dsl={}||templateNotifyESPOList={}",
                logicTemplateId, zeroDate, ratio, realDsl, JSON.toJSONString(templateNotifyESPOList));

        return templateNotifyESPOList;
    }

    public boolean insertTemplateNotifyESPO(TemplateNotifyESPO templateNotifyESPO){
        return updateClient.batchInsert(genIndexNameByDate(index), type, Arrays.asList(templateNotifyESPO));
    }

    /**************************************** private methods ****************************************/
    private String getDateStr(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(time));
    }

    private String genIndexNameByDate(Long date, String indexName){
        return indexName + "_" + getDateStr(date);
    }

    private String genIndexNameByDate(String indexName){
        return genIndexNameByDate(System.currentTimeMillis(), indexName);
    }
}
