package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.operaterecord;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

/**
 * @author d06679
 * @date 2017/10/9
 */
@RestController
@RequestMapping(V2_OP + "/record")
@Api(tags = "运维操作记录接口(REST)")
public class OperateRecordController {

    private static final int     MAX_RECORD_COUNT = 200;

    @Autowired
    private OperateRecordService operateRecordService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "查询操作记录接口", notes = "")
    public Result<List<OperateRecordVO>> list(@RequestBody OperateRecordDTO query) {
        return listInner(query);
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询操作记录接口", notes = "")
    public Result<List<OperateRecordVO>> list1(@RequestParam OperateRecordDTO query) {
        return listInner(query);
    }

    @GetMapping("/listModules")
    @ResponseBody
    @ApiOperation(value = "获取所有模块", notes = "")
    public Result<List<Map<String, Object>>> listModules() {
        return Result.buildSucc(ModuleEnum.getAllAriusConfigs());
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "保存操作记录接口", notes = "")
    public Result<Void> add(@RequestBody OperateRecordDTO param) {
        return operateRecordService.save(param);
    }

    /**************************************** private method ****************************************************/
    private void fillVOField(List<OperateRecordVO> records) {
        if(CollectionUtils.isEmpty(records)){return;}

        for(OperateRecordVO vo : records){
            vo.setModule(ModuleEnum.valueOf(vo.getModuleId()).getDesc());
            vo.setOperate(OperationEnum.valueOf(vo.getOperateId()).getDesc());
        }
    }

    public Result<List<OperateRecordVO>> listInner(@RequestParam OperateRecordDTO query) {
        List<OperateRecordVO> records = ConvertUtil.list2List(operateRecordService.list(query).getData(), OperateRecordVO.class);

        if (records.size() > MAX_RECORD_COUNT) {
            records = records.subList(0, MAX_RECORD_COUNT);
        }

        fillVOField(records);
        return Result.buildSucc(records);
    }

}
