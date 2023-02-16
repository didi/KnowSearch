import fetch, { formFetch } from "../lib/fetch";

const prefix = `/v3/security`;

export const userLogin = (params: any) => {
  return fetch(`${prefix}/account/login`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const userLogout = () => {
  return fetch(`${prefix}/account/logout`, {
    method: "POST",
  });
};

export const userRegister = (params: any) => {
  return fetch(`${prefix}/user`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const updateUserInfo = (params: any) => {
  return fetch(`${prefix}/user`, {
    method: "PUT",
    body: JSON.stringify(params),
  });
};

export const checkRegisterUser = (type: number, value: string) => {
  return fetch(`${prefix}/user/${type}/${value}/check`);
};

export const getUser = (id: number) => {
  return fetch(`${prefix}/user/${id}`);
};

export const getUserList = (params: any) => {
  return fetch(`${prefix}/user/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};

export const getRoleList = (params: any) => {
  return fetch(`${prefix}/role/page`, {
    method: "POST",
    body: JSON.stringify(params),
  });
};
