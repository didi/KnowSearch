import React from "react";
import { SortableContainerProps } from 'react-sortable-hoc';

import { propsType as IcontainerProps } from '../container';
import DragableContainer from './DragableContainer';
import DragableItem from './DragableItem';

interface ISortableElement1 {
  collection?: string | number;
  disabled?: boolean;
}

interface propsType extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  dragContainerProps?: SortableContainerProps;
  containerProps?: IcontainerProps;
  dragItemProps?: ISortableElement1;
}


const DragGroup: React.FC<propsType> = ({ children, containerProps, dragContainerProps, dragItemProps }) => {

  return (
    <DragableContainer
      dragContainerProps={{
        axis: "xy",
        ...dragContainerProps
      }}
      containerProps={{
        grid: 8,
        gutter: [10, 10],
        ...containerProps
      }}
    >
      {
        React.Children.map(children, (child: any, index) => {
          return (
            <DragableItem
              dragItemProps={{
                index: index,
                ...dragItemProps
              }}
              key={index}>
              {React.cloneElement(child, { key: index })}
            </DragableItem>
          )
        })
      }
    </DragableContainer>
  )

};

export default DragGroup;