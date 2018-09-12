import React, {Component} from 'react';
import { observer } from "mobx-react";
import {Breadcrumb,Icon,Modal,Pagination,Table,Tooltip,Row,Col,Input,Radio} from 'antd';
const { TextArea} = Input;
const RadioGroup = Radio.Group;
import $ from '../Common/jquery-3.2.1.min';

@observer
export default class EntranceContainer extends Component
{
    constructor(props){
        super(props);
        this.state={
            cron :'',
            radioValue:3
        };
    }
    radioChange = (e) => {
        this.setState({
            radioValue: e.target.value,
        });
    }
    clearRequestMsg(){
        $("#requestMsg").val('');
        $("#responseMsg").val('');
    };
    sendRequestMsg()
    {
        var requestMsg = $("#requestMsg").val();
        // this.props.store.sendToGuanYi(JSON.stringify(requestMsg));
        // $("#responseMsg").val(this.props.store.gyResult);
        console.info(requestMsg);
        $.ajax({
            type: "POST",
            // url: "http://oms.adepotech.com:443/Scheduler-Web/GYEntrance/Debug.action",
            url: "http://localhost:8050/GYEntrance/Debug.action",
            data: {'jString':requestMsg,'way':this.state.radioValue},
            contentType:"application/x-www-form-urlencoded;charset=UTF-8",
            dataType: "text",
            // contentType: "application/json;charset=UTF-8",
            success: function(res)
            {
                $("#responseMsg").val(res);
            },
        });
    };
    render(){
        return(
            <div>

                <Row style={{height:400,margin: '25px'}}>
                    <Col span={10}><TextArea id="requestMsg" placeholder="发送报文..." rows={20} style={{ fontSize: 15}}/></Col>
                    <Col span={4}>
                        <div style={{marginLeft: '90px',marginTop:20}}>
                            <Tooltip placement="top" title="测试" arrowPointAtCenter>
                                <a onClick={this.sendRequestMsg.bind(this)}><Icon type="play-circle-o" style={{ fontSize: 30, color: '#08c' }}/></a>
                            </Tooltip>
                        </div>
                        <div style={{marginLeft: '40px',marginTop:150}}>
                            <RadioGroup onChange={this.radioChange} defaultValue={3}>
                                <Radio value={3}>GY Message To OMS</Radio>
                                <Radio value={1}>Message To GY</Radio>
                                <Radio value={2}>Message To OMS</Radio>
                            </RadioGroup>
                        </div>
                        <div style={{marginLeft: '90px',marginTop:150}}>
                            <Tooltip placement="top" title="清空" arrowPointAtCenter>
                                <a onClick={this.clearRequestMsg.bind(this)}><Icon type="delete" style={{ fontSize: 30, color: '#08c' }}/></a>
                            </Tooltip>
                        </div>
                    </Col>
                    <Col span={10}><TextArea id="responseMsg" rows={20} style={{ fontSize: 15}}/></Col>
                </Row>
            </div>
        )
    }
}