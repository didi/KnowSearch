import React, { CSSProperties, ReactNode } from "react";
import { notification } from "antd";
import "./index.less";
interface INotificationItem {
  type: string;
  message: string;
  description?: string;
  className?: string;
  icon?: ReactNode;
  duration?: number;
  placement?: any;
  style?: CSSProperties;
  attrs?: object;
}
export const XNotification = (item: INotificationItem) => {
  const { type, message, description, className, icon, duration, placement, style, attrs } = item;
  const iconNode = (type: string) => {
    switch (type) {
      case "error":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconerror-circle"></use>
          </svg>
        );
      case "info":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconinfo-circle"></use>
          </svg>
        );
      case "success":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconsuccess-circle"></use>
          </svg>
        );
      case "warning":
        return (
          <svg className="icon svg-icon svg-style" aria-hidden="true">
            <use xlinkHref="#iconwarning-circle"></use>
          </svg>
        );
    }
  };
  return notification?.[type]({
    type,
    message,
    description,
    className: `notificate-name ${className ? className : ""}`,
    icon: icon || iconNode(type),
    duration: duration || 3,
    placement,
    style,
    ...attrs,
  });
};
