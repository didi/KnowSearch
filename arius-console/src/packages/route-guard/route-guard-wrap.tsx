
import React, { ComponentType, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { dropByCacheKey, useDidCache } from 'react-router-cache-route';

export interface routeGuardWrapPropsType {
  Component: ComponentType;
  cacheKey?: string;
  beforeEach?: (props: any) => Promise<Boolean>;
  switchCacheRouter?: (props: any) => void;
  afterEmit?: (props: any) => void;
  redirect?: string;
  routeType: string;
  attr?: any;
}

export const routeGuardWrap = ({
  Component,
  cacheKey,
  beforeEach,
  switchCacheRouter,
  afterEmit,
  redirect,
  routeType,
  attr,
}: routeGuardWrapPropsType) => {
  const RouteGuardWrap = withRouter((props) => {
    const { history } = props;
    
    const before = async () => {
      if (!beforeEach) {
        return;
      }

      const res = await beforeEach(props);

      if (res) {
        history.push(redirect || "");
        // 卸载组件
        if(routeType === 'cache') {
          cacheKey && dropByCacheKey(cacheKey);
        }
      }
    }

    useEffect(() => {
      before();

      return () => {
        afterEmit && afterEmit(props);
      }
    }, []);

    // 监听组件是否被缓存
    useDidCache(() => {
      // 只有 keepalive 才会有路由跳转事件
      if(routeType === 'cache') {
        switchCacheRouter && switchCacheRouter(props);
      }
    });

    return <Component {...props} {...attr} />;
  })

  return RouteGuardWrap;
};
