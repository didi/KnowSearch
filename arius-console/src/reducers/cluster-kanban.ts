import { IClusterKanBanAction } from "interface/common";
import { ClusterState } from "store/type";
import actionTypes from "../actions/actionTypes"

const ONE_HOUR = 1000 * 60 * 60;
const currentTime = new Date().getTime();

export const initialState: ClusterState = {
  clusterName: "",
  startTime: currentTime - ONE_HOUR,
  endTime: currentTime,
  isMoreDay: false,
};

export default (state = initialState, action: IClusterKanBanAction) => {
  switch (action.type) {
    case actionTypes.SET_CLUSTER_NAME_TIME: {
      return { ...state, ...action.clusterForm };
    }
    case actionTypes.SET_CLUSTER_IS_UPDATE: {
      return { ...state, ...action.clusterForm };
    }
  }
  return state;
};
