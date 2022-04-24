import { IAction } from "interface/common";
import { getCurrentProject } from "lib/utils";
import { AppState } from "store/type";
import actionTypes from "../actions/actionTypes"

export const initialState: AppState = {
  appInfo: getCurrentProject,
  appList: []
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_APP_LIST: {
      const { appList } = action.payload;
      return { ...state, appList };
    }
  }
  return state;
};
