import * as React from "react";
import { connect } from "react-redux";
import NewClusterModal from "./modal/logic-cluster/new-cluster";
import ApplyClusterModal from "./modal/logic-cluster/apply-cluster";
import EditClusterModal from "./modal/logic-cluster/edit-cluster";
import ExpandShrinkModal from "./modal/logic-cluster/expand-shrink-cluster";
import TransferClusterModal from "./modal/logic-cluster/transfer-cluster";
import ApplyAauthorityModal from "./modal/logic-cluster/apply-authority";
import { AccessCluster } from "./modal/physics-cluster/access-authority";
import ApplyPhyClusterModal from "./modal/physics-cluster/apply-cluster";
import { UpgradeCluster } from "./modal/physics-cluster/upgrade-cluster";
import RestartClusterModal from "./modal/physics-cluster/restart-cluster";
import DockerExpandShrinkCluster from "./modal/physics-cluster/docker-expand-shrink-cluster";
import EditPhyCluster from "./modal/physics-cluster/edit-cluster";
import { ExpandShrinkCluster } from "./modal/physics-cluster/expand-shrink-cluster";
import CustomPlugnModal from "./modal/plugn/custom-plugn";
import RelationRegionModal from "./modal/logic-cluster/relation-region";
import AddPackageModal from "./modal/edition-cluster/add-package";
import { NewConfigModal } from "./modal/physics-cluster/new-config";
import EditConfigDesc from "./modal/physics-cluster/edit-config-desc";
import EditConfig from "./modal/physics-cluster/edit-config";
import { ExpandShrinkIndex } from "./modal/cluster-index/expand-shrink-cluster";
import TransClusterIndex from "./modal/cluster-index/trans-cluster-index";
import { ClearClusterIndex } from "./modal/cluster-index/clear-cluster-index";
import { PhyUpgradeIndex } from "./modal/cluster-index/phy-upgrade-index";
import { PhyModifyIndex } from "./modal/cluster-index/phy-modify-index";
import { PhyCopyIndex } from "./modal/cluster-index/phy-copy-index";
import { LogicApplyAuth } from "./modal/cluster-index/logic-apply-auth";
import { ClusterConfigModal } from "./modal/system/cluster-config";
import NewRegionModal from "./modal/physics-cluster/new-region";
import AddOrEditUserModal from "./modal/user/user/add-new-user";
import AddOrEditProjectModal from "./modal/user/project/add-project";
import TransferOfResources from "./modal/user/project/transfer-of-resources";
import ResourcesAssociated from "./modal/user/project/resources-associated";
import AddOrEditRole from "./modal/user/role/add-or-edit-role";
import { ShowApprovalModal } from "./modal/work-order/approval-modal";
import { BigPicture } from "./modal/indicators/big-picture";
import { IndexConfig } from "./modal/indicators/index-config";
import EditPluginDesc from "./modal/physics-cluster/edit-plugin-desc";
import { DeleteIndex } from './modal/index-admin/delete-index';
import { ChartModal } from './modal/indicators/chart-modal';
import { DeleteCluster } from './modal/physics-cluster/deleteCluster';
import { InstallPlugin } from './modal/physics-cluster/install';
import { UninstallPlugin } from './modal/physics-cluster/unintallPlugn';
import { DeleteLogicCluster } from './modal/logic-cluster/deleteLogicCluster';

// drawer
import ConfigDetail from "./drawer/config-detail";
import RegionTaskList from "./drawer/region-task-list";
import NodeMonitorDrawer from "./drawer/node-monitor";
import PhysicsClusterTaskDrawer from "./drawer/physics-cluster-task";
import { TaskLogModal } from "./modal/work-order/task-log";
import { ShardList } from "./drawer/shard-list/index";
const mapStateToProps = (state: any) => ({
  isLoading: state.modal.loading,
  modalId: state.modal.modalId,
  drawerId: state.modal.drawerId,
});

const AllModalInOne = (props: any) => {
  const { modalId, drawerId } = props;
  if (!modalId && !drawerId) return null;

  return (
    <>
      {modalMap[modalId] || null}
      {drawerMap[drawerId] || null}
    </>
  );
};

const modalMap = {
  newCluster: <NewClusterModal />,
  applyCluster: <ApplyClusterModal />,
  editCluster: <EditClusterModal />,
  expandShrink: <ExpandShrinkModal />,
  transferCluster: <TransferClusterModal />,
  applyAauthority: <ApplyAauthorityModal />,
  accessCluster: <AccessCluster />,
  applyPhyCluster: <ApplyPhyClusterModal />,
  upgradeCluster: <UpgradeCluster />,
  restartCluster: <RestartClusterModal />,
  expandShrinkCluster: <ExpandShrinkCluster />,
  dockerExpandShrinkCluster: <DockerExpandShrinkCluster />,
  editPhyCluster: <EditPhyCluster />,
  customPlugn: <CustomPlugnModal />,
  relationRegion: <RelationRegionModal />,
  newConfigModal: <NewConfigModal />,
  addPackageModal: <AddPackageModal />,
  expandShrinkIndex: <ExpandShrinkIndex />,
  transClusterIndex: <TransClusterIndex />,
  clearClusterIndex: <ClearClusterIndex />,
  phyUpgradeIndex: <PhyUpgradeIndex />,
  phyModifyIndex: <PhyModifyIndex />,
  phyCopyIndex: <PhyCopyIndex />,
  logicApplyAuth: <LogicApplyAuth />,
  clusterConfigModal: <ClusterConfigModal />,
  newRegionModal: <NewRegionModal />,
  addOrEditUserModal: <AddOrEditUserModal />,
  addOrEditProjectModal: <AddOrEditProjectModal />,
  transferOfResources: <TransferOfResources />,
  resourcesAssociated: <ResourcesAssociated />,
  addOrEditRole: <AddOrEditRole />,
  showApprovalModal: <ShowApprovalModal />,
  taskLogModal: <TaskLogModal />,
  editConfigDesc: <EditConfigDesc />,
  editConfig: <EditConfig />,
  bigPicture: <BigPicture />,
  IndexConfig: <IndexConfig />,
  EditPluginDesc: <EditPluginDesc />,
  deleteIndex: <DeleteIndex />,
  chartModal: <ChartModal />,
  deleteCluster: <DeleteCluster />,
  installplugin: <InstallPlugin />,
  uninstallPlugin: <UninstallPlugin />,
  deleteLogicCluster: <DeleteLogicCluster />,
} as {
  [key: string]: JSX.Element;
};

const drawerMap = {
  configDetail: <ConfigDetail />,
  regionTaskList: <RegionTaskList />,
  nodeMonitorDrawer: <NodeMonitorDrawer />,
  physicsClusterTaskDrawer: <PhysicsClusterTaskDrawer />,
  shardList: <ShardList />
} as {
  [key: string]: JSX.Element;
};

export default connect(mapStateToProps)(AllModalInOne);
