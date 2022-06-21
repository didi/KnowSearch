package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionDslResponse {

    public  static final String TIME_FORMAT = "yyyy-MM-dd-HH:mm:ss";
    private static final String NEW_FORMAT_STR = "new dsl, time:%s, count:%d";
    private static final String GROW_FORMAT_STR = "grow dsl, before:%d, after:%d, time:%s";

    private int projectId;

    private String templateMD5;

    private String reason;

    public static ExceptionDslResponse buildForNew(long timeStamp, long count) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);

        ExceptionDslResponse ret = new ExceptionDslResponse();
        ret.reason = String.format(NEW_FORMAT_STR, format.format(timeStamp), count);
        return ret;
    }

    public static ExceptionDslResponse buildForGrow(long timeStamp, long before, long after) {
        SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);

        ExceptionDslResponse ret = new ExceptionDslResponse();
        ret.reason = String.format(GROW_FORMAT_STR, before, after, format.format(timeStamp));
        return ret;
    }

}