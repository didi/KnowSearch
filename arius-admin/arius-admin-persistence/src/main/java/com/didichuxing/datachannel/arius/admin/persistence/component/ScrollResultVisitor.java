package com.didichuxing.datachannel.arius.admin.persistence.component;

import java.util.List;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/30 下午5:59
 * @Modified By
 *
 * 遍历scroll结果
 */
public interface ScrollResultVisitor<T> {

    /**
     * 处理scroll结果
     *
     * @return
     */
    void handleScrollResult(List<T> resultList);

}
