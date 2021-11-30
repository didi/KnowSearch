import { applyMiddleware, createStore } from "redux";
import thunk from "redux-thunk";
import reducer from "../reducers";

const initialState = {};

const middleware = applyMiddleware(thunk);

export default createStore(reducer, initialState, middleware);
