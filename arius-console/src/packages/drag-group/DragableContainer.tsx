import React from "react";
import Container from '../container/index';
import { SortableContainer, SortableContainerProps } from 'react-sortable-hoc';
import { propsType as IcontainerProps } from '../container';

interface propsType extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  dragContainerProps?: SortableContainerProps;
  containerProps?: IcontainerProps;
}

const SortableCon = SortableContainer(({ children, containerProps }) => (
  <Container
    {...containerProps}
  >
    {children}
  </Container>
))

const DragableContainer: React.FC<propsType> = ({ children, dragContainerProps, containerProps }) => {

  return (
    <SortableCon
      {...dragContainerProps}
      containerProps={containerProps}
    >
      {children}
    </SortableCon>
  )

};

export default DragableContainer;