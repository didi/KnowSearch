package com.didichuxing.datachannel.arius.admin.persistence.mysql.template;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateAliasPO;

/**
 *
 *
 * @author d06679
 * @date 2018/6/11
 */
@Repository
public interface IndexTemplateAliasDAO {

    List<TemplateAliasPO> listAll();

    List<TemplateAliasPO> listByTemplateId(int logicId);

}
