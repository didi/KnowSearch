package com.didi.cloud.fastdump.common.bean.stats;

import lombok.Data;

import java.util.List;

/**
 * Created by linyunan on 2022/9/7
 */
@Data
public class FailedShardInfoStatus {
    private String       ip;
    private List<String> FailedShardDataPaths;

    private String       detail;
}
