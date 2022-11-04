package com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataType;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.DataTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.UNKNOW_DATA_TYPE;

@Service
public class DataTypeServiceImpl implements DataTypeService {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    private List<DataType>         defaultDataTypeList;

    @PostConstruct
    private List<DataType> getDefaultDataType() {
        defaultDataTypeList = new ArrayList<>();
        defaultDataTypeList.add(new DataType(0,"系统日志","system"));
        defaultDataTypeList.add(new DataType(1,"日志数据","log"));
        defaultDataTypeList.add(new DataType(2,"用户上报数据","olap"));
        defaultDataTypeList.add(new DataType(3,"RDS数据","binlog"));
        defaultDataTypeList.add(new DataType(4,"离线导入数据","offline"));
        return defaultDataTypeList;
    }

    @Override
    public Map<Integer, String> code2DescMap() {
        List<DataType> dataTypeList = getDataTypeList();
        return dataTypeList.stream().collect(Collectors.toMap(DataType::getCode, DataType::getDesc));
    }

    @Override
    public boolean isExit(Integer code) {
        if (code == null) {
            return false;
        }
        for (DataType dataType : getDataTypeList()) {
            if (dataType.getCode().equals(code) ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String descOfCode(Integer code) {
        if (code == null) {
            return UNKNOW_DATA_TYPE.getDesc();
        }
        for (DataType dataType : getDataTypeList()) {
            if (dataType.getCode().equals(code)) {
                return dataType.getDesc();
            }
        }

        return UNKNOW_DATA_TYPE.getDesc();
    }

    private List<DataType> getDataTypeList() {

        List<DataType> dataTypeList = new ArrayList<>();
        try {
            //从平台配置中获取业务类型并转换
            String defaultValue = JSON.toJSONString(defaultDataTypeList);
            dataTypeList = JSON.parseArray(ariusConfigInfoService.stringSetting(AriusConfigConstant.ARIUS_TEMPLATE_GROUP, AriusConfigConstant.LOGIC_TEMPLATE_BUSINESS_TYPE_LIST,
                    defaultValue), DataType.class);
        } catch (Exception e) {
            //若平台配置获取的业务类型转换失败，则返回未知业务类型
            dataTypeList.add(UNKNOW_DATA_TYPE);
        }

        return dataTypeList;
    }
}
