import { IPhysicalTemplateParams } from '@types/params-types';
import fetch, { formFetch } from '../lib/fetch';

export const getPhysicalTemplateList = (params: IPhysicalTemplateParams) => {
  return fetch(`/v2/op/template/physical/list`, {
    method: 'POST',
    body: JSON.stringify(params),
  });
};

export const getPhyNodeDivideList = (clusterId: number) => {
  return fetch(`/v3/op/phy/cluster/${clusterId}/regioninfo`); // regioninfo
};
