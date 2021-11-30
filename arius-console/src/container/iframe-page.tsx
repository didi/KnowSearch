import React from 'react';


interface IProps {
  src?: string;
  className?: string;
}
export const PageIFrameContainer = (props: IProps) => {

  const [loading, setLoading] = React.useState(false);

  return (
    <iframe style={{ display: loading ? 'none' : '' }} className={`iframe-page ${props.className}`} src={props.src} />
  );
};
