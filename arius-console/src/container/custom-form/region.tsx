import React from 'react';
import  { Select, Spin } from 'antd';
import { connect } from "react-redux";
import * as actions from 'actions';
import { getPhyClusterRegionListBind, getBindRack } from 'api/op-cluster-region-api';
import { getOpPhysicsClusterList } from 'api/cluster-api';

const mapStateToProps = (state) => ({
  region: state.region,
  params: state.modal.params,
});

export const AllPhyCluster = connect(mapStateToProps)((props: any) => {
  const { onChange, clusterPhyNameList, workid, clusterLogicName, diskQuota } = props;

  const [val, setVal] = React.useState(clusterPhyNameList[0] || "");

  const options = clusterPhyNameList?.length ? clusterPhyNameList.map(item => ({
    value: item, 
    labe: item
  })) : [];

  React.useEffect(() => {
    onChange && onChange(val);
    val && getRegionList(val);
  }, []);

  const handleSelect = (value) => {
    setVal(value);
    getRegionList(value);
    onChange && onChange(value);
  }

  const getRegionList = (e: string) => {
    getBindRack(e, clusterLogicName, diskQuota).then((res) => {
      const arr = [];
      res.forEach(element => {
        if (element.indexOf(',') != -1) {
          const strArr = element.split(',')
          strArr.forEach(item => {
            arr.push({value: item, label: item});
          })
        } else {
          arr.push({value: element, label: element});
        }
      });
      props.dispatch(actions.setRacksArr(arr));
    });
  }

  return (
    <>
      <Select
          placeholder="请选择物理集群"
          value={val}
          onChange={(value) => handleSelect(value)}
          options={options} 
          style={{width: '50%'}}>
      </Select>
    </>
  );
});

export const PhyClusterRacks = connect(mapStateToProps)((props: any) => {
  const handleSelect = (value) => {
    const { onChange } = props;
    onChange && onChange(value);
  }

  return (
    <>
      <Select
          placeholder="请选择racks"
          mode="multiple"
          onChange={handleSelect}
          options={props.region?.racksArr} 
          style={{width: '50%', height: '35px'}}>
      </Select>
    </>
  );
});


const PhyClusterSelect = (props) => {
  const handleSelect = (value) => {
    getRegionList(value);
    const { onChange } = props;
    onChange && onChange(value);
  }

  const getRegionList = (e: string) => {
    getPhyClusterRegionListBind(e, props.params?.type, props.params?.id).then((res) => {
      res = res.map(item => {
        return {
          label: `${item.id}(racks: ${item.racks})`,
          value: JSON.stringify({id: item.id, racks: item.racks}),
          racks: item.racks,
          // disabled: (item.logicClusterId === -1) ? false : true,
        };
      });
      props.dispatch(actions.setRegionList(res));
    });
  }

  React.useEffect(() =>  {
    if (props.value) {
      getRegionList(props.value);
    }
  }, [])

  return (
    <>
      <Select
          placeholder="请选择物理集群"
          onChange={(value) => handleSelect(value)}
          options={props.region?.phyClusterList} 
          style={{width: '50%'}}
          disabled={props.disabled || false}
          value={props.value}>
      </Select>
    </>
  );
};
export const PhyCluster = connect(mapStateToProps)(PhyClusterSelect);

const RegionSelect = (props) => {
  const handleSelect = (value) => {
    const racks = value.map(item => JSON.parse(item)?.racks);
    const ids = value.map(item => JSON.parse(item)?.id);
    props.dispatch(actions.setRacks(racks.toString()));
    const { onChange } = props;
    onChange && onChange(ids);
  }

  return (
    <>
      <Select
          placeholder="请选择region"
          mode="multiple"
          onChange={handleSelect}
          options={props.region?.region} 
          style={{width: '50%'}}>
      </Select>
    </>
  );
};
export const LogRegionSelect = connect(mapStateToProps)(RegionSelect);

const RegionRacksSelect = (props) => {
  return (
    <>
      <span>{props.region?.racks}</span>
    </>
  );
};
export const RegionRacksText = connect(mapStateToProps)(RegionRacksSelect);