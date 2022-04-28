---
order: 1
title: card拖拽
---

card拖拽示例

``` tsx
import React, { useState } from 'react';
import DragGroup from '../DragGroup';
import { arrayMoveImmutable } from 'array-move';
import Card from '../../../basic/card';
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
  }
]    
const Containerskk = (): JSX.Element => {
  const [lists, setlists] = useState(JSON.parse(JSON.stringify(datas)));
  const sortEnd = ({ oldIndex, newIndex, collection }) => {
    const listsNew = arrayMoveImmutable(lists, oldIndex, newIndex)
    setlists(listsNew);
  };       
  return (
    <>
      <DragGroup
        dragContainerProps={{
          onSortEnd: sortEnd,
          axis: "xy"
        }}
      >
        {lists.map((item, index) => (
          <Card title={item.name} key={index}>
            <p>Card content</p>
            <p>Card content</p>
            <p>Card content</p>
          </Card>
        ))}
      </DragGroup>           
    </>
  )
}

ReactDOM.render(
  <div>
    <Containerskk />
  </div>,
  mountNode,
);
```

```css
.drag-sort-item {
  
}

```