package com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataType;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.DataTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataTypeServiceImpl implements DataTypeService {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    public static final String LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_UNKNOW_VALUE = "[\n" +
            "    {\n" +
            "        \"code\":-1,\n" +
            "        \"desc\":\"未知数据\",\n" +
            "        \"label\":\"\"\n" +
            "    }\n" +
            "]";
    public static final String UNKNOW = "未知数据";

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
            return UNKNOW;
        }
        for (DataType dataType : getDataTypeList()) {
            if (dataType.getCode().equals(code)) {
                return dataType.getDesc();
            }
        }

        return UNKNOW;
    }

    private List<DataType> getDataTypeList() {

        List<DataType> dataTypeList = new ArrayList<>();
        try {
            //从平台配置中获取业务类型并转换
            dataTypeList = JSON.parseArray(ariusConfigInfoService.stringSetting(AriusConfigConstant.ARIUS_TEMPLATE_GROUP, AriusConfigConstant.LOGIC_TEMPLATE_BUSINESS_TYPE_LIST,
                    AriusConfigConstant.LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_DEFAULT_VALUE), DataType.class);
        } catch (Exception e) {
            //若平台配置获取的业务类型转换失败，则返回未知业务类型
            dataTypeList = JSON.parseArray(LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_UNKNOW_VALUE,DataType.class);
        }

        return dataTypeList;
    }
}
