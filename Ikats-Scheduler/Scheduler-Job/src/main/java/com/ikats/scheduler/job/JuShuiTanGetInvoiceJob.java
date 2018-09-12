package com.ikats.scheduler.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.ikats.scheduler.email.Email;
import com.ikats.scheduler.entity.bean.JSTOrderBean;
import com.ikats.scheduler.entity.enumerate.SendStatus;
import com.ikats.scheduler.logic.JSTOrderLogic;
import com.ikats.scheduler.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定时任务获取 - 聚水潭 - 的发货单
 *
 * @Author : liu kuo
 * @Date : 2017/10/26 15:14.
 * @Description : Indulge in study , wasting away
 */

public class JuShuiTanGetInvoiceJob extends QuartzJobBean {

    @Autowired
    private JSTOrderLogic orderLogic;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SystemOutMessage.start("聚水潭发货单查询任务", sdf.format(new Date()));
        if (null == orderLogic) {
            SystemOutMessage.body("false 系统异常");
            return;
        }
        try {
            //获取到 appkey 对应的客户关系
            String clientXml = "<eFreightService>\n" +
                    "<ServiceURL>Client</ServiceURL>\n" +
                    "<ServiceAction>queryClient</ServiceAction>\n" +
                    "<ServiceData>\n" +
                    "<ChannelId/>\n" +
                    "<AdminUserName>" + JuShuiTanPostUtil.PARTNER_ID + "</AdminUserName>\n" +
                    "<StockId/>\n" +
                    "<ClientId/>\n" +
                    "</ServiceData>\n" +
                    "</eFreightService>\n";

            String clients = OmsJSTPostUtil.PostXml(clientXml);

            JSONObject jsonClients = (JSONObject) JSONPath.read(clients, "$");
            if (null == jsonClients)
            {
                SystemOutMessage.body("OMS --  client 查询失败 -- 终止取消订单查询");
                return;
            }
            String clientSuccess = jsonClients.getString("success");
            if (null == clientSuccess || !clientSuccess.equals("true")) {
                SystemOutMessage.body("OMS --  client 查询失败 -- 终止订单发送");
                return;
            }
            JSONArray client = jsonClients.getJSONArray("client");
            if(null == client || client.size() == 0)
            {
                SystemOutMessage.body("OMS --  无客户数据 -- 终止订单发送");
                return;
            }
            //根据 客户 & 仓库 去重
            Map<String,Boolean> clientMap = new HashMap<String,Boolean>();
            for (int i = 0; i < client.size(); i++)
            {
                JSONObject jClient = (JSONObject) client.get(i);
                //客户
                String clientId = jClient.getString("clientid");
                //仓库
                String stockId = jClient.getString("stockid");
                //渠道
                String channelId = jClient.getString("channelid");

                String clientMapKey = clientId + "_" + stockId;
                if(clientMap.containsKey(clientMapKey))
                {
                    continue;
                }else
                {
                    clientMap.put(clientMapKey,true);
                }

                String sPartnerId = JuShuiTanPostUtil.PARTNER_ID;
                String sPartnerKey = JuShuiTanPostUtil.PARTNER_KEY;
                String sToken = JuShuiTanPostUtil.TOKEN;
                String sHostUrl = JuShuiTanPostUtil.QM_URL;

                String sMethod = "jst.orders.out.query";
                String sTaobaoAPPKEYString = JuShuiTanPostUtil.TAO_KEY;
                String sTaobaoAPPSECRET = JuShuiTanPostUtil.TAO_SECRET;
                List<JSTOrderBean> orderBeans = new ArrayList<JSTOrderBean>();
                for(int index=1;index<100;index++)
                {
                    JSTQuery api = new JSTQuery(sTaobaoAPPKEYString, sTaobaoAPPSECRET, sPartnerId, sPartnerKey, sToken, sMethod, sHostUrl);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date day = JSTUtility.getDay(new Date(), 1);
                    String nowDate = format.format(day);
                    String pastDate = JSTUtility.getPastDate2(1);
                    api.AddArg("modified_begin", pastDate);
                    api.AddArg("modified_end", nowDate);
                    api.AddArg("page_index",index + "" );
                    api.AddArg("page_size", "30");

                    String sData = api.QueryData();
                    JSONObject jsonJST = (JSONObject) JSONPath.read(sData, "$");
                    JSONObject response = jsonJST.getJSONObject("response");

                    JSONArray orders = response.getJSONArray("datas");
                    if(null == orders || orders.size() == 0)
                    {
                        break;
                    }

                    for(int j=0; j<orders.size(); j++)
                    {
                        JSONObject order = orders.getJSONObject(j);
                        String status = order.getString("status");

                        if (!status.equals("WaitConfirm")){
                            continue;
                        }
                        JSTOrderBean orderBean = new JSTOrderBean();
                        orderBean.setAppKey(JuShuiTanPostUtil.PARTNER_ID);
                        orderBean.setOrderNo(order.getString("io_id"));
                        orderBean.setState(SendStatus.INIT.getValue());
                        orderBean.setTimes(0);
                        orderBean.setCreateTime(new Date());

                        JSONArray items = order.getJSONArray("items");
                        int totalQty = 0;
                        StringBuilder skuItems = new StringBuilder();
                        for (int x = 0; x < items.size(); x++)
                        {
                            JSONObject sku = (JSONObject) items.get(x);
                            totalQty += JobDataTransferUtil.stringToInt(sku.getString("qty"));
                            //得到如果skuName缺失,需要根据skuId去查询
                            String skuName = sku.getString("name");
                            String skuCode = sku.getString("sku_id");
                            if(StringUtils.isBlank(skuName))
                            {
                                String method = "sku.query";
                                Long ts = System.currentTimeMillis()/1000;
                                JSONObject object = new JSONObject();
                                object.put("page_index",1);
                                object.put("page_size",30);
                                object.put("sku_ids",skuCode);
                                String url = JuShuiTanPostUtil.URL + "?token=" + JuShuiTanPostUtil.TOKEN + "&ts=" + ts + "&partnerid=" + JuShuiTanPostUtil.PARTNER_ID + "&method=" + method;
                                String objectValue = "token" + JuShuiTanPostUtil.TOKEN + "ts" + ts;
                                String sign = MD5Utils.md5(method + JuShuiTanPostUtil.PARTNER_ID + objectValue + JuShuiTanPostUtil.PARTNER_KEY);
                                url = url + "&sign=" + sign;
                                String msg = object.toJSONString();

                                String result = JuShuiTanPostUtil.sendPost(url,msg);
                                JSONObject jsStr = JSONObject.parseObject(result);
                                JSONArray datas = jsStr.getJSONArray("datas");
                                if(null == datas || datas.size() == 0)
                                {
                                    System.out.println("--------------- 商品skuCode :" + skuCode + "未查到相应的备案!--------------");
                                }else
                                {
                                    JSONObject searchSku = datas.getJSONObject(0);
                                    skuName = searchSku.getString("name");
                                }
                            }

                            skuItems.append("        <OrderDetailItem>\n" +
                                    "          <SKUCode>" + sku.get("sku_id") + "</SKUCode>\n" +
                                    "          <SKUName>" + skuName + "</SKUName>\n" +
                                    "          <SKUEnglishName/>\n" +
                                    "          <SKUModel>" + sku.get("properties_value") + "</SKUModel>\n" +
                                    "          <SKUPrice>" + sku.get("sale_price") + "</SKUPrice>\n" +
                                    "          <SKUCount>" + JobDataTransferUtil.stringToInt(sku.getString("qty")) + "</SKUCount>\n" +
                                    "          <LabelCode></LabelCode>\n" +
                                    "          <MailTaxNo></MailTaxNo>\n" +
                                    "          <HsCode></HsCode>\n" +
                                    "          <GoodsItemNo></GoodsItemNo>\n" +
                                    "          <ProductionMarketingCountry></ProductionMarketingCountry>\n" +
                                    "          <GoodsNetWeight>0</GoodsNetWeight>\n" +
                                    "          <GoodsRoughWeight>0</GoodsRoughWeight>\n" +
                                    "          <BargainCurrency>142</BargainCurrency>\n" +
                                    "          <DeclareCount>" + JobDataTransferUtil.stringToInt(sku.getString("qty")) + "</DeclareCount>\n" +
                                    "          <DeclareMeasureUnit></DeclareMeasureUnit>\n" +
                                    "          <FirstUnit/>\n" +
                                    "          <FirstCount/>\n" +
                                    "          <SecondUnit/>\n" +
                                    "          <SecondCount/>\n" +
                                    "        </OrderDetailItem>\n");
                        }

                        StringBuilder omsXml = new StringBuilder();
                        omsXml.append("<Service>\n" +
                                "  <ServiceURL>OMSOrder</ServiceURL>\n" +
                                "  <ServiceAction>DMOrder</ServiceAction>\n" +
                                "  <UserName>" + clientId + "</UserName>\n" +
                                "  <ChannelName>聚水潭</ChannelName>\n" +
                                "  <ServiceData>\n" +
                                "    <Order>\n" +
                                "      <SupplyVendor>" + clientId + "</SupplyVendor>\n" +
                                "      <SupplyChannel>" + clientId + "</SupplyChannel>\n" +
                                "      <ClearPort>境内</ClearPort>\n" +
                                "      <Port></Port>\n" +
                                "      <eStoreNo>" + clientId + "</eStoreNo>\n" +
                                "      <OrderNo>" + order.getString("io_id") + "</OrderNo>\n" +
                                "      <PaymentNo/>\n" +
                                "      <PayChannel></PayChannel>\n" +
                                "      <ParcelCode></ParcelCode>\n" +
                                "      <Mawbcode></Mawbcode>\n" +
                                "      <FreightCode/>\n" +
                                "      <OrderTotalAmount>" + order.get("pay_amount") + "</OrderTotalAmount>\n" +
                                "      <OrderGoodsAmount>" + order.get("pay_amount") + "</OrderGoodsAmount>\n" +
                                "      <OrderTotalCount>" + totalQty + "</OrderTotalCount>\n" +
                                "      <FreightCharge>0</FreightCharge>\n" +
                                "      <TarrifCharge>0</TarrifCharge>\n" +
                                "      <ValuationFee>0</ValuationFee>\n" +
                                "      <Discount>0</Discount>\n" +
                                "      <OrderTime>" + order.getString("modified") + "</OrderTime>\n" +
                                "      <Status>ORDER</Status>\n" +
                                "      <RoughWeight>0</RoughWeight>\n" +
                                "      <NetWeight>0</NetWeight>\n" +
                                "      <StockId>" + stockId + "</StockId>\n" +
                                "      <StockName></StockName>\n" +
                                "      <IdentityImageFront></IdentityImageFront>\n" +
                                "      <IdentityImageBack></IdentityImageBack>\n" +
                                "      <PurchaseChannel/>\n" +
                                "      <PurchaseLogistics/>\n" +
                                "      <PurchaseLogisticsCode></PurchaseLogisticsCode>\n" +
                                "      <TransferFirstName/>\n" +
                                "      <TransferLastName/>\n" +
                                "      <ChannelId>" + order.get("shop_id") + "</ChannelId>\n" +
                                "      <ShopName>波曼琪旗舰店</ShopName>\n" +
                                "      <ChannelName>聚水潭</ChannelName>\n" +
                                "      <Forwarder>"+ "CN" + order.get("lc_id") +"</Forwarder>\n" +
                                "      <SKUDesc></SKUDesc>\n" +
                                "\t  <Note></Note>\n" +
                                "\t  <Remark></Remark>\n" +
                                "      <NeutralPackage/>\n" +
                                "      <Buyer>\n" +
                                "        <BuyerID/>\n" +
                                "        <BuyerName></BuyerName>\n" +
                                "        <BuyerEmail/>\n" +
                                "        <BuyerPhone/>\n" +
                                "        <BuyerAddress/>\n" +
                                "        <BuyerIdentityCardType>01</BuyerIdentityCardType>\n" +
                                "        <BuyerIdentityCardNo></BuyerIdentityCardNo>\n" +
                                "      </Buyer>\n" +
                                "      <Consignee>\n" +
                                "        <ConsigneeName>" + order.get("receiver_name") + "</ConsigneeName>\n" +
                                "        <ConsigneeEmail/>\n" +
                                "        <ConsigneePhone>" + order.get("receiver_mobile") + "</ConsigneePhone>\n" +
                                "        <ConsigneePost/>\n" +
                                "        <ConsigneeAddress>" + order.get("receiver_address") + "</ConsigneeAddress>\n" +
                                "        <ConsigneeIdentityCardType>01</ConsigneeIdentityCardType>\n" +
                                "        <ConsigneeIdentityCardNo></ConsigneeIdentityCardNo>\n" +
                                "        <RecipientProvincesCode></RecipientProvincesCode>\n" +
                                "        <tcustProvince>" + order.get("receiver_state") + "</tcustProvince>\n" +
                                "        <tcustCity>" + order.get("receiver_city") + "</tcustCity>\n" +
                                "        <tcustCounty>" + order.get("receiver_district") + "</tcustCounty>\n" +
                                "      </Consignee>\n" +
                                "      <OrderDetailList>\n");

                        omsXml.append(skuItems.toString());
                        omsXml.append("      </OrderDetailList>\n" +
                                "    </Order>\n" +
                                "  </ServiceData>\n" +
                                "</Service>\n");
                        orderBean.setOmsRequest(omsXml.toString());
                        orderBeans.add(orderBean);
                    }
                    if(orders.size() <30) break;
                }

                this.orderLogic.insertList(orderBeans);
                if (!orderLogic.getSuccess())
                {
                    SystemOutMessage.body(this.orderLogic.getSuccess() + this.orderLogic.getMessage());
                    return;
                }
            }
        } catch (Exception e)
        {
            System.out.println("系统异常!" + e.getMessage());
            Email.sendTextMail("liukuo@ikats.com", "聚水潭发货单抓取任务", ExceptionUtils.getStackTrace(e));
            Email.sendTextMail("zhangxiaotao@ikats.com", "聚水潭发货单抓取任务", ExceptionUtils.getStackTrace(e));
            Email.sendTextMail("wuqing@ikats.com", "聚水潭发货单抓取任务", ExceptionUtils.getStackTrace(e));
            return;
        }
        SystemOutMessage.end("聚水潭发货单查询任务", sdf.format(new Date()));
    }
}
