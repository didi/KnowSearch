package com.didichuxing.datachannel.arius.admin.biz.extend.foctory;

import com.didichuxing.datachannel.arius.admin.common.bean.common.GetTemplateLimitStrategyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateLimitStrategy;

/**
 * @author d06679
 * @date 2019-08-22
 */
public interface TemplateLimitStrategyProvider {

    /**
     * 扩展模板限流接口
     * @param cluster 集群
     * @param template 模板名字
     * @param interval 时间间隔
     * @param context 计算上下文
     * @return 限流策略
     */
    TemplateLimitStrategy provide(String cluster, String template, long interval,
                                  GetTemplateLimitStrategyContext context);

}
