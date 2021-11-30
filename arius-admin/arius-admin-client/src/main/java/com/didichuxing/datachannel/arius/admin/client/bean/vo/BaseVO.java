package com.didichuxing.datachannel.arius.admin.client.bean.vo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.ToString;

/**
 * @author d06679
 * @date 2019/3/13
 */
@ToString
@Data
public class BaseVO implements Serializable {
    private Date createTime;
    private Date updateTime;
}
