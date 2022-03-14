import React, { ComponentType, FC, useMemo } from 'react';
import { Route, Switch } from 'react-router-dom';
import CacheRoute, { CacheSwitch } from 'react-router-cache-route';
import { routeGuardWrap } from './route-guard-wrap';

export type routeType = 'default' | 'cache';

export interface routeItemType {
  path: string,
  component: ComponentType,
  cacheKey?: string,
  redirect?: string
}

export interface routePropsType {
  routeList: routeItemType[];
  beforeEach?: (pathname: string) => Promise<Boolean>;
  switchCacheRouter?: (props: any) => void;
  afterEmit?: (props: any) => void;
  routeType?: routeType;
  attr?: any;
  pathRule?: (string) => boolean;
}

export const RouteGuard: FC<routePropsType> = ({ routeList, beforeEach, switchCacheRouter, afterEmit, routeType = 'default', attr = {}, pathRule }) => {
  const AppSwitch = routeType === 'default' ? Switch : CacheSwitch;
  const AppRoute = routeType === 'default' ? Route as any : CacheRoute;
  
  const renderRoute = ({ path, component, cacheKey, redirect }: routeItemType, key: number, pathRule: Function) => {
    const PathRoute = pathRule(path) ? AppRoute : Route;
    return <PathRoute
      path={path}
      exact={true}
      component={useMemo(() => routeGuardWrap({
        routeType,
        Component: component,
        cacheKey,
        beforeEach,
        switchCacheRouter,
        afterEmit,
        redirect,
        attr
      }), [
        routeType,
        component,
        cacheKey,
        beforeEach,
        switchCacheRouter,
        afterEmit,
        redirect,
        attr
      ])}
      when="always"
      cacheKey={cacheKey}
      key={key}
    />
  }

  return <AppSwitch>
    {
      routeList.map((item, index) => renderRoute(item, index, pathRule))
    }
  </AppSwitch>
}

