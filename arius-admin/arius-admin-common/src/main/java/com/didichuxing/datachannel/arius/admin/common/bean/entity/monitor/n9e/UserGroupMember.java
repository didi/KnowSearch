package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupMember {

    @JSONField(name = "user_group")
    private UserGroup userGroup;
    private List<UserInfo> users;
}
