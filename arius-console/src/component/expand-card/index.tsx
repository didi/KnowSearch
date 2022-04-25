import Icon from '@ant-design/icons';
import React from 'react';
import './index.less';

interface ICardProps {
  title: string;
  expand?: boolean;
  charts?: JSX.Element[];
  noTitle?: boolean;
}

export class ExpandCard extends React.Component<ICardProps> {
  public state = {
    innerExpand: true,
  };

  public handleClick = () => {
    this.setState({ innerExpand: !this.state.innerExpand });
  }

  public render() {
    let { expand } = this.props;
    const { noTitle } = this.props;
    if (expand === undefined) expand = this.state.innerExpand;
    const { charts } = this.props;
    return (
      <div className="card-wrapper">
        {!noTitle ? <div className="card-title" onClick={this.handleClick}>
          <Icon type={expand ? 'down' : 'up'} />
          {this.props.title}
        </div> : null}
        {expand ?
          <div className="card-content">
            {(charts || []).map((c, index) => {
                if (index % 2 !== 0) return null;
                return (
                  <div className="chart-row" key={index}>
                    <div className="chart-wrapper">{c}</div>
                    {(index + 1 < charts.length) ? <div className="chart-wrapper">{charts[index + 1]}</div> : null}
                  </div>
                );
              })
            }
          </div> : null}
      </div>
    );
  }
}
