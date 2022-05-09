import fetch from '../lib/fetch';

export const getLogicClusterNodeList = (clusterId: number) => {
  return fetch(`/v2/op/logic/cluster/nodes?clusterId=${clusterId}`);
};
