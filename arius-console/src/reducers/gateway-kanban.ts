import { IGatewayKanbanAction } from "interface/common";
import { GatewayState } from "store/type";
import actionTypes from "../actions/actionTypes"

const ONE_HOUR = 1000 * 60 * 60;
const currentTime = new Date().getTime();

export const initialState: GatewayState = {
  startTime: currentTime - ONE_HOUR,
  endTime: currentTime,
  isMoreDay: false,
  isUpdate: true,
};

export default (state = initialState, action: IGatewayKanbanAction) => {
  switch (action.type) {
    case actionTypes.SET_GATEWAY_TIME: {
      return { ...state, ...action.gatewayForm };
    }
    case actionTypes.SET_GATEWAY_IS_UPDATE: {
      return { ...state, ...action.gatewayForm };
    }
  }
  return state;
};
