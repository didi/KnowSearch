import * as Bowser from "bowser";
export const defaultPageSizeOptions = ['10', '30', '50', '100', '300', '500', '1000'];

const browser = Bowser.getParser(window.navigator.userAgent);
export const isValidBrowser = browser.satisfies({
  chrome: '>=70',
  ie: '>=10',
  edge: '>=18'
});

