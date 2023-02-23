---
order: 0
title: 自定义拖拽项&拖拽分组
---

DragGroup示例

``` tsx
import React, { useState } from 'react';
import DragGroup from '../DragGroup';
import { arrayMoveImmutable } from 'array-move';

let datas = [
  {
    id: 1,
    name: 'item1'
  },
  {
    id: 2,
    name: 'item2'
  },
  {
    id: 3,
    name: 'item3'
  },
  {
    id: 4,
    name: 'item4'
  },
  {
    id: 5,
    name: 'item5'
  },
  {
    id: 6,
    name: 'item6'
  }
]
let datas2 = [
  {
    id: 1,
    name: 'item1-2'
  },
  {
    id: 2,
    name: 'item2-2'
  },
  {
    id: 3,
    name: 'item3-2'
  },
  {
    id: 4,
    name: 'item4-2'
  },
  {
    id: 5,
    name: 'item5-2'
  },
  {
    id: 6,
    name: 'item6-2'
  }
]

const DragItem = (props) => {
  return (
    <div>{props.name}</div>
  )
}

const Containers = (): JSX.Element => {
    
  const [lists, setlists] = useState(JSON.parse(JSON.stringify(datas)));
  const [lists2, setlists2] = useState(JSON.parse(JSON.stringify(datas2)));

  const sortEnd = ({ oldIndex, newIndex, collection }) => {
    let datas = [];
    let setData;
    switch (collection) {
      case 'group1':
        datas = lists;
        setData = setlists;
        break;
      case 'group2':
        datas = lists2;
        setData = setlists2;
        break;
      default:
        break;
    }
    const listsNew = arrayMoveImmutable(datas, oldIndex, newIndex)
    setData(listsNew);
  };
  return (
      <>
        <h4>拖拽组1</h4>
        <DragGroup 
          dragContainerProps={{
            onSortEnd: sortEnd,
            axis: "xy"
          }}
          dragItemProps={{
            collection: 'group1'
          }}
        >
          {lists.map((item, index) => (
            <DragItem key={index} name={item.name} id={item.id}/>
          ))}
          
        </DragGroup> 
        <br/>

        <h4>拖拽组2</h4>
        <DragGroup 
          dragContainerProps={{
            onSortEnd: sortEnd,
            axis: "xy"
          }}
          dragItemProps={{
            collection: 'group2'
          }}
        >
          {lists2.map((item, index) => (
            <DragItem key={index} name={item.name} id={item.id}/>
          ))} 
        </DragGroup>             
      </>
  )
}

ReactDOM.render(
  <div>
    <Containers />
  </div>,
  mountNode,
);
```

```css
.drag-sort-item {
  background: #4482D4;
  border: 1px solid #50A5F1;
  color: #fff;
}

```