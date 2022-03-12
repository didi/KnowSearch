import { ILabelValue } from '@types/base-types';
import actionTypes from './actionTypes';

export const setPhyClusterList = (phyClusterList: ILabelValue[], type: string = "", tableData = []) => ({
  type: actionTypes.SET_PHY_CLUSTER_LIST,
  payload: {
    phyClusterList,
    type,
    tableData
  }
});

export const setRegionList = (region: any) => ({
  type: actionTypes.SET_REGION_LIST,
  payload: {
    region
  }
});

export const setRacks = (racks: string) => ({
  type: actionTypes.SET_RACKS,
  payload: {
    racks
  }
});

export const setRacksArr = (racksArr: ILabelValue[]) => ({
  type: actionTypes.SET_RACKS_ARR,
  payload: {
    racksArr
  }
});