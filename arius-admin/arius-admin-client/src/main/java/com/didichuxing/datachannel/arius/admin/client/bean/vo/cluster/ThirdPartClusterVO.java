package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
@ApiModel(description = "物理集群信息")
public class ThirdPartClusterVO extends BaseVO {

    /**
     * 集群id
     */
    @ApiModelProperty("集群ID")
    private Integer              id;

    /**
     * 集群名字
     */
    @ApiModelProperty("集群名字")
    private String               cluster;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String               desc;

    /**
     * 读地址
     */
    @ApiModelProperty("tcp地址-读")
    private String               readAddress;

    /**
     * 写地址
     */
    @ApiModelProperty("tcp地址-写")
    private String               writeAddress;

    /**
     * http地址
     */
    @ApiModelProperty("http地址-读")
    private String               httpAddress;

    /**
     * http写地址
     */
    @ApiModelProperty("http地址-写")
    private String               httpWriteAddress;

    /**
     * 集群类型
     * @see ESClusterTypeEnum
     */
    @ApiModelProperty("集群类型（0:数据集群；1:trib集群）")
    private Integer              type;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String               dataCenter;

    /**
     * 机房
     */
    @ApiModelProperty("机房")
    private String               idc;

    /**
     * 业务分组
     */
    @Deprecated
    @ApiModelProperty("业务分组")
    private Integer              bizGroup;

    /**
     * 服务等级
     */
    @ApiModelProperty("服务等级")
    private Integer              level;

    /**
     * es版本
     */
    @ApiModelProperty("es版本")
    private String               esVersion;

    /**
     * 集群密码
     */
    @ApiModelProperty("集群密码")
    private String               password;

    /**
     * 集群插件
     */
    @ApiModelProperty("集群插件")
    private Set<String>          plugins;

    @ApiModelProperty("tcp地址-读-map")
    private Map<String, Integer> readAddressMap;

    @ApiModelProperty("tcp地址-写-map")
    private Map<String, Integer> writeAddressMap;

    @ApiModelProperty("http地址-读-map")
    private Map<String, Integer> httpAddressMap;

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

            }
        }
        return addressMap;
    }
}
