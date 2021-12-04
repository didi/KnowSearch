import { Drawer } from 'antd';
import React from 'react';
import { connect } from "react-redux";
import * as actions from 'actions';


const mapStateToProps = (state: any) => ({
  params: state.modal.params,
});
class ConfigDetail extends React.Component<any> {

  public render() {
    const { dispatch } = this.props
    return (
      <Drawer
        title={'配置内容'}
        visible={true}
        onClose={() => dispatch(actions.setDrawerId(''))}
        width={600}
        maskClosable={true}
      >
        <pre>{this.props.params}</pre>  
      </Drawer>
    );
  }
}
export default connect(mapStateToProps)(ConfigDetail);


