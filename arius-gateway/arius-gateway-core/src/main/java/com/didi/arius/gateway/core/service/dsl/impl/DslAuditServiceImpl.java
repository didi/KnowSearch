package com.didi.arius.gateway.core.service.dsl.impl;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.DslForbiddenException;
import com.didi.arius.gateway.common.exception.DslRateLimitException;
import com.didi.arius.gateway.common.metadata.BaseContext;
import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.common.metadata.JoinLogContext;
import com.didi.arius.gateway.common.utils.Convert;
import com.didi.arius.gateway.core.component.QueryConfig;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.core.service.dsl.DslAuditService;
import com.didiglobal.knowframework.dsl.parse.DslExtractionUtilV2;
import com.didiglobal.knowframework.dsl.parse.bean.ExtractResult;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fitz
 * @date 2021/5/25 7:40 下午
 */
@Service
@NoArgsConstructor
public class DslAuditServiceImpl implements DslAuditService {
    protected static final Logger logger = LoggerFactory.getLogger(DslAuditServiceImpl.class);

    @Autowired
    private DslTemplateService dslTemplateService;

    @Autowired
    private QueryConfig queryConfig;

    @Override
    public String auditDSL(BaseContext baseContext, BytesReference source, String[] indices) {
        String strSource = "";

        try {
            if (source != null && source.length() > 0) {
                strSource = XContentHelper.convertToJson(source, false);
            }

            return audit(baseContext, strSource);
        } catch (IOException e) {
            logger.error("dsl_audit_unexpect_error||appid={}||requestId={}||source={}||exception={}", baseContext.getAppid(),
                    baseContext.getRequestId(), source, Convert.logExceptionStack(e));
            return "";
        }
    }

    @Override
    public String auditSQL(BaseContext baseContext, String sql, String[] indices) {
        if (sql == null || sql.length() == 0) {
            return "";
        }

        return audit(baseContext, sql);
    }

    /************************************************************** private method **************************************************************/
    private String audit(BaseContext baseContext, String source) {
        ExtractResult extractResult = DslExtractionUtilV2.extractDsl(source);
        if ("FAILED".equals(extractResult.getDslTemplate())) {
            logger.error("extractDsl_failed||projectId={}||appid={}||requestId={}||source={}",
                    baseContext.getProjectId(), baseContext.getAppid(), baseContext.getRequestId(), source);
            return "";
        }

        String dslKey = baseContext.getProjectId() + "_" + extractResult.getDslTemplateMd5();
        baseContext.setDslTemplateKey(dslKey);

        if (baseContext.isDetailLog()) {
            JoinLogContext joinLogContext = baseContext.getJoinLogContext();
            joinLogContext.setDslTemplate(extractResult.getDslTemplate());
            joinLogContext.setSearchType(extractResult.getSearchType());
            joinLogContext.setDsl(extractResult.getDsl());
            joinLogContext.setDslType(extractResult.getDslType());
            joinLogContext.setDslTag(extractResult.getTags());
            joinLogContext.setDslTemplateMd5(extractResult.getDslTemplateMd5());
            joinLogContext.setSelectFields(extractResult.getSelectFields());
            joinLogContext.setWhereFields(extractResult.getWhereFields());
            joinLogContext.setGroupByFields(extractResult.getGroupByFields());
            joinLogContext.setSortByFields(extractResult.getSortByFields());
        }

        DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate(dslKey);
        if (dslTemplate == null) {
            dslTemplate = dslTemplateService.getNewDSLTemplate(dslKey);
            if (dslTemplate == null) {
                synchronized (this) {
                    dslTemplate = new DSLTemplate(queryConfig.getDslQPSLimit(), queryConfig.getDslQPSLimit(), false);
                    dslTemplateService.putNewDSLTemplate(dslKey, dslTemplate);
                }
            }
        }

        // 是否禁止访问
        if (dslTemplate.isQueryForbidden()) {
            logger.warn("dsl_forbidden||appid={}||requestId={}||dsl={}||dslTemplate={}", baseContext.getAppid(), baseContext.getRequestId(),
                    extractResult.getDslTemplateMd5(), dslTemplate);
            throw new DslForbiddenException("dangerous query dsl forbidden, dsl=" + extractResult.getDslTemplateMd5());
        }

        // 限流校验
        if (!dslTemplate.getRateLimiter().tryAcquire(1, QueryConsts.QUERY_DSL_LIMIT_WAIT, TimeUnit.MILLISECONDS)) {
            logger.warn("dsl_rateLimit||appid={}||requestId={}||dsl={}||dslTemplate={}", baseContext.getAppid(), baseContext.getRequestId(),
                    extractResult.getDslTemplateMd5(), dslTemplate);
            throw new DslRateLimitException("query dsl flow limit, please wait and try again!, dsl=" + extractResult.getDslTemplateMd5());
        }

        return extractResult.getDslTemplateMd5();
    }
}