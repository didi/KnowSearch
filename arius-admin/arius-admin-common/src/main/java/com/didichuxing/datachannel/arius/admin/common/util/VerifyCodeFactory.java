package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

/**
 * @author d06679
 * @date 2019/4/18
 */
public class VerifyCodeFactory {

    private static final String  CHAR_SET = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static List<String> charList;

    static {
        charList = Lists.newArrayList();
        for (char c : CHAR_SET.toCharArray()) {
            charList.add(String.valueOf(c));
        }
    }

    public static String get(int length) {
        Collections.shuffle(charList);
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(charList.size());
            sb.append(charList.get(number));
        }
        return sb.toString();
    }

}
