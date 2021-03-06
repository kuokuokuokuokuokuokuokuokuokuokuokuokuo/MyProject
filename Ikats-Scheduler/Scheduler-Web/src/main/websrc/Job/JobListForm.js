import React, {Component} from 'react';
import { observer } from "mobx-react";
import {Row,Col,Form,Input,Select,Icon,Button,Modal} from 'antd';
const FormItem = Form.Item;
const Option = Select.Option;
const confirm = Modal.confirm;
import Css from "./JobStyle";
import NewJobForm from "./NewJobForm";



@observer
class ProductComponent extends Component
{
    constructor(props){
        super(props);
        this.state={
            visible: false
        };
        props.postForm(props.form);
    }
    pauseAll=()=>{
        confirm
        ({
            title: '确定要暂停所有任务吗？',
            onOk:()=>{
                this.props.store.pauseAllJob();
            },
            onCancel(){}
        })
    };
    resumeAll=()=>{
        confirm
        ({
            title: '确定要恢复所有任务吗？',
            onOk:()=>{
                this.props.store.resumeAllJob();
            },
            onCancel(){}
        })
    };
    search=()=>{
        this.props.store.selectDmsRecord();
    };
    getNewJobForm(form) {
        this.newJobForm = form;
    };
    showModal = () => {
        this.setState({
            visible: true
        });
    };
    handleOk = (e) => {
        this.newJobForm.validateFields((err, data) =>
        {
            if(!err)
            {
                this.newJob = data;
                this.props.store.addJob(this.newJob);
                this.setState({
                    visible: false,
                });
            }
        });
    };
    handleCancel = (e) => {
        this.setState({
            visible: false
        });
    };
    render(){
        let {style}=Css;
        const {getFieldDecorator}=this.props.form;
        let formItemLayout = {
            labelCol: {
                xs: { span: 24 },
                sm: { span: 8 },
            },
            wrapperCol: {
                xs: { span: 24 },
                sm: { span: 14 },
            },
        };
        return(
            <div style={style.ListSearchTable}>
                <Row style={{margin:"0 10px"}}>
                    <Col span="3">
                        <Button style={style.ListAddBtn} onClick={()=>{this.showModal()}}><Icon type="plus" />新增任务</Button>
                    </Col>
                    <Col span="3">
                        <Button style={style.ListAddBtn} onClick={()=>{this.pauseAll()}}><Icon type="pause" />暂停所有</Button>
                    </Col>
                    <Col span="3">
                        <Button style={style.ListAddBtn} onClick={()=>{this.resumeAll()}}><Icon type="play-circle-o" />启动所有</Button>
                    </Col>
                    <Col span="15" className="right">
                        <Button type="primary" style={style.ListAddBtn} onClick={()=>{this.search()}} >搜索</Button>
                    </Col>
                </Row>
                <Form style={{paddingTop:10,borderTop:"1px solid #ccc"}}>
                    <Row>
                        <Col span="6">
                            <FormItem {...formItemLayout}  label="任务名" >
                                {getFieldDecorator('userName', {
                                    rules: [{ message: '搜索条件不能为空格！',whitespace:true}],
                                })(<Input />)}
                            </FormItem>
                        </Col>
                        <Col span="6">
                            <FormItem {...formItemLayout}  label="任务状态" >
                                {getFieldDecorator('userStatus', {
                                    rules: [{ message: '搜索条件不能为空格！',whitespace:true}],
                                    initialValue:""
                                })(<Select>
                                    <Option value="">请选择</Option>
                                    <Option value="NORMAL">NORMAL</Option>
                                    <Option value="PAUSED">PAUSED</Option>
                                    <Option value="COMPLETE">COMPLETE</Option>
                                    <Option value="ERROR">ERROR</Option>
                                    <Option value="BLOCKED">BLOCKED</Option>
                                </Select>)}
                            </FormItem>
                        </Col>
                    </Row>
                    <Modal
                        title="新增任务"
                        visible={this.state.visible}
                        onOk={this.handleOk}
                        onCancel={this.handleCancel}
                    >
                        <NewJobForm
                            postForm = {this.getNewJobForm.bind(this)}
                        />
                    </Modal>
                </Form>
            </div>
        )
    }
}
const JobListForm=Form.create()(ProductComponent);
export default JobListForm;