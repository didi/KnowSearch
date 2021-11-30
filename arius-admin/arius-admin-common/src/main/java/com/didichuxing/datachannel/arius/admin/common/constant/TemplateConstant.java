package com.didichuxing.datachannel.arius.admin.common.constant;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by linyunan on 2021-10-18
 */

public class TemplateConstant {

    public static final Set<Character> TEMPLATE_NAME_CHAR_SET            = Sets.newHashSet('a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1',
        '2', '3', '4', '5', '6', '7', '8', '9', '-', '_', '.');

    public static final Integer        TEMPLATE_NAME_SIZE_MIN            = 4;
    public static final Integer        TEMPLATE_NAME_SIZE_MAX            = 128;
    public static final Integer        TEMPLATE_SAVE_BY_DAY_EXPIRE_MAX   = 180;
    public static final Integer        TEMPLATE_SAVE_BY_MONTH_EXPIRE_MIN = 30;

    public static final String         TEMPLATE_NOT_EXIST                = "模板不存在";
}
