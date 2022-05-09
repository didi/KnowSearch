import { IAction } from "interface/common";
import { RegionState } from "store/type";
import actionTypes from "../actions/actionTypes"

export const initialState = {
  typeNameList: [],
  configList: [],
  clusterRolesList: [],
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_PHY_CLUSTER_CONFIG: {
      const { typeNameList } = action.payload;
      return { ...state, typeNameList };
    }
    case actionTypes.SET_PHY_CLUSTER_CONFIG_LIST: {
      const { configList } = action.payload;
      return { ...state, configList };
    }
    case actionTypes.SET_PHY_CLUSTER_CONFIG_ROLES: {
      const { clusterRolesList } = action.payload;
      return { ...state, clusterRolesList };
    }
  }
  return state;
};
