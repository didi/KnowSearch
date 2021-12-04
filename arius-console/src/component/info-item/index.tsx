import React from 'react';
import './index.less';

export interface IInfoItemProps {
  label: string | number;
  value: string | number | JSX.Element;
  width?: number;
  marginTop?: number;
}

export class InfoItem extends React.Component<IInfoItemProps> {
  public render() {
    const { label, value } = this.props;
    let { width, marginTop } = this.props;
    if (!width) width = 250;
    if (!marginTop) marginTop = 24;
    return (
      <div className="info-item-wrapper" style={{ width: `${width}px` }}>
        <label>{label}: </label>
        <div className="ml-5">{value}</div>
      </div>
    );
  }
}
