import actionTypes from "./actionTypes";

export const setProjectList = (projectList: any) => {
  return {
    type: actionTypes.SET_APP_LIST,
    payload: {
      projectList,
    },
  };
};

export const setGatewayBindStatus = (gatewayStatus: boolean) => {
  return {
    type: actionTypes.SET_APP_GATEWAY_STATUS,
    payload: {
      gatewayStatus,
    },
  };
};
