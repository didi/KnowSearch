import fetch from '../lib/fetch';

export const getConsoleAppList = () => {
  return fetch(`/v2/console/app/list`);
};

export const getNoCodeLoginAppList = (userName: string) => {
  return fetch(`/v2/console/app/getNoCodeLogin?user=${userName}`, {
    headers: {
      'X-ARIUS-APP-TICKET': 'xTc59aY72',
    },
  });
};