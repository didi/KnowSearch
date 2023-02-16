import { applyMiddleware, createStore } from "redux";
import thunk from "redux-thunk";
import reducer from "../reducers";
import { createGlobalState } from "./../d1-packages/dantd/components/hook/create-global-state/index";

const initialState = {};

const middleware = applyMiddleware(thunk);

export default createStore(reducer, initialState, middleware);

// 切换routerTab时记录状态，用作监听
export const useGlobalPathStatus = createGlobalState([""]);
export const useGlobalLoginStatus = createGlobalState(false);
export const useGlobalPermissionTree = createGlobalState([]);
