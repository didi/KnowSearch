package com.didi.arius.gateway.core.es.http.document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.IndexDateFieldException;
import com.didi.arius.gateway.common.exception.IndexNotPermittedException;
import com.didi.arius.gateway.common.exception.InvalidParameterException;
import com.didi.arius.gateway.common.exception.ServerBusyException;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.TemplateClusterInfo;
import com.didi.arius.gateway.common.utils.AppUtil;
import com.didi.arius.gateway.core.es.http.HttpRestHandler;
import com.didiglobal.knowframework.log.LogGather;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RestBaseWriteAction extends HttpRestHandler {
    protected static final Logger logger = LoggerFactory.getLogger(RestBaseWriteAction.class);

    protected static final int OPER_INDEX                   = 100001;
    protected static final int OPER_UPDATE                  = 100002;
    protected static final int OPER_APPEND                  = 100003;
    protected static final int OPER_APPEND_LIST             = 100004;
    protected static final int OPER_DELETE                  = 100005;
    protected static final int OPER_BULK                    = 100000;
    protected static final String WRITE_TIME_FIELD          = "es_index_time";
    protected static final List<String> timePatterns = Arrays.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss.SSS Z", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy/MM/dd HH:mm:ss", "epoch_second", "epoch_millis", "yyyy-MM-dd");

    protected static final long MILLIS_ZONE_OFFSET = LocalDateTime.of(1970, 1, 1, 0, 0, 0,
            0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    private LoadingCache<Long, Map<String, String>> dayFormatCache = CacheBuilder.newBuilder().concurrencyLevel(20).expireAfterWrite(5,
            TimeUnit.MINUTES).initialCapacity(60).maximumSize(100).recordStats().build(new CacheLoader<Long, Map<String, String>>() {

        @Override
        public Map<String, String> load(Long key) {
            return new ConcurrentHashMap<>();
        }
    });

    @Override
    public void handleRequest(QueryContext queryContext) throws Exception {
        RestRequest request = queryContext.getRequest();
        RestChannel channel = queryContext.getChannel();

        String[]  indicesArr = Strings.splitStringByCommaToArray(request.param("index"));
        List<String> indices = Lists.newArrayList(indicesArr);
        queryContext.setIndices(indices);

        if (isOriginCluster(queryContext)) {
            handleOriginClusterRequest(queryContext);
        } else {
            if (isNeededCheckIndices()) {
                checkWriteIndicesAndTemplateBlockWrite(queryContext);
            }

            handleInterRequest(queryContext, request, channel);
        }
    }

    protected abstract void handleInterRequest(QueryContext queryContext, RestRequest request, RestChannel channel) throws Exception;


    public   String getIndexName(IndexTemplate indexTemplate, Map<String, Object> source) {
        if (StringUtils.isBlank(indexTemplate.getDateFormat())) {
            String indexExpression = indexTemplate.getExpression();
            if (indexExpression.endsWith("*")) {
                indexExpression = indexExpression.substring(0, indexExpression.length()-1);
            }

            return getIndexVersionName(indexExpression, indexTemplate.getVersion());
        }

        Object timeObj = source != null ? source.get(indexTemplate.getDateField()) : null;
        long messageTime = 0;

        if (null != timeObj) {
            String timeValue = String.valueOf(timeObj);
            if (StringUtils.isNumeric(timeValue)) {
                if (timeValue.length() == 13) {
                    messageTime = Long.parseLong(timeValue);
                } else if (timeValue.length() == 10) {
                    timeObj = Long.parseLong(timeValue);
                    messageTime = ((Long) timeObj) * 1000;
                }
            } else {
                messageTime = getMessageTime(messageTime, timeValue);
            }

            if (messageTime == 0) {
                throw new IndexDateFieldException(String.format("index time format error,index=%s, time field=%s, time=%s", indexTemplate.getExpression(), indexTemplate.getDateField(), timeValue));
            }
        } else {
            throw new InvalidParameterException(String.format("index time field missing,index=%s, time field=%s", indexTemplate.getExpression(), indexTemplate.getDateField()));
        }

        return getIndexNameWithDate(indexTemplate, messageTime);
    }

    private long getMessageTime(long messageTime, String timeValue) {
        for (String timePattern : timePatterns) {
            try {
                messageTime = DateTime.parse(timeValue, DateTimeFormat.forPattern(timePattern)).getMillis();
                break;
            } catch (Exception e) {
                // pass
            }
        }
        return messageTime;
    }

    protected String getIndexNameWithDate(IndexTemplate indexTemplate, long time) {
        // 配置了过期时间字段，则需要判断是否过期，或者过于超前
        if (indexTemplate.getExpireTime() > 0 &&
                (time < System.currentTimeMillis() - indexTemplate.getExpireTime() * QueryConsts.DAY_MILLIS
                    || time > System.currentTimeMillis() + 2 * QueryConsts.DAY_MILLIS)) {
            throw new IndexDateFieldException(String.format("index time expire,index=%s, time=%d", indexTemplate.getExpression(), time));
        }

        // 需要校准时区之差对应的时间
        long key = (time - MILLIS_ZONE_OFFSET) / QueryConsts.DAY_MILLIS;
        String indexExpression = indexTemplate.getExpression();
        String indexNameDateTime = null;
        String dateFormat = indexTemplate.getDateFormat().replace('Y', 'y');

        try {
            // 从缓存中获取
            Map<String, String> format2DayValueMap = dayFormatCache.get(key);

            // 如果该时间在缓存中找不到，则计算, 这里不会执行到，因为在构建dayFormatCache设置了load回调来放入新的key
            if (null == format2DayValueMap) {

                format2DayValueMap = new ConcurrentHashMap<>();
                // 不包含该日期转换格式则计算，然后放入map中

                Instant instant = Instant.ofEpochMilli(time);
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                indexNameDateTime = DateTimeFormatter.ofPattern(dateFormat).format(dateTime);

                format2DayValueMap.put(dateFormat, indexNameDateTime);
                // 放入缓存中
                dayFormatCache.put(key, format2DayValueMap);
            } else {
                // 如果包含了该日期转换格式
                if (format2DayValueMap.containsKey(dateFormat)) {
                    indexNameDateTime = format2DayValueMap.get(dateFormat);

                    if (logger.isDebugEnabled()) {
                        logger.debug("getIndexNameWithDate||infoMsg=get time -> {}, key -> {} from cache, dateFormat -> {}, indexNameDateTime -> {}",
                                time, key, dateFormat, indexNameDateTime);
                    }
                } else {
                    // 不包含该日期转换格式则计算，然后放入map中
                    Instant instant = Instant.ofEpochMilli(time);
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                    indexNameDateTime = DateTimeFormatter.ofPattern(dateFormat).format(dateTime);

                    // 有效的才放入map中
                    format2DayValueMap.put(dateFormat, indexNameDateTime);
                }
            }
        } catch (Exception e) {
            LogGather.recordErrorLog("LoadingCacheField_" + indexExpression, "Failed to get key ", e);
        }

        // 如果日期值为空，则默认为当天
        if (StringUtils.isBlank(indexNameDateTime)) {
            LocalDateTime dateTime = LocalDateTime.now();
            // 根据时间格式转换为格式化后的时间字符串
            indexNameDateTime = DateTimeFormatter.ofPattern(dateFormat).format(dateTime);
        }

        if (indexExpression.endsWith("*")) {
            indexExpression = indexExpression.substring(0, indexExpression.length()-1);
        }

        return getIndexVersionName(indexExpression.concat(indexNameDateTime), indexTemplate.getVersion());
    }

    protected String getIndexVersionName(String index, int version) {
        if (version == 0) {
            return index;
        } else {
            return String.format("%s_v%d", index, version);
        }
    }

    protected IndexTemplate getAndCheckIndexTemplate(String index, QueryContext queryContext) {
        IndexTemplate indexTemplate = getTemplateByIndex(Arrays.asList(index), queryContext);
        if (indexTemplate == null) {
            if (queryContext.isFromKibana())  {
                // kibana索引写入admin集群
                indexTemplate = new IndexTemplate();
                indexTemplate.setExpression(index);
                indexTemplate.setExpireTime(-1);
                indexTemplate.setDeployStatus(IndexTemplate.DeployStatus.MASTER_ONLY);
                indexTemplate.setInternal(true);

                TemplateClusterInfo templateClusterInfo = new TemplateClusterInfo();
                templateClusterInfo.setCluster(esRestClientService.getAdminClient(actionName).getClusterName());
                indexTemplate.setMasterInfo(templateClusterInfo);
                return indexTemplate;
            } else if (AppUtil.isAdminAppid(queryContext.getAppDetail())) {
                indexTemplate = new IndexTemplate();
                indexTemplate.setExpression(index);
                indexTemplate.setExpireTime(-1);
                indexTemplate.setDeployStatus(IndexTemplate.DeployStatus.MASTER_ONLY);
                indexTemplate.setInternal(true);

                TemplateClusterInfo templateClusterInfo = new TemplateClusterInfo();
                templateClusterInfo.setCluster(queryContext.getAppDetail().getCluster());
                indexTemplate.setMasterInfo(templateClusterInfo);
                return indexTemplate;
            }

            indexTemplate = getTemplateByIndexTire(Lists.newArrayList(index), queryContext);
            if (indexTemplate == null) {
                throw new IndexNotPermittedException("appid=" + queryContext.getAppDetail().getId() + " don't have permission to write " + index);
            }
        }

        if (indexTemplate.getDeployStatus() == IndexTemplate.DeployStatus.NONE) {
            throw new ServerBusyException(String.format("es internal write %s busy, please wait...", indexTemplate.getName()));
        }

        return indexTemplate;
    }
}
