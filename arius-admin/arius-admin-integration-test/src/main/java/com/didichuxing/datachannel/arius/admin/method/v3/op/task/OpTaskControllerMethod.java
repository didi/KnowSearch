package com.didichuxing.datachannel.arius.admin.method.v3.op.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.task.WorkTaskDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.task.TaskTypeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.task.WorkTaskVO;

import java.io.IOException;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author wuxuan
 * @Date 2022/3/30
 */
public class OpTaskControllerMethod {
    public static final String OpTask = V3_OP + "/worktask";

    public static Result<List<TaskTypeVO>> getOrderTypes() throws IOException {
        String path=String.format("%s/type-enums",OpTask);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<List<TaskTypeVO>>>(){});
    }
    //todo测试未成功
    public static Result<WorkTaskVO> submit(Integer type, WorkTaskDTO workTaskDTO) throws IOException {
        String path=String.format("%s/%d/submit",OpTask,type);
        return JSON.parseObject(AriusClient.put(path,workTaskDTO),new TypeReference<Result<WorkTaskVO>>(){});
    }
    //todo测试未成功
    public static Result<WorkTaskVO> getOrderDetail(Integer taskID) throws IOException{
        String path=String.format("%s/%d",OpTask,taskID);
        return JSON.parseObject(AriusClient.get(path),new TypeReference<Result<WorkTaskVO>>(){});
    }

    public static Result<List<WorkTaskVO>> getTaskList() throws IOException{
        String path=String.format("%s/tasks",OpTask);
        return JSON.parseObject(AriusClient.get(path),new TypeReference<Result<List<WorkTaskVO>>>(){});
    }
}
