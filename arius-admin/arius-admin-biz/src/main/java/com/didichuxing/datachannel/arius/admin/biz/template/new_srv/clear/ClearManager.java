package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.clear;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
public interface ClearManager {

    /**
     * 清除索引
     * @param clearDTO
     * @return
     */
    Result<Void> clearIndices(TemplateClearDTO clearDTO);
}
