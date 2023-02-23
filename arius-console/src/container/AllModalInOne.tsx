import * as React from "react";
import { connect } from "react-redux";
import ApplyClusterModal from "./modal/logic-cluster/apply-cluster";
import EditClusterModal from "./modal/logic-cluster/edit-cluster";
import ExpandShrinkModal from "./modal/logic-cluster/expand-shrink-cluster";
import ApplyAauthorityModal from "./modal/logic-cluster/apply-authority";
import AccessCluster from "./modal/physics-cluster/access-cluster";
import ApplyPhyClusterModal from "./modal/physics-cluster/apply-cluster";
import { UpgradeCluster } from "./modal/physics-cluster/upgrade-cluster";
import RestartClusterModal from "./modal/physics-cluster/restart-cluster";
import DockerExpandShrinkCluster from "./modal/physics-cluster/docker-expand-shrink-cluster";
import EditPhyCluster from "./modal/physics-cluster/edit-cluster";
import ExpandShrinkCluster from "./modal/physics-cluster/expand-shrink-cluster";
import RegionDivide from "./modal/physics-cluster/region-divide";
import RegionAdmin from "./modal/physics-cluster/region-admin";
import InstallClusterPlugin from "./modal/physics-cluster/install-plugin";
import EditGatewayUrl from "./modal/physics-cluster/edit-gateway-url";
import BindGateway from "./modal/physics-cluster/bind-gateway";
import CustomPlugnModal from "./modal/plugn/custom-plugn";
import AddPackageModal from "./modal/edition-cluster/add-package";
import { NewConfigModal } from "./modal/physics-cluster/new-config";
import EditConfig from "./modal/physics-cluster/edit-config";
import { ClusterConfigModal } from "./modal/system/cluster-config";
import AddOrEditProjectModal from "./modal/project/add-project";
import TransferOfResources from "./modal/project/transfer-of-resources";
import ResourcesAssociated from "./modal/project/resources-associated";
import AddOrEditRole from "./modal/role/add-or-edit-role";
import ShowApprovalModal from "./modal/work-order/approval-modal";
import { BigPicture } from "./modal/indicators/big-picture";
import { IndexConfig } from "./modal/indicators/index-config";
import EditPluginDesc from "./modal/physics-cluster/edit-plugin-desc";
import { DeleteIndex } from "./modal/index-admin/delete-index";
import { SetAlias } from "./modal/index-admin/set-alias";
import { DeleteAlias } from "./modal/index-admin/delete-alias";
import { BatchExecute } from "./modal/index-admin/batch-execute";
import { ChartTableModal } from "./modal/indicators/chart-tablemodal";
import { DeleteCluster } from "./modal/physics-cluster/deleteCluster";
import { OfflineCluster } from "./modal/logic-cluster/offlineLogicCluster";
import { InstallPlugin } from "./modal/physics-cluster/install";
import { UninstallPlugin } from "./modal/physics-cluster/unintallPlugn";
import { ClearModal } from "./modal/template/clear";
import { ExpandShrinkCapacity } from "./modal/template/expand-shrink-capacity";
import { OpenSeparate } from "./modal/template/open-separate";
import { CreateDCDR } from "./modal/template/create-DCDR";
import { BatchUpdate } from "./modal/template/batch-update";
import { DeleteProject } from "./modal/project/delete-project";
import { UpgradePlugin } from "./modal/physics-cluster/plugin-upgrade";
import { EditConfigGroup } from "./modal/physics-cluster/config-edit";
import { RollbackConfig } from "./modal/physics-cluster/config-rollback";
import { BatchAllocation } from "./modal/physics-cluster/batch-allocation";
import { GatewayVersion } from "./modal/gateway-manage/gateway-version";
import { GatewayEdit } from "./modal/gateway-manage/gateway-edit";
import ExpandShrinkGatewayCluster from "./modal/gateway-manage/gateway-cluster-expand-shrink";
import { CopyTask } from "./modal/scheduling/copy-task";
import { EditTask } from "./modal/scheduling/edit-task";
import { DcdrTimeout } from "./modal/template/dcdr-timeout";
import { Mapping } from "./modal/sql-query/mapping";

