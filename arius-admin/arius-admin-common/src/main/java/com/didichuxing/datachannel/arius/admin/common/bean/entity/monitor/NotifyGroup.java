package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengqiao
 * @date 20/7/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyGroup {
    private Long id;

    private String name;

    private String comment;
}