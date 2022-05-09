/* eslint-disable */
import { useState } from 'react';
import useEffectOnce from './useEffectOnce';
import useIsomorphicLayoutEffect from './useIsomorphicLayoutEffect';

export function createGlobalState<S = any>(initialState?: S) {
  const store: { state: S | undefined; setState: (state: S | ((preState: S) => S)) => void; setters: any[] } = {
    state: initialState,
    setState(state: S | ((preState: S) => S)) {
      if (Object.prototype.toString.call(state) === '[object Function]') {
        const stateFunc = state as Function;
        store.state = stateFunc(store.state);
      } else {
        store.state = state as S;
      }
      store.setters.forEach(setter => setter(store.state));
    },
    setters: [],
  };

  return (): [S | undefined, (state: S | ((preState: S) => S)) => void] => {
    const [globalState, stateSetter] = useState<S | undefined>(store.state);

    useEffectOnce(() => () => {
      store.setters = store.setters.filter(setter => setter !== stateSetter);
    });

    useIsomorphicLayoutEffect(() => {
      if (!store.setters.includes(stateSetter)) {
        store.setters.push(stateSetter);
      }
    });

    return [globalState, store.setState];
  };
}

export default createGlobalState;
