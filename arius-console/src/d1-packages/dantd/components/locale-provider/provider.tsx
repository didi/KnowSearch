import React from 'react';
import IntlContext from './context';
import TRANSLATIONS from '../locale';

interface IIntlProps {
  locale: 'zh-CN' | 'en-US' | string;
  children: React.ReactNode;
}

const IntlProvider = (props: IIntlProps) => {
  const locale = props.locale || 'en-US';
  const i18n = {
    t: (key) => TRANSLATIONS[locale][key],
  };
  return (
    <IntlContext.Provider 
      value={i18n} 
    >
      {props.children}
    </IntlContext.Provider>
  );
};

export default IntlProvider;
