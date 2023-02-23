import { IAction } from "interface/common";
import { getCurrentProject } from "lib/utils";
import { AppState } from "store/type";
import actionTypes from "../actions/actionTypes";

export const initialState: AppState = {
  appInfo: getCurrentProject,
  projectList: [],
  gatewayStatus: true,
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_APP_LIST: {
      const { projectList } = action.payload;
      return { ...state, projectList };
    }
    case actionTypes.SET_APP_GATEWAY_STATUS: {
      const { gatewayStatus } = action.payload;
      return { ...state, gatewayStatus };
    }
  }
  return state;
};
