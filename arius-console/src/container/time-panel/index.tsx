import React from 'react';
import './index.less';
import { PERIOD_RADIO_MAP, PERIOD_RADIO } from 'constants/time';
import { Radio, DatePicker, Button } from 'antd';
import moment from 'moment';

interface IDataCurveFilterProps {
  refreshAll?: any;
  setTimeRange?: any;
  radioValue?: string;
  timeRange?: [moment.Moment, moment.Moment];
  noRadioBtn?: boolean;
  noFreshBtn?: boolean;
}

export class DataCurveFilter extends React.Component<IDataCurveFilterProps> {
  public handleRangeChange = (dates: any, dateStrings: [string, string]) => {
    this.setCustomTimeRange(dates);
    // curveInfo.setTimeRange(dates as [moment.Moment, moment.Moment]);
    this.refresh();
  }

  public radioChange = (e: any) => {
    const { value } = e.target;
    this.setCustomTimeRange(PERIOD_RADIO_MAP.get(value).dateRange);
    // curveInfo.setTimeRange(PERIOD_RADIO_MAP.get(value).dateRange);
    this.refresh();
  }

  public setCustomTimeRange = (dates: any) => {
    const { setTimeRange } = this.props;
    if (setTimeRange) {
      setTimeRange(dates as [moment.Moment, moment.Moment]);
    }
  }

  public refresh = () => {
    const { refreshAll } = this.props;
    if (refreshAll) {
      refreshAll();
    }
  }

  public render() {
    const { noRadioBtn, noFreshBtn, radioValue, timeRange } = this.props;

    return (
      <div className="curve-wrapper">
        {noRadioBtn ? null : <Radio.Group onChange={this.radioChange} defaultValue={radioValue || curveInfo.periodKey}>
          {PERIOD_RADIO.map(p => <Radio.Button key={p.key} value={p.key}>{p.label}</Radio.Button>)}
        </Radio.Group>}
        <DatePicker.RangePicker
          showTime={true}
          onChange={this.handleRangeChange}
          className="ml-10"
          value={timeRange || [moment().subtract(6, 'hour'), moment()]}
        />
        {noFreshBtn ? null : <div className="right-btn">
          <Button onClick={this.refresh}><i className="dsui-icon-shuaxin1 mr-4" />刷新</Button>
        </div>}
      </div>
    );
  }
}
