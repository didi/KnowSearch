import React from 'react';
import { Spin, Table, Tooltip } from 'antd';
import './index.less'

interface ITableProps {
    columns: any[],
    dataSource: any[],
    title: string,
    isLoading: boolean,
    tooltip?: JSX.Element | string,
    unit?: string
}

const imgSrc = require('./../../../../assets/empty.png');

const TableCard = (props: ITableProps) => {
    const { columns, dataSource = [], title, tooltip, unit, isLoading } = props
    const renderTitle = () => {
        return (
            <div className="dashboard-table-title" style={{ position: 'relative', height: 22, lineHeight: '22px' }}>
                {title}
                {unit && <span>{`(${dataSource.length}${unit})`}</span>}
                {
                    tooltip ? <Tooltip title={tooltip}><svg className="icon" aria-hidden="true" style={{ width: 12, height: 12, color: '#ADB5BC', position: 'absolute', top: 5, marginLeft: 4, cursor: 'default', zIndex: 999 }}>
                        <use xlinkHref="#iconinfo"></use>
                    </svg></Tooltip> : null
                }
            </div>
        )
    }
    return (
        <div className="dashboard-table-container">
            <Spin spinning={isLoading || false}>
                {renderTitle()}
                {
                    dataSource.length ?
                        <Table
                            columns={columns}
                            dataSource={dataSource}
                            rowClassName={(record, index) => {
                                return index & 1 ? 'even-row' : ''
                            }
                            }
                            pagination={dataSource.length <= 6 ? false : {
                                simple: true,
                                total: dataSource.length,
                                pageSize: 6,
                            }}
                        />
                        : <div className={'dashboard-table-container-empty'}>
                            <div>
                                <img src={imgSrc} />
                            </div>
                            <div>
                                <span>数据为空</span>
                            </div>
                        </div>
                }

            </Spin>
        </div>
    )
}

export default TableCard