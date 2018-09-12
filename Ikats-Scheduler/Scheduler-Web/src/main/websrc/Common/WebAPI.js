import axios from 'axios';
import {notification} from 'antd';

export default function api(url,data)
{
    let token = localStorage.ikatsToken?localStorage.ikatsToken:'';
    return axios.post(url, data,{headers:{"ikats-ChiGoose-token" : token},contentType:"application/json;charset=utf-8"}).then(function(response)
    {
        if(response.data.errorStatus == 405)
        {
            //跳转到
            localStorage.clear();
            location.hash="Login";
        }
        return response.data;
    }).catch(function(error)
    {
        notification.open({
            message: '网络失联',
            description: error
        });
    });
}