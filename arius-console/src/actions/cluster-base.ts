import { INodeListObjet } from 'store/type';
import actionTypes from './actionTypes';

export const setClusterCost = (cost: number | string) => ({
  type: actionTypes.SET_CLUSTER_COST,
  payload: {
    cost
  }
});

export const setClusterNodeList = (nodeList: INodeListObjet) => ({
  type: actionTypes.SET_CLUSTER_NODE_LIST,
  payload: {
    nodeList
  }
});

export const setClusterPackage = (packageList: any) => ({
  type: actionTypes.SET_CLUSTER_PACKEAGE,
  payload: {
    packageList
  }
});
