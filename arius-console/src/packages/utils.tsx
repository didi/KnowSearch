import { defaultPageSizeOptions } from './config';

export const getPaginationOptions = (onShowSizeChange?: (size: number) => void) => {
  const _defaultPageSize = window.localStorage.getItem('ecmc-global-pagesize');
  const defaultPageSize = _defaultPageSize ? Number(_defaultPageSize) : undefined;
  return {
    pageSize: defaultPageSize,
    showSizeChanger: true,
    pageSizeOptions: defaultPageSizeOptions,
    onShowSizeChange: (_current: any, size: number) => {
      window.localStorage.setItem('ecmc-global-pagesize', String(size));
      if (onShowSizeChange) onShowSizeChange(size);
    },
  };
};

export function createStylesheetLink(ident: string, path: string) {
  const headEle = document.getElementsByTagName('head')[0];
  const linkEle = document.createElement('link');
  linkEle.id = `${ident}-stylesheet`;
  linkEle.rel = 'stylesheet';
  linkEle.href = path;
  headEle.append(linkEle);
}

export function parseJSON(json: string) {
  if (typeof json === 'string') {
    let paresed;
    try {
      paresed = JSON.parse(json);
    } catch (e) {
      console.log(e);
    }
    return paresed;
  }
  return undefined;
}

