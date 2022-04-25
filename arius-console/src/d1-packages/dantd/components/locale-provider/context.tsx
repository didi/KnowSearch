import { createContext } from 'react';
import TRANSLATIONS from '../locale';

interface IContextProps {
  t: (key: any) => any;
}

const locale = 'zh-CN';

const IntlContext = createContext<IContextProps>({
  t: (key) => TRANSLATIONS[locale][key],
});

export default IntlContext;
