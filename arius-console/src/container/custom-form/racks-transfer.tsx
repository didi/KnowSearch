import { INewRegionRacks, INewRegionRacksMap } from '@types/cluster/physics-type';
import { INodeDivide } from '@types/index-types';
import { getPhyClusterRacks } from 'api/op-cluster-region-api';
import  { Transfer } from 'antd';
import * as React from 'react';

interface IRacksTransfer {
  onChange?: (e: any) => any;
  value?: string[];
  isDisabled?: boolean;
  clusterName: string;
  nodeDivideList: INodeDivide[];
  editData?: INodeDivide;
}

// racks穿梭框
export class RacksTransfer extends React.Component<IRacksTransfer> {
  public state = {
    regionRacks: [] as INewRegionRacksMap[],
    targetKeys: [] as any,
  };

  public componentDidMount() {
    const { clusterName, nodeDivideList, editData } = this.props;
    const regionIdArr = nodeDivideList?.filter(item => (item.regionId !== '_'));
    const racks = regionIdArr.map(item =>item.rack);
    getPhyClusterRacks(clusterName).then((res: INewRegionRacks[]) => {
      let dataSource = res.map((item, index) => {
        return {
          key: item.rack,
          disabled: (item.usageFlags || racks.indexOf(item.rack) > -1) ? true : false,
          title: item.rack,
          description: `clusterName:${item.cluster}, rack: ${item.rack}`,
        };
      });

      let targetKeys = [];

      if (editData) {
        const arr = regionIdArr.filter(item => item.regionId === editData.regionId);
        targetKeys = Array.from(new Set(arr.map((item) => item.rack)));
        dataSource = dataSource.map((item) => {
          return {
            key: item.key,
            disabled: targetKeys.indexOf(item.title) > -1 ? false : item.disabled,
            title: item.title,
            description: item.description,
          };
        });
      }

      this.setState({
        regionRacks: dataSource,
        targetKeys,
      });
    })
  }

  public filterOption = (inputValue: any, option: any) => option.description.indexOf(inputValue) > -1;

  public handleChange = (targetKeys: any) => {
    this.setState({ targetKeys });
    const { onChange } = this.props;
    // tslint:disable-next-line:no-unused-expression
    onChange && onChange(targetKeys);
  }

  public render() {
    return (
      <Transfer
        dataSource={this.state.regionRacks}
        showSearch={true}
        filterOption={this.filterOption}
        targetKeys={this.state.targetKeys}
        onChange={this.handleChange}
        render={item => item.title}
      />
    );
  }
}
