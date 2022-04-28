import { RefObject } from "react";

import { useEffect } from "react";

export const useClickOutSide = (
  ref: RefObject<HTMLElement>,
  handler: Function
) => {
  useEffect(() => {
    const listener = (event: MouseEvent) => {
      if (!ref.current || ref.current.contains(event.target as HTMLElement)) {
        return;
      }
      handler(event);
    };
    document.addEventListener("click", listener);
    return () => {
      document.removeEventListener("click", listener);
    };
  }, [ref, handler]);
};
