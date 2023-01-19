package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.script.entity.Script;
import com.didiglobal.logi.op.manager.domain.script.repository.ScriptRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.ScriptPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.ScriptConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.ScriptDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-04 7:18 下午
 */
@Repository
public class ScriptRepositoryImpl implements ScriptRepository {

    @Autowired
    private ScriptDao scriptDao;

    @Override
    public Script findById(int id) {
        ScriptPO scriptPO = scriptDao.findById(id);
        return ScriptConverter.convertScriptPO2DO(scriptPO);
    }

    @Override
    public Script findByName(String name) {
        ScriptPO scriptPO = scriptDao.findByName(name);
        return ScriptConverter.convertScriptPO2DO(scriptPO);
    }

    @Override
    public List<Script> queryScript(Script script) {
        ScriptPO scriptPO = ScriptConverter.convertScriptDO2PO(script);
        return ScriptConverter.convertScriptPO2DOList(scriptDao.queryScript(scriptPO));
    }

    @Override
    public int updateScript(Script script) {
        ScriptPO scriptPO = ScriptConverter.convertScriptDO2PO(script);
        return scriptDao.update(scriptPO);
    }

    @Override
    public List<Script> pagingByCondition(Script script, Long page, Long size) {
        ScriptPO scriptPO = ScriptConverter.convertScriptDO2PO(script);
        return ScriptConverter.convertScriptPO2DOList(scriptDao.pagingByCondition(scriptPO,page,size));
    }

    @Override
    public Long countByCondition(Script script) {
        ScriptPO scriptPO = ScriptConverter.convertScriptDO2PO(script);
        return scriptDao.countByCondition(scriptPO);
    }

    @Override
    public int insertScript(Script script) {
        ScriptPO scriptPO = ScriptConverter.convertScriptDO2PO(script);
        scriptDao.insert(scriptPO);
        return scriptPO.getId();
    }

    @Override
    public int deleteScript(int id) {
        return scriptDao.delete(id);
    }
}
