import * as React from 'react';
import { DataCurveFilter } from 'container/time-panel';
import { ExpandCard } from 'component/expand-card';
import { ICurveType, allCurves, CURVE_KEY_MAP, indexCurveKeys } from 'container/common-curve/config';
import { CommonCurve } from 'container/common-curve';
import { curveInfo } from 'store/curve-info';
import { AccessStatistic } from './statistic';

export class MonitorPage extends React.Component {

  public refreshAll = () => {
    Object.keys(indexCurveKeys).forEach((c: string) => {
      const { typeInfo, curveInfo: option } = CURVE_KEY_MAP.get(c);
      const { parser } = typeInfo;
      curveInfo.getCommonCurveData(option, parser, true);
    });
  }

  public getCurves = (curveType: ICurveType) => {
    return curveType.curves.map(o => {
      if (o.path === indexCurveKeys.accessDetail) {
        return <AccessStatistic options={o}/>;
      }
      return <CommonCurve key={o.path} options={o} parser={curveType.parser} />;
    });
  }

  public render() {
    return (
      <div className="curve-wrapper">
        <DataCurveFilter refreshAll={this.refreshAll} />
        {allCurves.map(c => {
          return <ExpandCard key={c.type} title={c.title} charts={this.getCurves(c)} />;
        })}
      </div>
    );
  }
}
