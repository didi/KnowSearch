package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import java.util.Map;

public interface DataTypeService {
    /**
     * 获得数据类型的代码映射描述的map
     * @param
     * @return Map<Integer, String>
     */
    Map<Integer, String> code2DescMap();

    /**
     * 根据数据类型的代码判断数据类型是否存在
     * @param code
     * @return boolean
     */
    boolean isExit(Integer code);

    /**
     * 根据数据类型的代码获得对应的数据类型描述
     * @param code
     * @return boolean
     */
    String descOfCode(Integer code);

}
