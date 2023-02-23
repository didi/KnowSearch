import { IDashBoardAction } from "interface/common";
import { DashboardState } from "store/type";
import actionTypes from "../actions/actionTypes";

const ONE_HOUR = 1000 * 60 * 60;
const currentTime = new Date().getTime();

export const initialState: DashboardState = {
  startTime: currentTime - ONE_HOUR,
  endTime: currentTime,
  tabs: "operation",
  dymanicConfigMetrics: [],
};

export default (state = initialState, action: IDashBoardAction) => {
  switch (action.type) {
    case actionTypes.SET_DASHBOARD: {
      return { ...state, ...action.payload };
    }
    case actionTypes.SET_DASHBOARD_DYMANIC: {
      return { ...state, ...action.payload };
    }
  }
  return state;
};
