import fetch, { formFetch } from "../lib/fetch";
const v3Prefix = `/v3`;
const logiSecurityRole = `/security/role`;

export const judgeAdminUser = () => {
  return fetch(`${v3Prefix}${logiSecurityRole}/is-admin`, { errorNoTips: true, returnRes: true });
};
