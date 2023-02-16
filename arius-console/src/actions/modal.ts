import actionTypes from "./actionTypes";

export const setModalId = (modalId: string, params?: any, cb?: Function) => ({
  type: actionTypes.SET_MODAL_ID,
  payload: {
    modalId,
    params,
    cb,
  },
});

export const setDrawerId = (drawerId: string, params?: any, cb?: Function) => ({
  type: actionTypes.SET_DRAWER_ID,
  payload: {
    drawerId,
    params,
    cb,
  },
});
