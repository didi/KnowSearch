import { RefObject } from "react";

import { useEffect } from "react";

export const useMouseoutOutSide = (
  eventName: string,
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
    document.addEventListener(eventName, listener);
    return () => {
      document.removeEventListener(eventName, listener);
    };
  }, [eventName, ref, handler]);
};
