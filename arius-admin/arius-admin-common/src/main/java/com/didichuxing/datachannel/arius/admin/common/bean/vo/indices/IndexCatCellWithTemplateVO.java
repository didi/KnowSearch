package com.didichuxing.datachannel.arius.admin.common.bean.vo.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关联模板的索引信息
 *
 * @author shizeying
 * @date 2022/08/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "IndexCatCellWithTemplateVO", description = "关联模板的索引信息")
public class IndexCatCellWithTemplateVO extends BaseVO {
    

    @ApiModelProperty("索引名字")
    private String       index;


    @ApiModelProperty("分区存储大小")
    private String       priStoreSize;

}