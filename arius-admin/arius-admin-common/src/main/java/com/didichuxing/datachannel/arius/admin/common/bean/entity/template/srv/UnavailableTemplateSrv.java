package com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/20
 */
@Data
public class UnavailableTemplateSrv extends TemplateSrv {

    /**
     * 不可用原因
     */
    private String unavailableReason;

    public UnavailableTemplateSrv(Integer srvCode, String srvName, String esVersion, String unavailableReason) {
        super(srvCode, srvName, esVersion);
        this.unavailableReason = unavailableReason;
    }

}
