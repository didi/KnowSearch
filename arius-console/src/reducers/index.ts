import { combineReducers } from 'redux';
import modal from './modal';
import sliderMenu from './sliderMenu';
import clusterBase from './cluster-base';
import app from './app';
import user from './user';
import region from './region';
import configInfo from './phy-cluster-config';
import createIndex from './create-index';
import task from './task';
import clusterKanban from './cluster-kanban';
import gatewayKanban from './gateway-kanban';

export default combineReducers({
  modal,
  sliderMenu,
  clusterBase,
  app,
  user,
  region,
  configInfo,
  createIndex,
  task,
  clusterKanban,
  gatewayKanban,
});