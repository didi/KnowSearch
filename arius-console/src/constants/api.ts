const prefixMap = {
  demo: 'api',
};

export const csrfTokenMethod = ['POST', 'PUT', 'DELETE'];

function getApi(path: string, prefix: string = 'demo') {
  return `${prefixMap[prefix] || ''}${path}`;
}

const api = {
  demo: getApi('/auth/login'),
};

export default api;
