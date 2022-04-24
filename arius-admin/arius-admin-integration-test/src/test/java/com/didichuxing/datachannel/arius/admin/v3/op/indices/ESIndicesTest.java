package com.didichuxing.datachannel.arius.admin.v3.op.indices;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.base.BaseTemplateInfoTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.indices.IndicesOpenOrCloseDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.indices.IndexShardInfoVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.indices.ESIndicesControllerMethod;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengxiang
 */
public class ESIndicesTest extends BasePhyClusterInfoTest{

    @Test
    public void pageGetIndexCatInfoVOTest() throws IOException {
        IndicesConditionDTO indicesConditionDTO = new IndicesConditionDTO();
        indicesConditionDTO.setClusterPhyName(Lists.newArrayList(phyClusterInfo.getPhyClusterName()));
        indicesConditionDTO.setOrderByDesc(false);
        indicesConditionDTO.setPage(1L);
        indicesConditionDTO.setSize(10L);

        PaginationResult<IndexCatCellVO> result = ESIndicesControllerMethod.pageGetIndexCatInfoVO(indicesConditionDTO);
        Assert.assertTrue(result.success());
    }

    @Test
    public void getIndexCatInfoVOTest() throws IOException {
        PaginationResult<IndexCatCellVO> result = ESIndicesControllerMethod.getIndexCatInfoVO(phyClusterInfo.getPhyClusterName(),
                "test_index");
        Assert.assertTrue(result.success());
    }

    @Test
    public void deleteTest() throws IOException {
        List<IndicesClearDTO> params = new ArrayList<>();
        IndicesClearDTO index = new IndicesClearDTO();
        index.setClusterPhyName(phyClusterInfo.getPhyClusterName());
        index.setIndex("test_index");
        params.add(index);

        Result<Boolean> deleteResult = ESIndicesControllerMethod.delete(params);
        Assert.assertTrue(deleteResult.success());
    }


    @Test
    public void openAndCloseTest() throws IOException {
        List<IndicesOpenOrCloseDTO> params = new ArrayList<>();
        IndicesOpenOrCloseDTO index = new IndicesOpenOrCloseDTO();
        index.setClusterPhyName(phyClusterInfo.getPhyClusterName());
        index.setIndex("test_index");
        params.add(index);

        Result<Boolean> openResult = ESIndicesControllerMethod.open(params);
        Assert.assertTrue(openResult.success());

        Result<Boolean> closeResult = ESIndicesControllerMethod.close(params);
        Assert.assertTrue(closeResult.success());
    }

    @Test
    public void editIndexBlockSettingTest() throws IOException {
        List<IndicesBlockSettingDTO> params = new ArrayList<>();
        IndicesBlockSettingDTO index = new IndicesBlockSettingDTO();
        index.setClusterPhyName(phyClusterInfo.getPhyClusterName());
        index.setIndex("test_index");
        index.setType("1");
        index.setValue(Boolean.TRUE);
        params.add(index);

        Result<Boolean> result = ESIndicesControllerMethod.editIndexBlockSetting(params);
        Assert.assertTrue(result.success());
    }

    @Test
    public void getIndexShardTest() throws IOException {
        Result<List<IndexShardInfoVO>> result = ESIndicesControllerMethod.getIndexShard(phyClusterInfo.getPhyClusterName(),
                "test_index");
        Assert.assertTrue(result.success());
    }

    @Test
    public void mappingTest() throws IOException {
        Result<IndexMappingVO> result = ESIndicesControllerMethod.mapping(phyClusterInfo.getPhyClusterName(),
                "test_index");
        Assert.assertTrue(result.success());
    }

    @Test
    public void settingTest() throws IOException {
        Result<IndexSettingVO> result = ESIndicesControllerMethod.setting(phyClusterInfo.getPhyClusterName(),
                "test_index");
        Assert.assertTrue(result.success());
    }

}
