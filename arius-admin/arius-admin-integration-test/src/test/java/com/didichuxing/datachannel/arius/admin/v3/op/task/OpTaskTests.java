package com.didichuxing.datachannel.arius.admin.v3.op.task;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.TaskTypeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.task.OpTaskControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * @author wuxuan
 * @Date 2022/3/30
 */
public class OpTaskTests extends BaseContextTest {

    @Test
    public void testGetOrderTypes() throws IOException{
        Result<List<TaskTypeVO>> result= OpTaskControllerMethod.getOrderTypes();
        Assert.assertTrue(result.success());
    }

    @Test
    public void testSubmit() throws IOException{
          OpTaskDTO opTaskDTO = CustomDataSource.getworkTaskDTO();
          Result<WorkTaskVO> result=OpTaskControllerMethod.submit(2, opTaskDTO);
          Assert.assertTrue(result.success());
    }

    @Test
    public void testGetOrderDetail() throws IOException{
        Integer taskID=new Integer(00);
        Result<WorkTaskVO> result=OpTaskControllerMethod.getOrderDetail(taskID);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testGetTaskList() throws IOException{
        Result<List<WorkTaskVO>>  result=OpTaskControllerMethod.getTaskList();
        Assert.assertTrue(result.success());
    }

}