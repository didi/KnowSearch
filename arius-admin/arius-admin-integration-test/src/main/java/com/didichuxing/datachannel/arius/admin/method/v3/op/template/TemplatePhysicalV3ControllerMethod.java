package com.didichuxing.datachannel.arius.admin.method.v3.op.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.IndexTemplatePhysicalVO;

import java.io.IOException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class TemplatePhysicalV3ControllerMethod {

    public static final String TEMPLATE_PHYSICAL = V3_OP + "/template/physical";

    public static Result<List<IndexTemplatePhysicalVO>> list(Integer logicId) throws IOException {
        String path = String.format("%s/%d", TEMPLATE_PHYSICAL, logicId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<IndexTemplatePhysicalVO>>>(){});
    }

    public static Result<List<String>> listTemplatePhyNames() throws IOException {
        String path = String.format("%s/listNames", TEMPLATE_PHYSICAL);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<List<String>> getAppNodeNames(Long templatePhyId) throws IOException {
        String path = String.format("%s/%d/copyClusterPhyNames", TEMPLATE_PHYSICAL, templatePhyId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<String>>>(){});
    }

    public static Result<Boolean> multipleEdit(List<IndexTemplatePhysicalDTO> params) throws IOException {
        String path = String.format("%s/multipleEdit", TEMPLATE_PHYSICAL);
        return JSON.parseObject(AriusClient.put(path, params), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> multipleUpgrade(List<TemplatePhysicalUpgradeDTO> params) throws IOException {
        String path = String.format("%s/multipleUpgrade", TEMPLATE_PHYSICAL);
        return JSON.parseObject(AriusClient.post(path, params), new TypeReference<Result<Boolean>>(){});
    }
}
