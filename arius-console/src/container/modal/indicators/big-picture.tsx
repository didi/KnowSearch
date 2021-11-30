import { Button, Modal } from "antd";
import React, { useState } from "react";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";
import { DrawLine } from "container/indicators-kanban/components/line";

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const BigPicture = connect(mapStateToProps)(
  (props: { dispatch: Dispatch; params: any; cb: Function }) => {
    React.useEffect(() => {
      console.log(props.params);
    }, []);

    return (
      <>
        <Modal
          visible={true}
          title={props.params?.title.text}
          width={"calc(70vw)"}
          onCancel={() => props.dispatch(actions.setModalId(""))}
          footer={[
            <Button
              key="back1"
              onClick={() => props.dispatch(actions.setModalId(""))}
            >
              取消
            </Button>,
          ]}
        >
          <DrawLine
            index={"ele-id" + "modal"}
            option={props.params}
            bigPicture={true}
          />
        </Modal>
      </>
    );
  }
);
