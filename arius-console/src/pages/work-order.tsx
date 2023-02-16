import * as React from "react";
import { MyApplication } from "container/work-order/my-application";
import { OrderDetail } from "container/work-order/order-detail";
import { MyApproval } from "container/work-order/my-approval";
import { TaskList } from "container/work-order/task";
import { TaskDetail } from "container/work-order/task-detail";
import { DcdrTaskList } from "container/work-order/dcdr-detail";
import { MyApplyPermissions, MyApprovalPermissions, TaskPermissions } from "constants/permission";

export const workOrderPageRouter = [
  {
    path: "/work-order",
    exact: true,
    component: MyApplication,
    permissionPoint: MyApplyPermissions.PAGE,
  },
  {
    path: "/work-order/my-application",
    exact: true,
    component: MyApplication,
    needCache: true,
    permissionPoint: MyApplyPermissions.PAGE,
  },
  {
    path: "/work-order/my-application/detail",
    exact: true,
    component: OrderDetail,
    permissionPoint: MyApplyPermissions.PAGE,
  },
  {
    path: "/work-order/my-approval",
    exact: true,
    component: MyApproval,
    needCache: true,
    permissionPoint: MyApprovalPermissions.PAGE,
  },
  {
    path: "/work-order/my-approval/detail",
    exact: true,
    component: OrderDetail,
    permissionPoint: MyApprovalPermissions.PAGE,
  },
  {
    path: "/work-order/task",
    exact: true,
    needCache: true,
    component: TaskList,
    permissionPoint: TaskPermissions.PAGE,
  },
  {
    path: "/work-order/task/detail",
    exact: true,
    component: TaskDetail,
    permissionPoint: TaskPermissions.PAGE,
  },
  {
    path: "/work-order/task/dcdrdetail",
    exact: true,
    component: DcdrTaskList,
    permissionPoint: TaskPermissions.PAGE,
  },
];
