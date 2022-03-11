import React, { ReactNode, useEffect, useState } from 'react';
import { Button, Popover, Tag, Tooltip } from 'antd';
import Container from '../../ProLayout/Container';
import { optionItemType, propsType, BASIS_TYPE } from '../type';
import { copyContentFn } from '../../Utils/tools';
import './basis-info.less';

const classNamePrefix = 'basis-info';

// 渲染需要处理的详情内容的方法
export const renderColumnTagShow = (optionItem: optionItemType): string | ReactNode | any => {
  const { customType, content, render } = optionItem ?? {};
  if (render) {
    if (typeof render === 'function') {
      return render(content);
    }
    return;
  }

  switch (customType) {
    case BASIS_TYPE.tag:
      if (!Array.isArray(content)) return <Tag>{content}</Tag> ?? '-';
      if (content.length <= 3)
        return content.map((item: React.ReactNode, index: string | number | null | undefined) => <Tag key={index}>{item}</Tag>);
      return (
        <div className={`${classNamePrefix}-table-item-tags`}>
          {content.slice(0, 3).map((item: React.ReactNode, key: string | number | null | undefined) => (
            <Tag key={key}>{item}</Tag>
          ))}
          <Popover
            placement="bottom"
            content={
              <div className="table-popover">
                {content.map((item: React.ReactNode, key: string | number | null | undefined) => (
                  <Tag key={key}>{item}</Tag>
                ))}
              </div>
            }
            trigger="hover"
          >
            <Button className="item-content" size="small" type="dashed">
              共{content.length}个
            </Button>
          </Popover>
        </div>
      );
    case BASIS_TYPE.highlight:
      if (!Array.isArray(content)) {
        return <span style={{ color: '#1890ff' }}>{content ?? '-'}</span>;
      } else {
        return <span style={{ color: '#1890ff' }}>{`${content.length}个`}</span>;
      }

    case BASIS_TYPE.editable:
    default:
      break;
  }
};

// 将数据处理成符合Description的格式
export const getBasisInfoConfig = (data: any, basisInfoConfig: optionItemType[]): optionItemType[] => {
  const list = basisInfoConfig.map((item) => {
    item.content = data[item.key] ?? '-';
    return item;
  });
  return list;
};

export const BasisInfo: React.FC<propsType> = (props: propsType) => {
  const { title, dataSource, config, labelWidth, labelStyle, titleStyle, xl = 8, xxl = 6, needColon = false } = props;
  const [optionList, setOptionList] = useState(getBasisInfoConfig(dataSource, config))
  // 默认的总格数
  const gridTotal = 24;

  // 计算小于1920屏幕下所有详情占用的对应格数
  const gridXl = () => optionList.map((item) => {
    if (item.span && item.span > gridTotal / xl) {
      return gridTotal;
    }
    return item.span ? item.span * xl : xl;
  });

  // 计算大于1920屏幕下所有详情占用的对应格数
  const gridXxl = () => optionList.map((item) => {
    if (item.span && item.span > gridTotal / xl) {
      return gridTotal;
    }
    return item.span ? item.span * xxl : xxl;
  });

  useEffect(() => {
    setOptionList(props.getBasisInfoConfig ? props.getBasisInfoConfig(dataSource, config) : getBasisInfoConfig(dataSource, config))
    return () => {
      setOptionList([])
    }
  }, [dataSource, config])

  return (
    <div className={`basis-info`}>
      {title && (
        <div className="basis-title" style={titleStyle}>
          {title}
        </div>
      )}
      {/* Container布局组件，当前awd为自适应布局 */}
      <Container gutter={10} awd={true} xl={gridXl()} xxl={gridXxl()}>
        {optionList.map((item) => {
          return (
            <div key={item.key} className={`basis-info-item`}>
              <span className={`basis-info-item-label${needColon ? ' needColon' : ''}`} style={{ width: labelWidth || 80, ...labelStyle, ...item.labelStyle }}>
                {item.label}
              </span>
              {console.log(item, item.content.length, 'item')}
              <div className={`basis-info-item-content`}>
                {(item.render || item.customType) && item.content ? (
                  renderColumnTagShow(item)
                ) : item?.content && JSON.stringify(item?.content)?.length > 30 ? (
                  <Tooltip placement="bottomLeft" title={item?.content}>
                    {item?.content}
                  </Tooltip>
                ) : (
                      <span>{item.content}</span>
                    )}
                {item.copy ? (
                  <svg
                    onClick={() => {
                      copyContentFn(item?.content as string);
                    }}
                    style={{ width: 16, height: 16, cursor: 'pointer' }}
                    aria-hidden="true"
                  >
                    <use xlinkHref="#icon-fuzhi"></use>
                  </svg>
                ) : null}
              </div>
            </div>
          );
        })}
      </Container>
    </div>
  );
};

