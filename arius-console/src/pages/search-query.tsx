import * as React from 'react';
import CommonRoutePage from './common';
import { DslTpl } from 'container/search-query/dsl-tpl';
import { IndexSearch } from 'container/search-query/index-search';

export const SearchQueryPageRoutes = [
  {
    path: '/search-query/dsl-tpl',
    exact: true,
    component: DslTpl,
  },
  {
    path: '/search-query/index-search',
    exact: true,
    component: IndexSearch,
  },
]