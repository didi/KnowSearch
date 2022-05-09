import { IAction } from "interface/common";
import actionTypes from "../actions/actionTypes"

export const initialState = {
  sliderMenuCollapsed: false
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_SLIDER_MENU: {
      const { sliderMenuCollapsed } = action.payload;
      return { ...state, sliderMenuCollapsed };
    }
  }
  return state;
};
