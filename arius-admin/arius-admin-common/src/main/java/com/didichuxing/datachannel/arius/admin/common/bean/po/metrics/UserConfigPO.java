package com.didichuxing.datachannel.arius.admin.common.bean.po.metrics;

import com.baomidou.mybatisplus.annotation.TableName;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户指标配置po
 *
 * @author shizeying
 * @date 2022/05/24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_config_info")
public class UserConfigPO extends BasePO {
    /**
     * id自增
     */
    private Integer id;

    /**
     * 用户名
     */
    private String  userName;

    /**
     * 指标看板的配置
     */
    private String  configInfo;

    /**
     * 应用ID
     */
    private Integer projectId;

    /**
     * 配置类型
     */
    private Integer configType;
}