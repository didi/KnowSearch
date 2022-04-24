import actionTypes from './actionTypes';

export const setFullScreenContent = (content: string | JSX.Element) => ({
  type: actionTypes.SET_FULL_SCREEN_CONTENT,
  payload: {
    content,
  }
});