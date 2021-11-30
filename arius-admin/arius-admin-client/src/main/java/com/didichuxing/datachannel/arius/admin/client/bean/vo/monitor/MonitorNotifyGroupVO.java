package com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "告警通知组")
public class MonitorNotifyGroupVO extends BaseVO {
    private Long id;

    private String name;

    private String comment;
}