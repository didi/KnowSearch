import * as React from "react";
import { Schedulinglog } from "container/Scheduling/schedulinglog";
import { TaskList } from "container/Scheduling/taskList";
import { ShceduleLogPermissions, ShceduleTaskPermissions } from "constants/permission";

export const SchedulingPageRoutes = [
  {
    path: "/scheduling",
    exact: true,
    component: TaskList,
    permissionPoint: ShceduleTaskPermissions.PAGE,
  },
  {
    path: "/scheduling/task",
    exact: true,
    component: TaskList,
    needCache: true,
    permissionPoint: ShceduleTaskPermissions.PAGE,
  },
  {
    path: "/scheduling/log",
    exact: true,
    component: Schedulinglog,
    needCache: true,
    permissionPoint: ShceduleLogPermissions.PAGE,
  },
  {
    path: "/scheduling/log/detail",
    exact: true,
    component: Schedulinglog,
    permissionPoint: ShceduleLogPermissions.PAGE,
  },
];
