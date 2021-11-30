package com.didi.arius.gateway.core.service.arius;

import com.didi.arius.gateway.common.enums.TemplateBlockTypeEnum;
import com.didi.arius.gateway.common.exception.TooManyIndexException;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.TemplateInfo;
import org.elasticsearch.index.IndexNotFoundException;

import java.util.List;
import java.util.Map;

public interface IndexTemplateService {
    /**
     * 获取索引模版表达式map
     */
    Map<String, Map<String, TemplateInfo>> getTemplateExpressionMap();

    /**
     * 获取索引模版别名map
     */
    Map<String, Map<String, TemplateInfo>> getTemplateAliasMap();

    /**
     * 根据名字获取索引模版
     */
    IndexTemplate getIndexTemplate(String template);

    /**
     * 根据index名字搜索索引模版
     */
    IndexTemplate getIndexTemplateByTire(String index) throws TooManyIndexException ;

    /**
     * 获取所有索引模版map
     */
    Map<String, IndexTemplate> getIndexTemplateMap();

    /**
     * 更新各集群模板、大基数mapping等信息
     * 更新逻辑索引模板列表
     * 更新索引别名
     */
    void resetIndexTemplateInfo();

    /**
     * 获取索引别名
     */
    String getIndexAlias(String index);

    /**
     * 判断索引是否符合索引表达式
     * @param index
     * @param indexExps
     * @return
     */
    boolean checkIndex(String index, List<String> indexExps);

    /**
     *
     * 根据cluster, index获取索引的版本号
     * @param indexName
     * @param cluster
     * @return
     */
    int getIndexVersion(String indexName, String cluster);

    /**
     * 获取索引模版
     * @param indices
     * @param queryContext
     * @return
     * @throws IndexNotFoundException
     * @throws TooManyIndexException
     */
    IndexTemplate getTemplateByIndexTire(List<String> indices, QueryContext queryContext) throws IndexNotFoundException, TooManyIndexException;

    /**
     *  增加模板别名
     * @param appid
     * @param templateId
     * @param templateName
     * @param aliasName
     * @return
     */
    public boolean addTemplateAlias(int appid, int templateId, String templateName, String aliasName);

    /**
     * 删除模板别名
     * @param appid
     * @param templateId
     * @param templateName
     * @param aliasName
     * @return
     */
    public boolean delTemplateAlias(int appid, int templateId, String templateName, String aliasName);

    /**
     * 校验模板存在
     * @param indices
     * @throws Exception
     */
    public void checkTemplateExist(List<String> indices);

    /**
     * 检查模板是否block
     * @param indices 索引
     * @param appDetail app
     * @param blockTypeEnum 类型
     */
    public void checkTemplateBlock(List<String> indices, AppDetail appDetail, TemplateBlockTypeEnum blockTypeEnum);
}
