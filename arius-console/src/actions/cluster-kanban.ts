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
export const setClusterKanban = (sliderMenuCollapsed: any) => {
  //SET_KAN_BAN 保存接口返回的标题信息用于标题配置化
  return ({
    type: actionTypes.SET_KAN_BAN,
    clusterForm: sliderMenuCollapsed
  })
}