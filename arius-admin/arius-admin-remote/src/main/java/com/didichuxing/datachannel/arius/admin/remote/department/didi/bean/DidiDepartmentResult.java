package com.didichuxing.datachannel.arius.admin.remote.department.didi.bean;

import lombok.Data;

@Data
public class DidiDepartmentResult<T> {
    private T                 data;

    private boolean           success;
}
