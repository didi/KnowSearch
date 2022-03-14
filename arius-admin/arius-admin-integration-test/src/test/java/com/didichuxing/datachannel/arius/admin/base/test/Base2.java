package com.didichuxing.datachannel.arius.admin.base.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * @author cjm
 */
public class Base2 extends Base1 {

    @BeforeAll
    public static void preHandle() throws IOException {
        System.out.println("base2...preHandle");
    }

    /**
     * 在当前类中的所有测试方法之后执行
     * 本类所有方法测试完毕后，删除物理集群，删除逻辑集群
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        System.out.println("base2...afterCompletion");
    }
}
