package com.didichuxing.datachannel.arius.admin.metadata.service;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataType;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataTypeService {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    public static final String LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_UNKNOW_VALUE = "[\n" +
            "    {\n" +
            "        \"code\":-1,\n" +
            "        \"desc\":\"未知数据\",\n" +
            "        \"label\":\"\"\n" +
            "    }\n" +
            "]";

    public Map<Integer, String> code2DescMap() {
        List<DataType> dataTypeList = getDataTypeList();
        return dataTypeList.stream().collect(Collectors.toMap(DataType::getCode, DataType::getDesc));
    }

    private List<DataType> getDataTypeList() {

        List<DataType> dataTypeList = new ArrayList<>();
        try {
            //从平台配置中获取业务类型
            dataTypeList = JSON.parseArray(ariusConfigInfoService.stringSetting(AriusConfigConstant.ARIUS_TEMPLATE_GROUP, AriusConfigConstant.LOGIC_TEMPLATE_BUSINESS_TYPE_LIST,
                    AriusConfigConstant.LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_DEFAULT_VALUE), DataType.class);
        } catch (Exception e) {
            //若平台配置获取的业务类型转换失败，则返回未知业务类型
            dataTypeList = JSON.parseArray(LOGIC_TEMPLATE_BUSINESS_TYPE_LIST_UNKNOW_VALUE,DataType.class);
        }

        return dataTypeList;
    }

}
