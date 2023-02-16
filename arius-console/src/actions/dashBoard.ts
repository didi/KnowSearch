import { DashboardState } from "store/type";
import actionTypes from "./actionTypes";

export const setDashBoard = (payload: DashboardState | any) => ({
  type: actionTypes.SET_DASHBOARD,
  payload,
});

export const setDashBoardDymanicMetrics = (payload: DashboardState | any) => ({
  type: actionTypes.SET_DASHBOARD_DYMANIC,
  payload,
});
