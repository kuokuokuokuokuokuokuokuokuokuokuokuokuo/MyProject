package com.ikats.scheduler.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.ikats.scheduler.email.Email;
import com.ikats.scheduler.entity.bean.GYSkuBean;
import com.ikats.scheduler.entity.enumerate.SendStatus;
import com.ikats.scheduler.entity.exception.DmsOrderSettlementException;
import com.ikats.scheduler.logic.GYSkuLogic;
import com.ikats.scheduler.util.GuanYiPostUtil;
import com.ikats.scheduler.util.OmsPostUtil;
import com.ikats.scheduler.util.SystemOutMessage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author : liu kuo
 * @Date : 2017/10/26 15:14.
 * @Description : Indulge in study , wasting away
 */

public class OmsSendSkuRegisterJob extends QuartzJobBean
{

    @Autowired
    private GYSkuLogic logic;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SystemOutMessage.start("OMS_SendSkuRegister",sdf.format(new Date()));
        if(null == logic)
        {
            SystemOutMessage.body("false 系统异常");
            return;
        }

        List<GYSkuBean> updateBeans = new ArrayList<GYSkuBean>();
        try
        {
            //获取到 appkey 对应的客户关系
            String omsXml = "<eFreightService>\n" +
                    "<ServiceURL>Client</ServiceURL>\n" +
                    "<ServiceAction>queryClient</ServiceAction>\n" +
                    "<ServiceData>\n" +
                    "<ChannelId/>\n" +
                    "<AdminUserName>" + GuanYiPostUtil.APPKEY + "</AdminUserName>\n" +
                    "<StockId/>\n" +
                    "<ClientId/>\n" +
                    "</ServiceData>\n" +
                    "</eFreightService>\n";

            String clients = OmsPostUtil.PostXml(omsXml);

            JSONObject jsonClients = (JSONObject) JSONPath.read(clients,"$");
            if(null == jsonClients)
            {
                SystemOutMessage.body("OMS --  client 查询失败 -- 终止备案发送");
                return;
            }
            String clientSuccess = jsonClients.getString("success");
            if(null == clientSuccess || !clientSuccess.equals("true"))
            {
                SystemOutMessage.body("OMS --  client 查询失败 -- 终止备案发送");
                return;
            }
            JSONArray client = jsonClients.getJSONArray("client");
            for(int i=0;i<client.size();i++)
            {
                JSONObject jClient = (JSONObject) client.get(i);
                String clientId = jClient.getString("clientid");
                String stockid = jClient.getString("stockid");
                //循环每个客户发送一次
                List<GYSkuBean> skuBeans = this.logic.getSkuSendJob();
                if(null == skuBeans) return;
                for(GYSkuBean bean : skuBeans)
                {
                    String omsRequest = bean.getOmsRequest();
                    if(bean.getCombine().equals("0"))
                    {
                        omsRequest = omsRequest.replace("<ClientId></ClientId>","<ClientId>" + clientId + "</ClientId>");//ClientId
                        omsRequest = omsRequest.replace("<WarehouseNo></WarehouseNo>","<WarehouseNo>" + stockid + "</WarehouseNo>");
                    }
                    else
                    {
                        omsRequest = omsRequest.replace("<UserName></UserName>","<UserName>" + clientId + "</UserName>");
                        omsRequest = omsRequest.replace("<ClientID></ClientID>","<ClientID>" + clientId + "</ClientID>");
                    }
//                    System.out.println(omsRequest);
                    String result = OmsPostUtil.PostXml(omsRequest);
                    if(i == 0)
                    {
                        GYSkuBean update = new GYSkuBean();
                        update.setId(bean.getId());
                        update.setTimes(bean.getTimes() + 1);
                        update.setSendTime(new Date());
                        if(result.contains("<ResultCode>1</ResultCode>") || result.contains("<ResultCode>2</ResultCode>"))
                        {
                            update.setState(SendStatus.SEND_OK.getValue());
                        }
                        else
                        {
                            update.setState(SendStatus.SEND_ERROR.getValue());
                        }
                        update.setReturnTime(new Date());
                        update.setOmsResponse(result);
                        //暂时只保存一次的,忽略之后的
                        updateBeans.add(update);
                    }
                }
            }
        }catch (DmsOrderSettlementException e)
        {
            SystemOutMessage.body(" false  " + e.getMessage());
            Email.sendTextMail("liukuo@ikats.com", "管易商品备案发送任务", ExceptionUtils.getStackTrace(e));
            Email.sendTextMail("zhangxiaotao@ikats.com", "管易商品备案发送任务", ExceptionUtils.getStackTrace(e));
            Email.sendTextMail("wuqing@ikats.com", "管易商品备案发送任务", ExceptionUtils.getStackTrace(e));
        }finally
        {
            for(GYSkuBean bean : updateBeans)
            {
                this.logic.update(bean);
            }
        }
        SystemOutMessage.end("OMS_SendSkuRegister",sdf.format(new Date()));
    }
}
