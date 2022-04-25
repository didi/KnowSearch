import { IDcdrStepDetailInfo } from 'typesPath/task-types';
import actionTypes from './actionTypes';

export const setTaskBaseInfo = (taskBaseInfo: any) => {
  return {
    type: actionTypes.SET_TASK_BASE_INFO,
    payload: {
      taskBaseInfo,
    }
  }
};

export const setDcdrStepDetail = (dcdrStepDetail: IDcdrStepDetailInfo) => {
  return {
    type: actionTypes.SET_DCDR_STEP_DEL,
    payload: {
      dcdrStepDetail
    }
  }
};

export const setTaskMenu = () => {
  return {
    type: actionTypes.SET_TASK_MENU,
    payload: {
      menu: window.location.hash.replace('#', '') || 'base'
    }
  }
};

export const setTaskBaseLoading = (loading: boolean) => {
  return {
    type: actionTypes.SET_TASK_BASE_LOADING,
    payload: {
      loading
    }
  }
};