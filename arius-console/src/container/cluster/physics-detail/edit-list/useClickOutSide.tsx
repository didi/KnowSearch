import { RefObject } from "react";

import { useEffect } from "react";

export const useClickOutSide = (ref: RefObject<HTMLElement>, handler: Function, options?: any) => {
  useEffect(() => {
    const listener = (event: MouseEvent) => {
      if (!ref.current || ref.current.contains(event.target as HTMLElement)) {
        return;
      }
      handler(event);
    };
    options ? document.addEventListener("click", listener, options) : document.addEventListener("click", listener);
    return () => {
      options ? document.addEventListener("click", listener, options) : document.addEventListener("click", listener);
    };
  }, [ref, handler]);
};
