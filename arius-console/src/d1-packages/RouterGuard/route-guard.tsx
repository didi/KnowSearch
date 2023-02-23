import React, { ComponentType, FC, useMemo } from "react";
import { Route, Switch } from "react-router-dom";
import CacheRoute, { CacheSwitch } from "react-router-cache-route";
import { routeGuardWrap } from "./route-guard-wrap";

export interface routeItemType {
  path: string;
  component: ComponentType;
  cacheKey?: string;
  needCache?: boolean;
  redirect?: string;
  permissionPoint?: string;
}

export interface routePropsType {
  routeList: routeItemType[];
  beforeEach?: (props: any) => Promise<Boolean>;
  switchCacheRouter?: (props: any) => void;
  afterEmit?: (props: any) => void;
  attr?: any;
  mulityPage: object;
}

export const RouteGuard: FC<routePropsType> = ({ routeList, beforeEach, switchCacheRouter, afterEmit, attr = {}, mulityPage }) => {
  const AppSwitch = routeList.filter((item) => item.needCache)?.length ? CacheSwitch : Switch;

  const renderRoute = ({ path, component, cacheKey, needCache, redirect, permissionPoint }: routeItemType, key: number) => {
    if (Object.keys(mulityPage).indexOf(path) > -1) {
      cacheKey = window.location.search;
    }
    const PathRoute: any = needCache ? CacheRoute : Route;

    return (
      <PathRoute
        path={path}
        exact={true}
        component={useMemo(
          () =>
            routeGuardWrap({
              needCache,
              Component: component,
              cacheKey,
              beforeEach,
              switchCacheRouter,
              afterEmit,
              redirect,
              attr,
              path,
              permissionPoint,
            }),
          [needCache, component, cacheKey, beforeEach, switchCacheRouter, afterEmit, redirect, attr]
        )}
        when="always"
        cacheKey={cacheKey}
        key={key}
      />
    );
  };

  return <AppSwitch>{routeList.map((item, index) => renderRoute(item, index))}</AppSwitch>;
};
