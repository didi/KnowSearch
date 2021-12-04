import actionTypes from './actionTypes';

export const setSlierMenu = (sliderMenuCollapsed: boolean) => {
  return ({
    type: actionTypes.SET_SLIDER_MENU,
    payload: {
      sliderMenuCollapsed
    }
  })
}