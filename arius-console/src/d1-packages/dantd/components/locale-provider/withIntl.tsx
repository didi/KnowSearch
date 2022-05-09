import React from 'react';
import IntlContext from './context';

export const withIntl = () => {
  return (WrappedComponent) => {
    const ComponentWithIntl = (props) => (
      <IntlContext.Consumer>
        {(i18n) => <WrappedComponent {...i18n} {...props} />}
      </IntlContext.Consumer>
    );
    return ComponentWithIntl;
  };
};

export default withIntl;
