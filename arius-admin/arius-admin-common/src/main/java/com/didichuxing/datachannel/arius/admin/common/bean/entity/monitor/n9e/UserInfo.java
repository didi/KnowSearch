package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author fitz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private int status;
//    private String contacts;//是个数组

}
