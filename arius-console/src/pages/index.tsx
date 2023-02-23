import { ClusterAdminPageRoutes } from "./cluster-admin";
import { ClusterSystemPageRoutes } from "./cluster-system";
import { IndexAdminPagePageRoutes } from "./index-admin";
import { IndexTplManagementPageRoutes } from "./index-tpl-management";
import { IndicatorsKanbanPageRoutes } from "./indicators-kanban";
import { SchedulingPageRoutes } from "./scheduling";
import { SearchQueryPageRoutes } from "./search-query";
import { systemPageRoutes } from "./system-page";
import { workOrderPageRouter } from "./work-order";
import { DashBoardPageRoutes } from "./dashboard";
import { SoftwarePageRoutes } from "./software-admin";
import "../styles/common.less";
import "./index.less";
import { routeItemType } from "../d1-packages/RouterGuard";

export const PageRoutes = [
  ...ClusterAdminPageRoutes,
  ...ClusterSystemPageRoutes,
  ...IndexAdminPagePageRoutes,
  ...IndexTplManagementPageRoutes,
  ...IndicatorsKanbanPageRoutes,
  ...SchedulingPageRoutes,
  ...SearchQueryPageRoutes,
  ...systemPageRoutes,
  ...workOrderPageRouter,
  ...DashBoardPageRoutes,
  ...SoftwarePageRoutes,
] as routeItemType[];
