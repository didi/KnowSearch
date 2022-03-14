import { ClusterAdminPageRoutes } from './cluster-admin';
import { ClusterIndexPageRoutes } from './cluster-index';
import { ClusterSystemPageRoutes } from './cluster-system';
import { IndexAdminPagePageRoutes } from './index-admin';
import { IndexTplManagementPageRoutes } from './index-tpl-management';
import { IndicatorsKanbanPageRoutes } from './indicators-kanban';
import { SchedulingPageRoutes } from './scheduling';
import { SearchQueryPageRoutes } from './search-query';
import { UserManagementPageRoutes } from './user-management';
import { workOrderPageRouter } from './work-order';
import "../styles/common.less";
import "./index.less";

export const PageRoutes = [
  ...ClusterAdminPageRoutes,
  ...ClusterIndexPageRoutes,
  ...ClusterSystemPageRoutes,
  ...IndexAdminPagePageRoutes,
  ...IndexTplManagementPageRoutes,
  ...IndicatorsKanbanPageRoutes,
  ...SchedulingPageRoutes,
  ...SearchQueryPageRoutes,
  ...UserManagementPageRoutes,
  ...workOrderPageRouter,
]