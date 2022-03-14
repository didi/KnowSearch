import { Col, Row } from 'antd';
import * as React from 'react';
import './index.less'

export interface IBaseDetail {
  title?: string | JSX.Element;
  columns: IItem[][];
  baseDetail: object;
}

interface IItem {
  label: string;
  key: string;
  render?: (value: any) => string | JSX.Element
}

export const BaseDetail = (props: IBaseDetail) => {

  return (<>
        {
          props.title ?
          <div className='detail-base-info-title'>
            { props.title }
          </div>
          : 
          null
        }
        <div className='detail-base-info-box'>
          {
            props.columns.map((itemArr: IItem[], index: number) => (
              <Row className={`detail-base-info-box-row ${ (index % 2) != 0 ? 'detail-base-info-box-activ' : ''}`}  key={index + 'row'}>
                {itemArr.map((item: IItem, indexC: number)=> (
                  <Col span={12} className='detail-base-info-box-row-col' key={indexC + 'col'}>
                    <div className='detail-base-info-box-row-col-label'>
                      {item?.label}
                    </div>
                    <div className='detail-base-info-box-row-col-content'>
                      {item?.render ? item.render(props.baseDetail?.[item.key]) : props.baseDetail?.[item.key]}
                    </div>
                  </Col>
                ))}
              </Row>
            ))
          }
        </div>
  </>)
}