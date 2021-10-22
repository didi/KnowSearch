package com.didi.arius.gateway.dsl;

import com.didi.arius.gateway.dsl.bean.ExtractResult;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/11/6 上午9:58
 * @Modified By
 *
 * 查询模板提取工具
 */
public class DslExtractionUtil {

    private static final ILog LOGGER = LogFactory.getLog(DslExtractionUtil.class);

    /**
     * 提取dsl语句成查询模板，包含多个版本号的MD5
     *
     * @param dslContent
     * @return
     */
    public static ExtractResult extractDsl(String dslContent) {
        // 使用第一版本查询模板提取
        //ExtractResult extractResultV1 = DslExtractionUtilV1.extractDsl(dslContent);
        // 使用第二版本查询模板提取
        ExtractResult extractResultV2 = DslExtractionUtilV2.extractDsl(dslContent);

        return mergeExtractResult(dslContent, null, extractResultV2);
    }

    /**
     * 合并不同查询模板提取sdk版本的结果
     *
     * @param extractResultV1
     * @param extractResultV2
     * @return
     */
    private static ExtractResult mergeExtractResult(String dslContent, ExtractResult extractResultV1, ExtractResult extractResultV2) {
        ExtractResult extractResult = null;

        if (extractResultV2 != null) {
            extractResult = extractResultV2;

            if (null == extractResultV1) {
                // LOGGER.error("mergeExtractResult extractResultV1 is null, dsl {}", dslContent);
                return extractResult;
            }

            extractResult.setNewDslTemplate(extractResultV2.getDslTemplate());
            extractResult.setDslTemplate(extractResultV1.getDslTemplate());

            String oldDslTemplateMd5 = extractResultV1.getDslTemplateMd5();
            String newDslTemplateMd5 = extractResultV2.getDslTemplateMd5();

            // 合并查询模板MD5包含版本号
            String dslTemplateMd5 = String.format("%s,%s", oldDslTemplateMd5, newDslTemplateMd5);
            extractResult.setDslTemplateMd5(dslTemplateMd5);
        } else {
            extractResult = extractResultV1;
            LOGGER.error("mergeExtractResult extractResultV2 is null, dsl {}", dslContent);
        }

        return extractResult;
    }

}
