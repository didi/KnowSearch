import actionTypes from "./actionTypes";

export const setProjectList = (projectList: any) => {
  return {
    type: actionTypes.SET_APP_LIST,
    payload: {
      projectList,
    },
  };
};
