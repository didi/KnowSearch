import { ClusterState } from 'store/type';
import actionTypes from './actionTypes';

export const setClusterForm = (clusterForm: ClusterState) => ({
  type: actionTypes.SET_CLUSTER_NAME_TIME,
  clusterForm
});

export const setIsUpdate = (isUpdate: boolean) => ({
  type: actionTypes.SET_CLUSTER_IS_UPDATE,
  clusterForm: {
    isUpdate
  }
})