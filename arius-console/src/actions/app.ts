import { IProject } from 'interface/project';
import actionTypes from './actionTypes';

export const setAppList = (appList: any) => {
  return ({
    type: actionTypes.SET_APP_LIST,
    payload: {
      appList,
    }
  })
}
