import { createContext } from 'react';

interface IContextProps {
  t: (key: any) => any;
}


const IntlContext = createContext<IContextProps>({
  t: () => '展开',
});

export default IntlContext;
