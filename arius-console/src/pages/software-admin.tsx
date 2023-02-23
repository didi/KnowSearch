import { ScriptCenter } from "container/software-admin/script-center";
import { SoftwareCenter } from "container/software-admin/software-center";
import { ScriptCenterPermissions, SoftwareCenterPermissions } from "constants/permission";

export const SoftwarePageRoutes = [
  {
    path: "/software",
    exact: true,
    component: ScriptCenter,
    permissionPoint: ScriptCenterPermissions.PAGE,
  },
  {
    path: "/software/script-center",
    exact: true,
    component: ScriptCenter,
    permissionPoint: ScriptCenterPermissions.PAGE,
  },
  {
    path: "/software/software-center",
    exact: true,
    component: SoftwareCenter,
    permissionPoint: SoftwareCenterPermissions.PAGE,
  },
];
