import { IAction } from "interface/common";
import { ClustrtData, INodeListObjet } from "store/type";
import actionTypes from "../actions/actionTypes"

export const initialState: ClustrtData = {
  cost: 0,
  nodeList: {} as INodeListObjet,
  packageList: { docker: [], host: [] },
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_CLUSTER_COST: {
      const { cost } = action.payload;
      return { ...state, cost };
    }
    case actionTypes.SET_CLUSTER_NODE_LIST: {
      const { nodeList } = action.payload;
      return { ...state, nodeList };
    }
    case actionTypes.SET_CLUSTER_PACKEAGE: {
      const { packageList } = action.payload;
      return { ...state, packageList };
    }
  }

  return state;
};
