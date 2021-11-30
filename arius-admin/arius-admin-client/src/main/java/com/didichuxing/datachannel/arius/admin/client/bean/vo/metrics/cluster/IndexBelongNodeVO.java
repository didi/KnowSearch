package com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("索引归属节点信息")
public class IndexBelongNodeVO extends BaseVO {

    @ApiModelProperty("节点名称")
    private String node;
}
