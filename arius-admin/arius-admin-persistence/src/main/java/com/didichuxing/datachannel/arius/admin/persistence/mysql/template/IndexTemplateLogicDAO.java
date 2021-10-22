package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLogicPO;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Repository
public interface IndexTemplateLogicDAO {

    List<TemplateLogicPO> listByCondition(TemplateLogicPO param);

    int insert(TemplateLogicPO param);

    int update(TemplateLogicPO param);

    int delete(Integer logicId);

    TemplateLogicPO getById(Integer logicId);

    List<TemplateLogicPO> listByAppId(Integer appId);

    List<TemplateLogicPO> listAll();

    List<TemplateLogicPO> listByIds(List<Integer> logicIds);

    List<TemplateLogicPO> listByDataCenter(String dataCenter);

    List<TemplateLogicPO> listByName(String name);

    List<TemplateLogicPO> likeByCondition(TemplateLogicPO param);

    List<TemplateLogicPO> likeByResponsible(String responsible);

    int batchChangeHotDay(Integer days);
}
