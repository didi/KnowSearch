function getApi(sys: string, path: string) {
  const prefix = "/api/";
  return `${prefix}${sys}${path}`;
}

const getCompleteApi = (path: string) => path;

const api = {
  publicKey: "/v3/thirdpart/sso/publicKey",
  opensourceLogin: "/v3/thirdpart/sso/login",
  opensourceRegister: "/v3/thirdpart/sso/register",
  completeUrl: getCompleteApi,
};

export default api;
