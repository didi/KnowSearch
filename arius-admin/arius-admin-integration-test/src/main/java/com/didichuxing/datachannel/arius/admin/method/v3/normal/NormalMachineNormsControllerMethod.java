package com.didichuxing.datachannel.arius.admin.method.v3.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.user.AriusUserInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

/**
 * @author wuxuan
 * @Date 2022/3/31
 */
public class NormalMachineNormsControllerMethod {
    public static final String MachineNorms = V3_NORMAL + "/ecm/machineNorms";

    public static Result<List<ESMachineNormsPO>> listMachineNorms(String type) throws IOException {
        String path=String.format("%s/list", MachineNorms);
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        return JSON.parseObject(AriusClient.get(path,params), new TypeReference<Result<List<ESMachineNormsPO>>>(){});
    }

    public static Result<ESMachineNormsPO> machineNormsDetail(Long id) throws IOException{
        return Result.buildSucc();
    }
 }
