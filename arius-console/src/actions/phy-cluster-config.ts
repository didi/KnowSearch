import actionTypes from './actionTypes';

export const setPhyClusterConfigType = (typeNameList: any,) => ({
  type: actionTypes.SET_PHY_CLUSTER_CONFIG,
  payload: {
    typeNameList,
  }
});

export const setPhyClusterConfigList = (configList: any,) => ({
  type: actionTypes.SET_PHY_CLUSTER_CONFIG_LIST,
  payload: {
    configList,
  }
});

export const setPhyClusterConfigRoles = (clusterRolesList: any,) => ({
  type: actionTypes.SET_PHY_CLUSTER_CONFIG_ROLES,
  payload: {
    clusterRolesList,
  }
});