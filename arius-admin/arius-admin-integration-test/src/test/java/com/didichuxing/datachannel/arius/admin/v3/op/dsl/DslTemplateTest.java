package com.didichuxing.datachannel.arius.admin.v3.op.dsl;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.dsl.DslTemplateControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cjm
 */
public class DslTemplateTest extends BaseContextTest {

    @Test
    public void pageTest() throws IOException {
        DslTemplateConditionDTO dto = CustomDataSource.getDslTemplateConditionDTO();
        PaginationResult<DslTemplateVO> result = DslTemplateControllerMethod.page(dto);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getDetailTemplateTest() throws IOException {
        DslTemplateConditionDTO dto = CustomDataSource.getDslTemplateConditionDTO();
        PaginationResult<DslTemplateVO> result = DslTemplateControllerMethod.page(dto);
        Assertions.assertTrue(result.success());
        if(!result.getData().getBizData().isEmpty()) {
            Result<DslTemplateVO> result2 =
                    DslTemplateControllerMethod.getDetailTemplate(result.getData().getBizData().get(0).getDslTemplateMd5());
            Assertions.assertTrue(result2.success());
        }
    }

    @Test
    public void changeStatusTest() throws IOException {
        DslTemplateConditionDTO dto = CustomDataSource.getDslTemplateConditionDTO();
        PaginationResult<DslTemplateVO> result = DslTemplateControllerMethod.page(dto);
        Assertions.assertTrue(result.success());
        if(!result.getData().getBizData().isEmpty()) {
            String dslTemplateMd5 = result.getData().getBizData().get(0).getDslTemplateMd5();
            Result<DslTemplateVO> result2 = DslTemplateControllerMethod.getDetailTemplate(dslTemplateMd5);
            Result<Boolean> result3 = DslTemplateControllerMethod.changeStatus(dslTemplateMd5);
            Assertions.assertTrue(result3.success());
            Result<DslTemplateVO> result4 = DslTemplateControllerMethod.getDetailTemplate(dslTemplateMd5);
            Assertions.assertEquals(result4.getData().getEnable(), !result2.getData().getEnable());
            // 还原状态
            Result<Boolean> result5 = DslTemplateControllerMethod.changeStatus(dslTemplateMd5);
            Assertions.assertTrue(result5.success());
        }
    }

    @Test
    public void updateQueryLimitTest() throws IOException {
        DslTemplateConditionDTO dto = CustomDataSource.getDslTemplateConditionDTO();
        PaginationResult<DslTemplateVO> result = DslTemplateControllerMethod.page(dto);
        Assertions.assertTrue(result.success());
        if(!result.getData().getBizData().isEmpty()) {
            String dslTemplateMd5 = result.getData().getBizData().get(0).getDslTemplateMd5();
            Result<DslTemplateVO> result2 = DslTemplateControllerMethod.getDetailTemplate(dslTemplateMd5);
            List<String> dslTemplateMd5List = new ArrayList<>();
            dslTemplateMd5List.add(dslTemplateMd5);
            double oldQueryLimit = result2.getData().getQueryLimit();
            double newQueryLimit = oldQueryLimit + 10d;
            Result<Boolean> result3 = DslTemplateControllerMethod.updateQueryLimit(dslTemplateMd5List, newQueryLimit);
            Assertions.assertTrue(result3.success());
            Result<DslTemplateVO> result4 = DslTemplateControllerMethod.getDetailTemplate(dslTemplateMd5);
            Assertions.assertEquals(result4.getData().getQueryLimit(), newQueryLimit);
            // 还原限流值
            Result<Boolean> result5 = DslTemplateControllerMethod.updateQueryLimit(dslTemplateMd5List, oldQueryLimit);
            Assertions.assertTrue(result5.success());
        }
    }

    @Test
    public void slowListTest() throws IOException {
        GatewayJoinQueryDTO queryDTO = CustomDataSource.getGatewayJoinQueryDTO();
        Result<List<GatewayJoinVO>> result = DslTemplateControllerMethod.slowList(queryDTO);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void errorListTest() throws IOException {
        GatewayJoinQueryDTO queryDTO = CustomDataSource.getGatewayJoinQueryDTO();
        Result<List<GatewayJoinVO>> result = DslTemplateControllerMethod.errorList(queryDTO);
        Assertions.assertTrue(result.success());
    }
}
