import React, { FC, memo, useEffect, useState } from 'react'
import { shallowEqual, useDispatch, useSelector } from 'react-redux';

import { EditorComLoading } from './editor-com-loading';
import { getSetting } from 'api/index-admin';

interface propsType {
  data: any
}

export const Setting: FC<propsType> =  memo(({data}) => {
  const clusterName =  data.cluster || "";
  const indexName = data.index || "";

  const [settingData, setSettingData] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const getAsyncSetting = async () => {
    setIsLoading(true);
    try {
      const res = await getSetting(clusterName, indexName);
      res?.properties ? setSettingData(JSON.stringify(res.properties, null, 2)) : setSettingData("");
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  }
  
  useEffect(() => {
    getAsyncSetting();
  }, []);

  console.log(settingData);

  return (
    <div>
      <EditorComLoading loading={isLoading} data={settingData} />
    </div>
  )
})
