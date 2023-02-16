import { DslTpl } from "container/search-query/dsl-tpl";
import { DslPage } from "container/search-query/dsl-page";
import { SqlPage } from "container/search-query/sql-page";
import { KibanaPage } from "container/search-query/kibana-page";
import { QueryTpl } from "container/search-query/query-tpl";
import { DslPermissions, SqlPermissions, KibanaPermissions, SearchQueryPermissions, SearchTemplatePermissions } from "constants/permission";

export const SearchQueryPageRoutes = [
  {
    path: "/search-query/dsl-tpl",
    exact: true,
    component: DslTpl,
    needCache: true,
    permissionPoint: SearchQueryPermissions.PAGE,
  },
  {
    path: "/search-query/search-template",
    exact: true,
    component: QueryTpl,
    permissionPoint: SearchTemplatePermissions.PAGE,
  },
  {
    path: "/search-query/dsl",
    exact: true,
    component: DslPage,
    permissionPoint: DslPermissions.PAGE,
  },
  {
    path: "/search-query/sql",
    exact: true,
    component: SqlPage,
    needCache: true,
    permissionPoint: SqlPermissions.PAGE,
  },
  {
    path: "/search-query/kibana",
    exact: true,
    component: KibanaPage,
    needCache: true,
    permissionPoint: KibanaPermissions.PAGE,
  },
];
