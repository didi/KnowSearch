import actionTypes from "actions/actionTypes";
import { getCookie } from "knowdesign/lib/utils/tools";
import { IAction } from "interface/common";
import { IUser, UserState } from "store/type";

export const initialState: UserState = {
  getName: getCookie,
  isAdminUser: getCookie("isAdminUser") === "yes",
  permissionTree: [],
  userInfo: {} as IUser,
};

export default (state = initialState, action: IAction) => {
  switch (action.type) {
    case actionTypes.SET_USER_TYPE: {
      const { isAdminUser } = action.payload;
      return { ...state, isAdminUser };
    }
    case actionTypes.SET_USER: {
      const { userInfo } = action.payload;
      return { ...state, userInfo };
    }
    case actionTypes.SET_USER_PERMISSION: {
      const { permissionTree } = action.payload;
      return { ...state, permissionTree };
    }
  }
  return state;
};
