import * as React from 'react';
import { Empty, List, Spin, Tabs } from 'antd';
import WindowScroller, { WindowScrollerChildProps } from 'react-virtualized/dist/commonjs/WindowScroller';
import AutoSizer from 'react-virtualized/dist/commonjs/AutoSizer';
import VList, { ListProps } from 'react-virtualized/dist/commonjs/List';
import InfiniteLoader from 'react-virtualized/dist/commonjs/InfiniteLoader';
import './index.less';

import utils from '../../Utils/index';

const emptyImg = require('./assets/empty.png');

interface IMsg {
  title: string;
  content: string;
  timestamp: string;
  createTime: string;
}
const { TabPane } = Tabs;

export const MsgPanel: React.FC = () => {
  const [data, setData] = React.useState<IMsg[]>([]);
  const [msgCount, setMsgCount] = React.useState(0);
  const [status, setStatus] = React.useState('unread');
  const [loading, setLoading] = React.useState(false);
  const loadedRowsMap = {} as {
    [key: number]: number;
  };
  const queryProjectList = (params): any => {
    // return Promise.resolve([
    //   {
    //     content: "ejqwkjdkqwdksajdjkasdksjajdksajdksajhdjkhsajkdhkjsahdkjshdkjsahdwuyeuahdmasbmndbamsbdmnsabdnsa",
    //     createTime: "00:00:00",
    //     id: 0,
    //     isRead: true,
    //     title: "操作消息说就是看电视卡等级",
    //     timestamp: "22312312312",
    //   },
    // ]);
    return utils.request(`/v1/message/list`, params);
  };
  const getData = async (params = "") => {
    setLoading(true);
    const fakeData: any = await queryProjectList(params);
    console.log(fakeData);
    setData(fakeData);
    setMsgCount(0);
    setLoading(false);
  };

  const onHandleAllMsg = () => {
    //
  };

  React.useEffect(() => {
    getData();
  }, []);

  const handleInfiniteOnLoad = ({ startIndex, stopIndex }: { startIndex: number; stopIndex: number }): any => {
    setLoading(true);
    for (let i = startIndex; i <= stopIndex; i++) {
      // 1 means loading
      loadedRowsMap[i] = 1;
    }
    if (data.length > 10) {
      setLoading(false);
      return;
    }
    getData();
    return;
  };

  const isRowLoaded = ({ index }: { index: number }) => !!loadedRowsMap[index];

  const renderItem = ({ index, key, style }: { index: number; key: string; style: React.CSSProperties | any }) => {
    const item = data[index] || ({} as IMsg);
    return (
      <List.Item key={key} style={style}>
        <List.Item.Meta title={<span className={status}>{item.title}</span>} description={<span className={status}>{item.content}</span>} />
        <div className="right-text">{item.createTime}</div>
      </List.Item>
    );
  };

  const vlist = ({ height, isScrolling, onChildScroll, scrollTop, onRowsRendered, width }: ListProps) => (
    <VList
      autoHeight
      height={height}
      isScrolling={isScrolling}
      onScroll={onChildScroll}
      overscanRowCount={2}
      rowCount={data.length}
      rowHeight={78}
      rowRenderer={renderItem}
      onRowsRendered={onRowsRendered}
      scrollTop={scrollTop}
      width={width}
      style={{ paddingLeft: 16, paddingRight: 16 }}
    />
  );
  const autoSize = ({ height, isScrolling, onChildScroll, scrollTop, onRowsRendered }: ListProps) => (
    <AutoSizer disableHeight>
      {({ width }: { width: number }) =>
        vlist({
          height,
          isScrolling,
          onChildScroll,
          scrollTop,
          onRowsRendered,
          width,
        } as unknown as ListProps)
      }
    </AutoSizer>
  );
  const infiniteLoader = ({ height, isScrolling, onChildScroll, scrollTop }: WindowScrollerChildProps) => (
    <InfiniteLoader isRowLoaded={isRowLoaded} loadMoreRows={handleInfiniteOnLoad} rowCount={data.length}>
      {({ onRowsRendered }: any) =>
        autoSize({
          height,
          isScrolling,
          onChildScroll,
          scrollTop,
          onRowsRendered,
        } as unknown as ListProps)
      }
    </InfiniteLoader>
  );

  return (
    <>
      <div className="msg-panel">
        <>
          <Tabs className="tabs" defaultActiveKey={status} onChange={setStatus}>
            <TabPane tab={`未读(${msgCount})`} key="unread"></TabPane>
            <TabPane tab="已读" key="read"></TabPane>
          </Tabs>
          <a className="right-btn" onClick={onHandleAllMsg}>
            全部消息
          </a>
        </>
        {data.length === 0 && !loading && <Empty image={emptyImg} />}
        {data.length > 0 && (
          <List className="list">
            {<WindowScroller>{infiniteLoader}</WindowScroller>}
            {loading && <Spin className="loading" />}
          </List>
        )}
      </div>
    </>
  );
};