// drawer
import ConfigDetail from "./drawer/config-detail";
import NodeMonitorDrawer from "./drawer/node-monitor";
import { TaskLogModal } from "./modal/work-order/task-log";
import { ShardList } from "./drawer/shard-list/index";
import MappingSettingDiff from "./drawer/mapping-diff";
import AccessSetting from "./modal/project/access-setting";
import { DCDRDetail } from "./modal/template/DCDR-detail";
import CreateTemplate from "./drawer/template-create";
import EditTemplate from "./drawer/template-edit";
import CreateIndex from "./drawer/index-create";
import { EditIndexMapping } from "./drawer/index-mapping-edit";
import { EditIndexSetting } from "./drawer/index-setting-edit";
import { IndexSrvRollover } from "./drawer/index-srv-rollover";
import { IndexSrvForceMerge } from "./drawer/index-srv-forceMerge";
import { IndexSrvShrinkSplit } from "./drawer/index-srv-shrinkSplit";
import PhysicsClusterTask from "./modal/physics-cluster/physicsClusterTask/index";
import { ShowApprovalDrawer } from "./modal/work-order/approval-drawer";
import AddScriptDrawer from "./drawer/addScriptDrawer";
import AddSoftwareDrawer from "./drawer/addSoftwareDrawer";
import { ResetPlugin } from "./modal/physics-cluster/plugin-reset";
import { EditPlugin } from "./modal/physics-cluster/plugin-edit";
import JoinGateway from "./drawer/gateway-join";
import { AddGateway } from "./drawer/gateway-add";
import GatewayReset from "./drawer/gateway-reset";
import FastIndex from "./drawer/fast-index";

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
  applyCluster: <ApplyClusterModal />,
  editCluster: <EditClusterModal />,
  expandShrink: <ExpandShrinkModal />,
  applyAauthority: <ApplyAauthorityModal />,
  accessCluster: <AccessCluster />,
  applyPhyCluster: <ApplyPhyClusterModal />,
  dockerExpandShrinkCluster: <DockerExpandShrinkCluster />,
  editPhyCluster: <EditPhyCluster />,
  customPlugn: <CustomPlugnModal />,
  clusterConfigModal: <ClusterConfigModal />,
  transferOfResources: <TransferOfResources />,
  resourcesAssociated: <ResourcesAssociated />,
  addOrEditRole: <AddOrEditRole />,
  showApprovalModal: <ShowApprovalModal />,
  taskLogModal: <TaskLogModal />,
  editConfig: <EditConfig />,
  bigPicture: <BigPicture />,
  IndexConfig: <IndexConfig />,
  EditPluginDesc: <EditPluginDesc />,
  deleteIndex: <DeleteIndex />,
  setAlias: <SetAlias />,
  deleteAlias: <DeleteAlias />,
  batchExecute: <BatchExecute />,
  chartTableModal: <ChartTableModal />,
  deleteCluster: <DeleteCluster />,
  offlineCluster: <OfflineCluster />,
  installplugin: <InstallPlugin />,
  uninstallPlugin: <UninstallPlugin />,
  expandShrinkCapacity: <ExpandShrinkCapacity />,
  openSeparate: <OpenSeparate />,
  createDCDR: <CreateDCDR />,
  batchUpdate: <BatchUpdate />,
  regionDivide: <RegionDivide />,
  regionAdmin: <RegionAdmin />,
  installClusterPlugin: <InstallClusterPlugin />,
  editGatewayUrl: <EditGatewayUrl />,
  bindGateway: <BindGateway />,
  deleteProject: <DeleteProject />,
  upgradePlugin: <UpgradePlugin />,
  rollbackConfig: <RollbackConfig />,
  batchAllocation: <BatchAllocation />,
  gatewayVersion: <GatewayVersion />,
  gatewayEdit: <GatewayEdit />,
  copyTask: <CopyTask />,
  editTask: <EditTask />,
  dcdrTimeout: <DcdrTimeout />,
  mapping: <Mapping />,
} as {
  [key: string]: JSX.Element;
};

const drawerMap = {
  configDetail: <ConfigDetail />,
  nodeMonitorDrawer: <NodeMonitorDrawer />,
  shardList: <ShardList />,
  mappingSettingDiff: <MappingSettingDiff />,
  addOrEditProjectModal: <AddOrEditProjectModal />,
  AccessSetting: <AccessSetting />,
  clearModal: <ClearModal />,
  dcdrDetail: <DCDRDetail />,
  createTemplate: <CreateTemplate />,
  editTemplate: <EditTemplate />,
  createIndex: <CreateIndex />,
  editIndexMapping: <EditIndexMapping />,
  editIndexSetting: <EditIndexSetting />,
  indexSrvRollover: <IndexSrvRollover />,
  indexSrvForceMerge: <IndexSrvForceMerge />,
  indexSrvShrinkSplit: <IndexSrvShrinkSplit />,
  physicsClusterTask: <PhysicsClusterTask />,
  showApprovalDrawer: <ShowApprovalDrawer />,
  addPackageModal: <AddPackageModal />,
  upgradeCluster: <UpgradeCluster />,
  restartCluster: <RestartClusterModal />,
  newConfigModal: <NewConfigModal />,
  resetPlugin: <ResetPlugin />,
  editPlugin: <EditPlugin />,
  editConfigGroup: <EditConfigGroup />,
  addScriptDrawer: <AddScriptDrawer />,
  addSoftwareDrawer: <AddSoftwareDrawer />,
  joinGateway: <JoinGateway />,
  addGateway: <AddGateway />,
  gatewayReset: <GatewayReset />,
  expandShrinkCluster: <ExpandShrinkCluster />,
  expandShrinkGatewayCluster: <ExpandShrinkGatewayCluster />,
  fastIndex: <FastIndex />,
} as {
  [key: string]: JSX.Element;
};

export default connect(mapStateToProps)(AllModalInOne);
