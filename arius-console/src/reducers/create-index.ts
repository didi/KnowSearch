import { IUNSpecificInfo } from "typesPath/base-types";
import { IAction, IStringMap } from "interface/common";
import actionTypes from "../actions/actionTypes";

export const initialState: any = {
  currentStep: 0,
  secondChildMap: new Map(),
  thirdChildMap: new Map(),
  temporaryFormMap: new Map(),
  fieldTypeMap: {} as IStringMap,
  activeInstance: null,
  loadingMap: {
    list: false,
    zoomInfo: false,
  } as {
    [key: string]: boolean;
  },
  cancelCopy: false,
  asyncTranslog: false,
  customerAnalysisJson: null,
  customerAnalysisValue: "",
  settingCount: 0,
  dataCenter: null,
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_INDEX_CREATE_TEMPORAYFORMMAP: {
      const { key, result } = action.payload;
      state.temporaryFormMap.set(key, result);
      return { ...state };
    }
    case actionTypes.SET_INDEX_CREATE_SECOND_CHILD_MAP: {
      const { key, result } = action.payload;
      state.secondChildMap.set(key, result);
      return { ...state };
    }
    case actionTypes.SET_INDEX_CREATE_LOADING_MAP: {
      const { key, result } = action.payload;
      state.loadingMap[key] = result;
      return { ...state };
    }
    case actionTypes.SET_THIRD_CHILD_MAP: {
      const { key, result } = action.payload;
      if (result) {
        state.thirdChildMap.set(key, result);
      } else {
        state.thirdChildMap.delete(key);
      }
      return { ...state };
    }
    case actionTypes.SET_CURRENT_STEP: {
      const { currentStep } = action.payload;
      return { ...state, currentStep };
    }
    case actionTypes.SET_FIELD_TYPE_MAP: {
      const { fieldTypeMap } = action.payload;
      return { ...state, fieldTypeMap };
    }
    case actionTypes.SET_EDITOR_INSTANCE: {
      const { activeInstance } = action.payload;
      return { ...state, activeInstance };
    }
    case actionTypes.SET_CLEAR_CREATE_INDEX: {
      return {
        currentStep: 0,
        secondChildMap: new Map(),
        thirdChildMap: new Map(),
        temporaryFormMap: new Map(),
        fieldTypeMap: {} as IStringMap,
        activeInstance: null,
        loadingMap: {
          list: false,
          zoomInfo: false,
        } as {
          [key: string]: boolean;
        },
        cancelCopy: false,
        asyncTranslog: false,
        customerAnalysisJson: null,
        customerAnalysisValue: "",
      };
    }
    case actionTypes.SET_CREATE_INDEX: {
      return { ...state, ...action.payload };
    }
  }

  return state;
};
