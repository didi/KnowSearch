
import fetch, { formFetch } from '../lib/fetch';

export const getAccount = () => {
  return fetch(`/v3/normal/account/role`);
};

export const userLogin = (params: any) => {
  return fetch(`/v3/thirdpart/sso/login`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const userLogout = () => {
  return fetch(`/v3/thirdpart/sso/logout`, {
    method: 'DELETE',
  });
};

export const userRegister = (params: any) => {
  return fetch(`/v3/thirdpart/sso/register`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const checkUserNameApi = (userName: string) => {
  return fetch(`/v3/normal/user/${userName}/check`);
}

export const getPublicKey = () => {
  return fetch(`/v3/thirdpart/sso/publicKey`);
}

export const getDomainAccount = (domainAccount: string) => {
  return fetch(`/v3/normal/user/${domainAccount}/get`);
}

export const upUser = (params: any) => {
  return fetch(`/v3/normal/user`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
}

export const updataPassword = (params: any) => {
  return fetch(`/v3/normal/user/password`, {
    method: 'PUT',
    body: JSON.stringify(params),
  });
}