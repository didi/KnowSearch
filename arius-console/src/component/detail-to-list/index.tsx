import React, { useEffect, useState } from 'react';
import { withRouter } from 'react-router';
import { detailToListMap } from './config';

const DetailToList: React.FC = (props: any) => {
  const department: string = localStorage.getItem("current-project");
  const [oldDepartment, setOldDepartment] = useState(localStorage.getItem("current-project"))

  useEffect(() => {
    if (department !== oldDepartment && detailToListMap[location.pathname]) {
      props.history?.push(detailToListMap[location.pathname]);
      setOldDepartment(department)
    }
  }, [department])
  return (
    <span></span>
  )
}

export default withRouter(DetailToList)