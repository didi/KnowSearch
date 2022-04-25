import { GatewayState } from 'store/type';
import actionTypes from './actionTypes';

export const setGatewayForm = (gatewayForm: GatewayState) => ({
  type: actionTypes.SET_GATEWAY_TIME,
  gatewayForm
});

export const setIsUpdate = (isUpdate: boolean) => ({
  type: actionTypes.SET_GATEWAY_IS_UPDATE,
  gatewayForm: {
    isUpdate
  }
})