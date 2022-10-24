package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lyn on 2022/5/17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群带节点信息的Region实体")
public class ClusterRegionWithNodeInfoDTO extends ClusterRegionDTO {
    @ApiModelProperty("绑定节点id列表")
    private List<Integer> bindingNodeIds;

    @ApiModelProperty("解绑节点id列表")
    private List<Integer> unBindingNodeIds;
    
    private ClusterRegionWithNodeInfoDTO(ClusterRegionWithNodeInfoDTOBuilder clusterRegionWithNodeInfoDTOBuilder) {
        setId(clusterRegionWithNodeInfoDTOBuilder.id);
        setName(clusterRegionWithNodeInfoDTOBuilder.name);
        setLogicClusterIds(clusterRegionWithNodeInfoDTOBuilder.logicClusterIds);
        setPhyClusterName(clusterRegionWithNodeInfoDTOBuilder.phyClusterName);
        setConfig(clusterRegionWithNodeInfoDTOBuilder.config);
        setBindingNodeIds(clusterRegionWithNodeInfoDTOBuilder.bindingNodeIds);
        setUnBindingNodeIds(clusterRegionWithNodeInfoDTOBuilder.unBindingNodeIds);
    }
    
    public static final class ClusterRegionWithNodeInfoDTOBuilder {
        private Long          id;
        private String        name;
        private String        logicClusterIds;
        private String        phyClusterName;
        private String        config;
        private List<Integer> bindingNodeIds;
        private List<Integer> unBindingNodeIds;
        
        public ClusterRegionWithNodeInfoDTOBuilder() {
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder(ClusterRegionWithNodeInfoDTO copy) {
            this.id = copy.getId();
            this.name = copy.getName();
            this.logicClusterIds = copy.getLogicClusterIds();
            this.phyClusterName = copy.getPhyClusterName();
            this.config = copy.getConfig();
            this.bindingNodeIds = copy.getBindingNodeIds();
            this.unBindingNodeIds = copy.getUnBindingNodeIds();
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withId(Long val) {
            id = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withName(String val) {
            name = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withLogicClusterIds(String val) {
            logicClusterIds = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withPhyClusterName(String val) {
            phyClusterName = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withConfig(String val) {
            config = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withBindingNodeIds(List<Integer> val) {
            bindingNodeIds = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTOBuilder withUnBindingNodeIds(List<Integer> val) {
            unBindingNodeIds = val;
            return this;
        }
        
        public ClusterRegionWithNodeInfoDTO build() {
            return new ClusterRegionWithNodeInfoDTO(this);
        }
    }
}