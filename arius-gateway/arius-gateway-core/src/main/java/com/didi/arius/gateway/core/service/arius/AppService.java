package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.BaseContext;

import java.util.List;
import java.util.Map;

public interface AppService {

    /**
     * 根据appid获取app详情
     * @param appid
     * @return
     */
    AppDetail getAppDetail(int appid);

    /**
     * 获取所有的app详情map
     * @return
     */
    Map<Integer, AppDetail> getAppDetails();

    /**
     * 通过ip获取app详情
     * @param ip
     * @return
     */
    AppDetail getAppDetailFromIp(String ip);

    /**
     * 更新app信息
     */
    void resetAppInfo();

    /**
     * 验证请求token信息
     * @param baseContext
     */
    void checkToken(BaseContext baseContext);

    /**
     * 检查索引是否有app授权的写索引权限
     * @param baseContext
     * @param indices
     */
    void checkWriteIndices(BaseContext baseContext, List<String> indices);

    /**
     * 检查索引是否有app授权的索引权限
     * @param baseContext
     * @param indices
     */
    void checkIndices(BaseContext baseContext, List<String> indices);
}
