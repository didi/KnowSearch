
package com.didi.arius.gateway.common.metadata;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class IndexTemplate {

    /**
     * 逻辑模板ID
     */
    private Integer id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 时间字段
     */
    private String dateField;

    /**
     * 时间格式
     */
    private String dateFormat;

    /**
     * 过期天数，-1为不过期
     */
    private long expireTime;

    /**
     * 索引表达式
     */
    private String expression;

    /**
     * 是否用默认路由
     */
    private Boolean isDefaultRouting;

    /**
     * 索引模板版本号
     */
    private int version;

    /**
     * 索引主从类型
     */
    private DeployStatus deployStatus;

    /**
     * 别名列表
     */
    private List<String> aliases;

    /**
     * 是否禁读
     */
    private Boolean blockRead;

    /**
     * 是否禁写
     */
    private Boolean blockWrite;

    /**
     * master模板信息
     */
    private TemplateClusterInfo masterInfo;

    /**
     * slave模板信息
     */
    private List<TemplateClusterInfo> slaveInfos;

    private boolean internal = false;

    private String ingestPipeline;

    public enum DeployStatus {
        MASTER_AND_SLAVE(1),
        NONE(2),
        MASTER_ONLY(3);

        int type;

        private DeployStatus(int type) {
            this.type = type;
        }

        public static DeployStatus integerToStatus(int code) {
            for (DeployStatus status : DeployStatus.values()) {
                if (status.type == code) {
                    return status;
                }
            }
            return MASTER_AND_SLAVE;
        }
    }
}
