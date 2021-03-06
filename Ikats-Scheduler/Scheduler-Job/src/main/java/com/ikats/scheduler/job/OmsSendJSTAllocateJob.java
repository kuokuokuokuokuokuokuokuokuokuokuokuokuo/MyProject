package com.ikats.scheduler.job;

import com.ikats.scheduler.email.Email;
import com.ikats.scheduler.entity.bean.JSTAllocateBean;
import com.ikats.scheduler.entity.enumerate.GYStatus;
import com.ikats.scheduler.logic.JSTAllocateLogic;
import com.ikats.scheduler.util.OmsJSTPostUtil;
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
 * @Author: Zhao Jianzhen
 * @Date: Created in 15:40 2018/1/4
 * @Description:
 */
public class OmsSendJSTAllocateJob extends QuartzJobBean {

    @Autowired
    private JSTAllocateLogic logic;
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SystemOutMessage.start("OMS_SendAllocate",sdf.format(new Date()));
        if(null == logic)
        {
            SystemOutMessage.body("false 系统异常");
            return;
        }

        List<JSTAllocateBean> sendJob = logic.getAllocateSendJob();

        if (sendJob==null){
            SystemOutMessage.body(" -- 没有需要发送的调拨单 -- ");
            return;
        }
        ArrayList<JSTAllocateBean> updateBeans = new ArrayList<JSTAllocateBean>();
        try {
            for (int i=0;i<sendJob.size();i++){
                JSTAllocateBean bean = sendJob.get(i);
                String request = bean.getOmsRequest();
                String result = OmsJSTPostUtil.PostXml(request);
                if (result!=null){
                    JSTAllocateBean update = new JSTAllocateBean();
                    update.setId(bean.getId());
                    update.setTimes(bean.getTimes() + 1);
                    update.setSendTime(new Date());
                    if(result.contains("<ResultCode>1</ResultCode>") || result.contains("<ResultCode>2</ResultCode>"))
                    {
                        update.setState(GYStatus.SEND_OK.getValue());
                    }
                    else
                    {
                        update.setState(GYStatus.SEND_ERROR.getValue());
                    }
                    update.setReturnTime(new Date());
                    update.setOmsResponse(result);
                    updateBeans.add(update);
                }
            }
        }catch (Exception e){
            SystemOutMessage.body(" false  " + e.getMessage());
            Email.sendTextMail("liukuo@ikats.com", "聚水潭调拨单发送任务", ExceptionUtils.getStackTrace(e));
            Email.sendTextMail("zhangxiaotao@ikats.com", "聚水潭调拨单发送任务", ExceptionUtils.getStackTrace(e));
            Email.sendTextMail("wuqing@ikats.com", "聚水潭调拨单发送任务", ExceptionUtils.getStackTrace(e));
        }finally
        {
            for(JSTAllocateBean bean : updateBeans)
            {
                this.logic.update(bean);
            }
        }
        SystemOutMessage.end("OMS_SendJSTAllocate",sdf.format(new Date()));


    }
    
}
