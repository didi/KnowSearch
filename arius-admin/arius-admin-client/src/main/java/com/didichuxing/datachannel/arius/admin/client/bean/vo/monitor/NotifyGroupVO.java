package com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyGroupVO {
    private Long id;
    private Long appId;
    private String appName;
    private String name;
    private List<IdName> userList;
    private String comment;
    private String operator;
    private Integer status;
    private Date updateTime;

    @Data
    @AllArgsConstructor
    public static class IdName {
        Long id;
        String name;
    }
}
