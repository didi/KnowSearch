import React from 'react';
import { InfoCircleOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';

// 集群类型
export const nounClusterType =
(
  <>
      <Tooltip title={<><p>公共集群：Arius团队维护的大集群，成本低，隔离性较弱;</p><p>独享集群、独立集群：隔离性好但成本高；</p></>}>
        <InfoCircleOutlined className="ml-10 mr-5" />
      </Tooltip>
  </>
);

// 集群状=
const clusterTypeEle =
(
  <>
    <p>green: 集群主副分片均正常;</p>
    <p>yellow：集群主分片均正常，存在异常副分片;</p>
    <p>red：集群存在异常主分片;</p>
  </>
);
export const nounClusterStatus =
(
  <>
      <Tooltip title={clusterTypeEle}>
        <InfoCircleOutlined className="ml-10 mr-5" />
      </Tooltip>
  </>
);

// 分区创建
export const nounPartitionCreate  =
(
  <>
      <Tooltip title={<>分区创建：按照时间每天/每月创建物理索引，在性能、稳定性、扩容上更有优势;<br/> 不分区创建：只创建一个物理索引</>}>
          <InfoCircleOutlined className="ml-10 mr-10" />
      </Tooltip>
  </>
);

// 集群权限
export const nounClusterAuthority  =
(
  <>
      <Tooltip title={<>访问权限： 在集群上进行创建索引、查看数据、查询索引等操作；<br/>配置管理：除访问权限外，可修改集群配置，申请扩缩容</>}>
          <InfoCircleOutlined className="ml-10 mr-10" />
      </Tooltip>
  </>
);

// 索引服务
export const nounIndexServer  =
(
  <>
      <Tooltip title={<>xxxx</>}>
          <InfoCircleOutlined className="ml-10 mr-10" />
      </Tooltip>
  </>
);

// 索引权限
const indexAuthorityEle =
(
  <>
    <p>读权限：可实现对索引的查看、查询操作；</p>
    <p>读写权限：在读权限基础上，可向索引中写入数据；</p>
    <p>管理权限：在读写权限基础上，还具备修改索引的配置（如mapping结构）、清理索引、下线索引的能力。</p>
  </>
);
export const nounIndexAuthority  =
(
  <>
      <Tooltip title={indexAuthorityEle}>
          <InfoCircleOutlined className="ml-10 mr-10" />
      </Tooltip>
  </>
);
export const nounAuthority =
(
  <>
      <Tooltip title='访问权限表示您所在的项目可以查看该集群，并在其上申请逻辑索引'>
        <InfoCircleOutlined style={{ color: 'green' }} />
      </Tooltip>
  </>
);

export const renderTip = (t: string, color?: string) => (
  <>
      <Tooltip title={t}>
        <InfoCircleOutlined style={{ color: color ? color : 'green' }} />
      </Tooltip>
  </>
);
