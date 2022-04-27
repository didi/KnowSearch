import React, { memo, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Menu, PageHeader, Spin, Tag } from 'antd';

import { HashMenu, IMenuItem } from 'component/hash-menu';
import { InfoItem } from 'component/info-item';
import { renderMoreBtns } from 'container/custom-component';

import { getBtnList, TAB_LIST, MENU_MAP, DESC_LIST  } from './config';
import * as actions from "actions";
import { getIndexDetail } from 'api/index-admin';
import urlParser from 'lib/url-parser';

export const IndexAdminDetail =  withRouter((props) => {
  const { history } = props;

  const [indexBaseInfo, seIndexBaseInfo] = useState({});
  
  const [isLoading, setIsLoading] = useState(false);

  const dispatch = useDispatch();

  const setModalId = (modalId: string, params?: any, cb?: Function) => {
    dispatch(actions.setModalId(modalId, params, cb));
  }

  const reload = (del?: boolean) => {
    if (del) {
      history.replace('/index-admin');
      return;
    }
    getData();
  }

  const getData = async () => {
    const { search } = urlParser();
    setIsLoading(true);
    try {
      const data = await getIndexDetail(search.cluster, search.index);
      seIndexBaseInfo(data);
    } catch (error) {
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    getData();
  }, []);

  const renderPageHeader = () => {
    const btnList = renderMoreBtns(getBtnList(indexBaseInfo, setModalId, null, reload), {});
    
    return (
      <PageHeader
        className="detail-header"
        backIcon={false}
        title={indexBaseInfo["index"] || ""}
        extra={btnList}
        tags={
          <Tag color={indexBaseInfo["health"]}>
            {indexBaseInfo["health"]}
          </Tag>
        }
      >
        {DESC_LIST.map((row, index) => (
          <InfoItem
            key={index}
            label={row.label}
            value={
              row.render
                ? row.render(indexBaseInfo?.[row.key])
                : `${indexBaseInfo?.[row.key] || ""}`
            }
            width={250}
          />
        ))}
      </PageHeader>
    );
  }

  return (
    <div>
      <Spin spinning={isLoading}>
        {renderPageHeader()}
        <HashMenu  TAB_LIST={TAB_LIST} MENU_MAP={MENU_MAP} defaultHash={"baseInfo"} data={indexBaseInfo}/>
      </Spin>
    </div>
  )
});

