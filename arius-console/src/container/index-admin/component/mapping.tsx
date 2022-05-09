import { getMapping } from 'api/index-admin';
import React, { FC, memo, useEffect, useState } from 'react'
import { shallowEqual, useDispatch, useSelector } from 'react-redux';
import { EditorComLoading } from './editor-com-loading';

interface propsType {
  data: any
}

export const Mapping: FC<propsType> =  memo(({data}) => {
  const clusterName =  data.cluster || "";
  const indexName = data.index || "";

  const [mappingData, setMappingData] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const getAsyncMapping = async () => {
    setIsLoading(true);
    try {
      const res = await getMapping(clusterName, indexName);
      res?.typeProperties?.properties ? setMappingData(JSON.stringify(res.typeProperties.properties, null, 2)) : "";
    } catch (error) {
      console.log(error);
    } finally {
      setIsLoading(false);
    }
  }
  
  useEffect(() => {
    getAsyncMapping();
  }, []);

  return (
    <div>
      <EditorComLoading loading={isLoading} data={mappingData} />
    </div>
  )
})
