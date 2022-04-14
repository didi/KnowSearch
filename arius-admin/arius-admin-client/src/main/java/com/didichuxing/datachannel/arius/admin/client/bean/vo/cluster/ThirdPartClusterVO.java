package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理集群信息")
public class ThirdPartClusterVO extends BaseVO {

    @ApiModelProperty("集群ID")
    private Integer              id;

    @ApiModelProperty("集群名字")
    private String               cluster;

    @ApiModelProperty("描述")
    private String               desc;

    @ApiModelProperty("tcp地址-读")
    private String               readAddress;

    @ApiModelProperty("tcp地址-写")
    private String               writeAddress;

    @ApiModelProperty("http地址-读")
    private String               httpAddress;

    @ApiModelProperty("http地址-写")
    private String               httpWriteAddress;

    /**
     * 集群类型
     * @see ESClusterTypeEnum
     */
    @ApiModelProperty("集群类型（0:数据集群；1:trib集群）")
    private Integer              type;

    @ApiModelProperty("数据中心")
    private String               dataCenter;

    @ApiModelProperty("机房")
    private String               idc;

    /**
     * @deprecated
     */
    @Deprecated
    @ApiModelProperty("业务分组")
    private Integer              bizGroup;

    @ApiModelProperty("服务等级")
    private Integer              level;

    @ApiModelProperty("es版本")
    private String               esVersion;

    @ApiModelProperty("集群密码")
    private String               password;

    @ApiModelProperty("集群插件")
    private Set<String>          plugins;

    @ApiModelProperty("tcp地址-读-map")
    private Map<String, Integer> readAddressMap;

    @ApiModelProperty("tcp地址-写-map")
    private Map<String, Integer> writeAddressMap;

    @ApiModelProperty("http地址-读-map")
    private Map<String, Integer> httpAddressMap;

    @ApiModelProperty("client运行模式（0：读写共享 1：读写分离）")
    private Integer             runMode;

    @ApiModelProperty("指定用写client的action")
    private String              writeAction;

    public void setReadAddress(String readAddress) {
        this.readAddress = readAddress;
        readAddressMap = toAddressMap(readAddress);
    }

    public void setWriteAddress(String writeAddress) {
        this.writeAddress = writeAddress;
        writeAddressMap = toAddressMap(writeAddress);
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
        httpAddressMap = toAddressMap(httpAddress);
    }

    private Map<String, Integer> toAddressMap(String addresses) {
        if (StringUtils.isBlank(addresses)) {
            return null;
        }
        String[] addressList = addresses.split(",");
        Map<String, Integer> addressMap = new HashMap<>();
        for (String address : addressList) {
            try {
                String[] hostAndPort = address.trim().split(":");
                addressMap.put(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
            } catch (Exception e) {
                return addressMap;
            }
        }
        return addressMap;
    }
}
