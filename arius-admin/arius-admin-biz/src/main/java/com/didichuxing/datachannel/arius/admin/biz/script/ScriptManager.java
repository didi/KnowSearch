package com.didichuxing.datachannel.arius.admin.biz.script;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptAddDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.script.ScriptUpdateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptNameListVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptPageVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.script.ScriptQueryVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;

public interface ScriptManager {
    /**
     * 分页查询脚本中心
     * @param conditionDTO
     * @param projectId
     * @return
     */
    PaginationResult<ScriptPageVO> pageGetScripts(ScriptQueryDTO conditionDTO, Integer projectId) throws NotFindSubclassException;

    /**
     * 获取脚本名称列表
     * @return
     */
    Result<List<ScriptNameListVO>> listScriptName();

    /**
     * 根据脚本id查询脚本
     * @param scriptId
     * @return
     */
    Result<ScriptQueryVO> getScriptByScriptId(Long scriptId);

    /**
     * 新增脚本
     * @param scriptAddDTO
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> addScript(ScriptAddDTO scriptAddDTO, String operator, Integer projectId);

    /**
     * 修改脚本
     * @param scriptUpdateDTO
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> editScript(ScriptUpdateDTO scriptUpdateDTO, String operator, Integer projectId);

    /**
     * 删除脚本
     * @param id
     * @param operator
     * @param projectId
     * @return
     */
    Result<Long> deleteScript(Long id, String operator, Integer projectId);

    /**
     * 脚本是否在使用
     * @param id
     * @param operator
     * @param projectId
     * @return
     */
    Result<Boolean> usingScript(Long id, String operator, Integer projectId);
}
