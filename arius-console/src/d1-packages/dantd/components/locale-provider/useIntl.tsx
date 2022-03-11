import { useContext } from 'react';
import IntlContext from './context';

function useIntl(): any {
  const i18n = useContext(IntlContext);
  return i18n;
}

export default useIntl;
