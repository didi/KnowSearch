import { DashboardState } from 'store/type';
import actionTypes from './actionTypes';

export const setDashBoard = (payload: DashboardState | any) => ({
  type: actionTypes.SET_DASHBOARD,
  payload,
});
