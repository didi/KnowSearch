package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linyunan
 * @date 2021-03-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "带有region信息的逻辑集群")
public class ESLogicClusterWithRegionDTO extends ESLogicClusterDTO {

    @ApiModelProperty("集群Region")
    private List<ClusterRegionDTO> clusterRegionDTOS;
    
    private ESLogicClusterWithRegionDTO(ESLogicClusterWithRegionDTOBuilder ESLogicClusterWithRegionDTOBuilder) {
        setId(ESLogicClusterWithRegionDTOBuilder.id);
        setName(ESLogicClusterWithRegionDTOBuilder.name);
        setType(ESLogicClusterWithRegionDTOBuilder.type);
        setProjectId(ESLogicClusterWithRegionDTOBuilder.projectId);
        setDataCenter(ESLogicClusterWithRegionDTOBuilder.dataCenter);
        setDataNodeNum(ESLogicClusterWithRegionDTOBuilder.dataNodeNum);
        setMemo(ESLogicClusterWithRegionDTOBuilder.memo);
        setLevel(ESLogicClusterWithRegionDTOBuilder.level);
        setQuota(ESLogicClusterWithRegionDTOBuilder.quota);
        setConfigJson(ESLogicClusterWithRegionDTOBuilder.configJson);
        setHealth(ESLogicClusterWithRegionDTOBuilder.health);
        setDataNodeSpec(ESLogicClusterWithRegionDTOBuilder.dataNodeSpec);
        setDiskUsagePercent(ESLogicClusterWithRegionDTOBuilder.diskUsagePercent);
        setDiskTotal(ESLogicClusterWithRegionDTOBuilder.diskTotal);
        setDiskUsage(ESLogicClusterWithRegionDTOBuilder.diskUsage);
        setEsClusterVersion(ESLogicClusterWithRegionDTOBuilder.esClusterVersion);
        setClusterRegionDTOS(ESLogicClusterWithRegionDTOBuilder.clusterRegionDTOS);
    }
    
    public static final class ESLogicClusterWithRegionDTOBuilder {
        private Long                   id;
        private String                 name;
        private Integer                type;
        private Integer                projectId;
        private String                 dataCenter;
        private Integer                dataNodeNum;
        private String                 memo;
        private Integer                level;
        private Double                 quota;
        private String                 configJson;
        private Integer                health;
        private String                 dataNodeSpec;
        private Double                 diskUsagePercent;
        private Long                   diskTotal;
        private Long                   diskUsage;
        private String                 esClusterVersion;
        private List<ClusterRegionDTO> clusterRegionDTOS;
        
        public ESLogicClusterWithRegionDTOBuilder() {
        }
        
        public ESLogicClusterWithRegionDTOBuilder(ESLogicClusterWithRegionDTO copy) {
            this.id = copy.getId();
            this.name = copy.getName();
            this.type = copy.getType();
            this.projectId = copy.getProjectId();
            this.dataCenter = copy.getDataCenter();
            this.dataNodeNum = copy.getDataNodeNum();
            this.memo = copy.getMemo();
            this.level = copy.getLevel();
            this.quota = copy.getQuota();
            this.configJson = copy.getConfigJson();
            this.health = copy.getHealth();
            this.dataNodeSpec = copy.getDataNodeSpec();
            this.diskUsagePercent = copy.getDiskUsagePercent();
            this.diskTotal = copy.getDiskTotal();
            this.diskUsage = copy.getDiskUsage();
            this.esClusterVersion = copy.getEsClusterVersion();
            this.clusterRegionDTOS = copy.getClusterRegionDTOS();
        }
        
        public ESLogicClusterWithRegionDTOBuilder withId(Long val) {
            id = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withName(String val) {
            name = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withType(Integer val) {
            type = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withProjectId(Integer val) {
            projectId = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withDataCenter(String val) {
            dataCenter = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withDataNodeNum(Integer val) {
            dataNodeNum = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withMemo(String val) {
            memo = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withLevel(Integer val) {
            level = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withQuota(Double val) {
            quota = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withConfigJson(String val) {
            configJson = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withHealth(Integer val) {
            health = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withDataNodeSpec(String val) {
            dataNodeSpec = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withDiskUsagePercent(Double val) {
            diskUsagePercent = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withDiskTotal(Long val) {
            diskTotal = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withDiskUsage(Long val) {
            diskUsage = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withEsClusterVersion(String val) {
            esClusterVersion = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTOBuilder withClusterRegionDTOS(List<ClusterRegionDTO> val) {
            clusterRegionDTOS = val;
            return this;
        }
        
        public ESLogicClusterWithRegionDTO build() {
            return new ESLogicClusterWithRegionDTO(this);
        }
    }
}