package com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSrv extends BaseEntity {

    /**
     * 模板服务code
     */
    protected Integer srvCode;

    /**
     * 模板服务名称
     */
    protected String  srvName;



    public static List<TemplateSrv> codeStr2SrvList(String codeStr) {
        if (StringUtils.isBlank(codeStr)) {
            return new ArrayList<>();
        }

        List<TemplateSrv> srvList = new ArrayList<>();
        for (String srvId : StringUtils.split(codeStr, ",")) {
            TemplateSrv templateSrv = getSrv(Integer.parseInt(srvId));
            if (null != templateSrv) {
                srvList.add(templateSrv);
            }
        }

        return srvList;
    }

    public static TemplateSrv getSrv(Integer templateSrvCode) {
        TemplateServiceEnum srvEnum = TemplateServiceEnum.getById(templateSrvCode);
        return new TemplateSrv(srvEnum.getCode(), srvEnum.getServiceName());
    }
}