import actionTypes from './actionTypes';

export const setTemporaryFormMap = (key: string, result: any,) => ({
  type: actionTypes.SET_INDEX_CREATE_TEMPORAYFORMMAP,
  payload: {
    key,
    result,
  }
});

export const setSecondChildMap = (key: string, result: any,) => ({
  type: actionTypes.SET_INDEX_CREATE_SECOND_CHILD_MAP,
  payload: {
    key,
    result,
  }
});

export const setLoadingMap = (key: string, result: any,) => ({
  type: actionTypes.SET_INDEX_CREATE_LOADING_MAP,
  payload: {
    key,
    result,
  }
});

export const setCurrentStep = (currentStep: number,) => ({
  type: actionTypes.SET_CURRENT_STEP,
  payload: {
    currentStep
  }
});

export const setFieldTypeMap = (fieldTypeMap: any) => ({
  type: actionTypes.SET_FIELD_TYPE_MAP,
  payload: {
    fieldTypeMap
  }
});

export const setThirdChildMap = (key: string, result?: any,) => ({
  type: actionTypes.SET_THIRD_CHILD_MAP,
  payload: {
    key,
    result,
  }
});

export const setEditorInstance = (activeInstance: any,) => ({
  type: actionTypes.SET_EDITOR_INSTANCE,
  payload: {
    activeInstance
  }
});

export const setClearCreateIndex = () => ({
  type: actionTypes.SET_CLEAR_CREATE_INDEX,
  payload: {}
});

export const setCreateIndex = (payload: any) => {
  return {
    type: actionTypes.SET_CREATE_INDEX,
    payload,
  }
};