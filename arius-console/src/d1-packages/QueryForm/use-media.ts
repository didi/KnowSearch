import { useEffect, useState } from 'react';

export const isClient = typeof window === 'object';

const useMedia = (query: string, defaultState: boolean = false) => {
  const [state, setState] = useState(
    isClient ? () => window.matchMedia(query).matches : defaultState,
  );

  useEffect(() => {
    let mounted = true;
    const mql = window.matchMedia(query);
    const onChange = () => {
      if (!mounted) {
        return;
      }
      setState(!!mql.matches);
    };

    mql.addListener(onChange);
    setState(mql.matches);

    return () => {
      mounted = false;
      mql.removeListener(onChange);
    };
  }, [query]);

  return state;
};

export default useMedia;
