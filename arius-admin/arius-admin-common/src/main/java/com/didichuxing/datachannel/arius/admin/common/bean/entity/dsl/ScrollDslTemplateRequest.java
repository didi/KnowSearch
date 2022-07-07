package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/22 下午4:31
 * @modified By D10865
 *
 * 滚动获取查询模板接口
 *
 */
@Data
@NoArgsConstructor
public class ScrollDslTemplateRequest {

    /**
     * 滚动查询大小
     */
    private Long scrollSize;
    /**
     * 查询模板版本号
     */
    private String dslTemplateVersion;
    /**
     * 上次修改时间
     */
    private Long lastModifyTime;
    /**
     * 滚动游标
     */
    private String scrollId;

    /**
     * 请求参数是否有效
     *
     * @return
     */
    @JSONField(serialize = false)
    public boolean isValid() {

        if (StringUtils.isBlank(this.dslTemplateVersion)) {
            return false;
        }

        if (this.lastModifyTime == null) {
            return false;
        }

        return this.scrollSize != null;
    }

}
