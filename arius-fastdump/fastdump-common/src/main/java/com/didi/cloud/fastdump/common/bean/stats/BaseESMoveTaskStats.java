package com.didi.cloud.fastdump.common.bean.stats;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/5
 */
@Data
public abstract class BaseESMoveTaskStats extends BaseMoveTaskStats {
    /**
     * 获取主键key
     *
     * @return
     */
    public abstract String getKey();
}
