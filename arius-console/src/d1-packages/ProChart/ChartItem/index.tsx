import React from 'react';
import classNames from 'classnames';
import './style.less';

interface IChartItem {
  titleText?: string;
  titleIcon?: string;
  children?: React.ReactNode;
  className?: string;
}

const ChartItem = (props: IChartItem): JSX.Element => {
  const { titleIcon, titleText, children, className } = props;
  return (
    <div className={classNames('chart-item', className)}>
      <div className="chart-item-title">
        <span className="chart-item-title-icon">
          <svg style={{ width: 16, height: 16 }} aria-hidden="true" fill="#888FAB">
            <use xlinkHref={titleIcon}></use>
          </svg>
        </span>
        <span className="chart-item-title-text">{titleText}</span>
      </div>
      <div className="chart-item-content">{children}</div>
    </div>
  );
};

export default ChartItem;
