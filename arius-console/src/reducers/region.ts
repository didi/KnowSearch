import { IAction } from "interface/common";
import { RegionState } from "store/type";
import actionTypes from "../actions/actionTypes"

export const initialState: RegionState = {
  phyClusterList: [],
  region: [],
  racks: '',
  racksArr: [],
  type: "",
  tableData: [],
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_PHY_CLUSTER_LIST: {
      const { phyClusterList, type, tableData} = action.payload;
      return { ...state, phyClusterList, type, tableData };
    }
    case actionTypes.SET_REGION_LIST: {
      const { region } = action.payload;
      return { ...state, region };
    }
    case actionTypes.SET_RACKS: {
      const { racks } = action.payload;
      return { ...state, racks };
    }
    case actionTypes.SET_RACKS_ARR: {
      const { racksArr } = action.payload;
      return { ...state, racksArr };
    }
  }
  return state;
};
