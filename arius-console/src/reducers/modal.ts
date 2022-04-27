import { IAction } from "interface/common";
import { ModalState } from "store/type";
import actionTypes from "../actions/actionTypes"

export const initialState: ModalState = {
  loading: false,
  modalId: '',
  drawerId: '',
  params: null,
  cb: null,
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_MODAL_ID: {
      const { modalId, params, cb } = action.payload;
      return { ...state, modalId, params, cb };
    }
    case actionTypes.SET_DRAWER_ID: {
      const { drawerId, params, cb } = action.payload;
      return { ...state, drawerId, params, cb };
    }
    case actionTypes.SET_LOADING: {
      const { loading } = action.payload;
      return { ...state, loading };
    }
  }

  return state;
};
