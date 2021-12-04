import { IAction } from "interface/common";
import { TaskState } from "store/type";
import actionTypes from "../actions/actionTypes"

export const initialState: TaskState = {
  dcdrStepDetail: null,
  taskBaseInfo: null,
  menu: window.location.hash.replace('#', '') || 'base',
  loading: false,
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_TASK_BASE_INFO: {
      const { taskBaseInfo } = action.payload;
      return { ...state, taskBaseInfo };
    }
    case actionTypes.SET_DCDR_STEP_DEL: {
      const { dcdrStepDetail } = action.payload;
      return { ...state, dcdrStepDetail };
    }
    case actionTypes.SET_TASK_MENU: {
      const { menu } = action.payload;
      return { ...state, menu };
    }
    case actionTypes.SET_TASK_BASE_LOADING: {
      const { loading } = action.payload;
      return { ...state, loading };
    }
  }

  return state;
};
