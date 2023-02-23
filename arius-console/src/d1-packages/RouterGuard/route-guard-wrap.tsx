import React, { ComponentType, useEffect, useState } from "react";
import { withRouter } from "react-router-dom";
import { dropByCacheKey, useDidCache, useDidRecover } from "react-router-cache-route";
import { Page403 } from "../CommonPages/Exception";

export interface routeGuardWrapPropsType {
  Component: ComponentType;
  cacheKey?: string;
  beforeEach?: (props: any) => Promise<Boolean>;
  switchCacheRouter?: (props: any) => void;
  afterEmit?: (props: any) => void;
  redirect?: string;
  needCache?: boolean;
  attr?: any;
  permissionPoint?: string;
  path?: string;
}

export const routeGuardWrap = ({
  Component,
  cacheKey,
  needCache,
  beforeEach,
  switchCacheRouter,
  afterEmit,
  redirect,
  attr,
  path,
  permissionPoint,
}: routeGuardWrapPropsType) => {
  const RouteGuardWrap = withRouter((props) => {
    const { history } = props;
    const [pagePermission, setPagePermission] = useState<Boolean>(true);
    const [loading, setLoading] = useState<Boolean>(true);

    const before = async () => {
      if (!beforeEach) {
        setLoading(false);
        return;
      }
      try {
        const res = await beforeEach({ path, permissionPoint, ...props });
        // history.push(redirect || "");
        // 卸载组件
        if (needCache) {
          cacheKey && dropByCacheKey(cacheKey);
        }
        setLoading(false);
      } catch (err) {
        setPagePermission(false);
        setLoading(false);
      }
    };

    useEffect(() => {
      before();

      return () => {
        afterEmit && afterEmit(props);
      };
    }, []);

    // 监听组件是否被缓存
    useDidCache(() => {
      // 只有 keepalive 才会有路由跳转事件
      if (needCache) {
        switchCacheRouter && switchCacheRouter(props);
      }
    });

    useDidRecover(() => {
      // 页面移回可视区
    });

    return loading ? null : pagePermission ? <Component {...props} {...attr} /> : <Page403 />;
  });

  return RouteGuardWrap;
};
