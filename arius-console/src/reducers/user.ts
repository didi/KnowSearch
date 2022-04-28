import { IAction } from "interface/common";
import { getCookie } from "lib/utils";
import { UserState } from "store/type";

export const initialState: UserState = {
  getName: getCookie
};

export default (state = initialState, action: IAction) => {
  return state;
};
