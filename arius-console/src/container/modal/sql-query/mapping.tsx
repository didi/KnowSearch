import React, { useState } from "react";
import { Modal, Form, Select, Spin, Table } from "knowdesign";
import { connect } from "react-redux";
import * as actions from "../../../actions";
import { Dispatch } from "redux";

const FormItem = Form.Item;

const mapStateToProps = (state: any) => ({
  params: state.modal.params,
  cb: state.modal.cb,
});

export const Mapping = connect(mapStateToProps)((props: { dispatch: Dispatch; params: any; cb: Function }) => {
  const [originSelectMapJson] = useState(props.params.selectMapJson || []);
  const [selectMapJson, setSelectMapJson] = useState(props.params.selectMapJson || []);
  const [mappingJson, setMappingJson] = useState(props.params.mappingJson || []);
  const [originMappingJson] = useState(props.params.mappingJson || []);
  const mappingHandleSearch = (value) => {
    console.log("originSelectMapJson", originSelectMapJson, value);
    setSelectMapJson(originSelectMapJson.filter((row) => row.value.includes(value)));
  };
  const onMappingChange = (value) => {
    console.log("originMappingJson", originMappingJson, value);
    setMappingJson(value ? originMappingJson.filter((row) => row.value.includes(value)) : originMappingJson);
  };

  return (
    <>
      <Modal
        visible={true}
        title={props.params?.title}
        width={600}
        height={440}
        onCancel={() => props.dispatch(actions.setModalId(""))}
        footer={null}
      >
        <Form>
          <FormItem key="mapping" name="mapping" label="" className="mapping-input">
            <Select
              style={{ width: "321px" }}
              showSearch
              options={selectMapJson}
              allowClear
              placeholder="Mapping"
              defaultActiveFirstOption={false}
              filterOption={false}
              onSearch={mappingHandleSearch}
              onChange={onMappingChange}
            />
          </FormItem>
        </Form>
        <div className="mapping-tab">
          <Spin spinning={props.params.tableLoading}>
            <Table
              rowKey="name"
              dataSource={mappingJson}
              columns={props.params.columns}
              scroll={{ y: 225 }}
              pagination={{
                simple: true,
                pageSize: 5,
                size: "small",
              }}
            />
          </Spin>
        </div>
      </Modal>
    </>
  );
});
