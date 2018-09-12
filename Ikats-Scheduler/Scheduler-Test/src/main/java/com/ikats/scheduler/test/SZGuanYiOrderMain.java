package com.ikats.scheduler.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.ikats.scheduler.util.*;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.util.Calendar;

/**
 * 四洲对接管易的测试main函数
 * @Author : liu kuo
 * @Date : 2018/5/3 16:07.
 * @Description : Indulge in study , wasting away
 */
public class SZGuanYiOrderMain
{
    private static String APP_KEY = "156484";
    private static String SECRET = "557c67476fc34c6bbac3d32d9340bfad";
    private static String SESSION_KEY = "6b4bb42b124a4cb780a4564e7ea7e20b";

    private static String FACE_NAME = "gy.erp.trade.deliverys.get";

    public static void main(String[] args) throws IOException
    {
        //获取到 appkey 对应的客户关系
        String clientXml = "<eFreightService>\n" +
                "<ServiceURL>Client</ServiceURL>\n" +
                "<ServiceAction>queryClient</ServiceAction>\n" +
                "<ServiceData>\n" +
                "<ChannelId/>\n" +
                "<AdminUserName>" + APP_KEY + "</AdminUserName>\n" +
                "<StockId/>\n" +
                "<ClientId/>\n" +
                "</ServiceData>\n" +
                "</eFreightService>\n";

        String clients = SZOmsPostUtil.PostXml(clientXml);
        System.out.println(clients);
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

        for (int i = 0; i < client.size(); i++)
        {
            for (int page = 1; page < 10000; page++)
            {
                JSONObject jClient = (JSONObject) client.get(i);
                //客户
                String clientId = jClient.getString("clientid");
                //仓库
                String stockId = jClient.getString("stockid");
                //渠道
                String channelId = jClient.getString("channelid");
                Calendar now = Calendar.getInstance();
                JSONObject sendJson = new JSONObject();
                sendJson.put("appkey", APP_KEY);//SZGuanYiPostUtil.APPKEY
                sendJson.put("sessionkey", SESSION_KEY);//SZGuanYiPostUtil.SESSIONKEY
                sendJson.put("method", FACE_NAME);
                sendJson.put("warehouse_code", stockId);
                sendJson.put("shop_code", channelId);
                sendJson.put("code", "");
                sendJson.put("page_no", page);
                sendJson.put("page_size", "100");
                sendJson.put("delivery", "0");
                String sendString = sendJson.toJSONString();
                String sign = SZGuanYiPostUtil.sign(sendString,SECRET);//SZGuanYiPostUtil.SECRET
                sendJson.put("sign", sign);
                String sendParam = sendJson.toJSONString();
                //管易回执
                String result = SZGuanYiPostUtil.sendPost(SZGuanYiPostUtil.URL, sendParam);//SZGuanYiPostUtil.URL
                JSONObject jsonGy = (JSONObject) JSONPath.read(result, "$");
                String success = jsonGy.getString("success");

                if (success.equals("false"))
                {
                    break;
                }

                JSONArray deliverys = (JSONArray) jsonGy.get("deliverys");
                if (null == deliverys || deliverys.size() == 0)
                {
                    System.out.println("----------------------- 订单查询无最新数据 ----------------------");
                    break;
                }
                for (int j = 0; j < deliverys.size(); j++)
                {
                    //某个订单
                    JSONObject order = (JSONObject) deliverys.get(j);
                    //订单的商品 集
                    JSONArray details = (JSONArray) order.get("details");
                    String[] areas = {"","",""};
                    String areaName = order.getString("area_name");
                    if(StringUtils.isNotBlank(areaName))
                    {
                        String[] areasTemp = areaName.split("-");
                        if(areasTemp.length == 3)
                        {
                            areas = areasTemp;
                        }
                    }
                    String omsXml =
                            "<Service>\n" +
                                    "  <ServiceURL>OMSOrder</ServiceURL>\n" +
                                    "  <ServiceAction>DMOrder</ServiceAction>\n" +
                                    "  <UserName>" + clientId + "</UserName>\n" +
                                    "  <ChannelName>管易</ChannelName>\n" +
                                    "  <ServiceData>\n" +
                                    "    <Order>\n" +
                                    "      <SupplyVendor>" + clientId + "</SupplyVendor>\n" +
                                    "      <SupplyChannel>" + clientId + "</SupplyChannel>\n" +
                                    "      <ClearPort>" + (order.getString("shop_code").equals("6") ? "淘宝C店" : "境内") + "</ClearPort>\n" +
                                    "      <Port></Port>\n" +
                                    "      <eStoreNo>" + clientId + "</eStoreNo>\n" +
                                    "      <OrderNo>" + order.getString("code") + "</OrderNo>\n" +
                                    "      <SOOrderCode>" + order.getString("platform_code") + "</SOOrderCode>\n" +
                                    "      <PaymentNo/>\n" +
                                    "      <PayChannel></PayChannel>\n" +
                                    "      <ParcelCode>" + (order.get("express_no") == null ? "" : order.getString("express_no")) + "</ParcelCode>\n" +
                                    "      <Mawbcode></Mawbcode>\n" +
                                    "      <FreightCode/>\n" +
                                    "      <OrderTotalAmount>" + order.get("amount") + "</OrderTotalAmount>\n" +
                                    "      <OrderGoodsAmount>" + order.get("amount") + "</OrderGoodsAmount>\n" +
                                    "      <OrderTotalCount>" + JobDataTransferUtil.stringToInt(order.getString("qty")) + "</OrderTotalCount>\n" +
                                    "      <FreightCharge>" + order.get("post_fee") + "</FreightCharge>\n" +
                                    "      <TarrifCharge>0</TarrifCharge>\n" +
                                    "      <ValuationFee>0</ValuationFee>\n" +
                                    "      <Discount>" + order.get("discount_fee") + "</Discount>\n" +
                                    "      <OrderTime>" + (order.get("pay_time") == null ? order.getString("create_date") : order.getString("pay_time")) + "</OrderTime>\n" +
                                    "      <Status>ORDER</Status>\n" +
                                    "      <RoughWeight>0</RoughWeight>\n" +
                                    "      <NetWeight>0</NetWeight>\n" +
                                    "      <StockId>" + order.get("warehouse_code") + "</StockId>\n" +
                                    "      <StockName>" + order.getString("warehouse_name") + "</StockName>\n" +
                                    "      <IdentityImageFront></IdentityImageFront>\n" +
                                    "      <IdentityImageBack></IdentityImageBack>\n" +
                                    "      <PurchaseChannel/>\n" +
                                    "      <PurchaseLogistics/>\n" +
                                    "      <PurchaseLogisticsCode></PurchaseLogisticsCode>\n" +
                                    "      <TransferFirstName/>\n" +
                                    "      <TransferLastName/>\n" +
                                    "      <ChannelId>" + order.get("shop_code") + "</ChannelId>\n" +
                                    "      <ShopName>" + order.getString("shop_name") + "</ShopName>\n" +
                                    "      <ChannelName>管易</ChannelName>\n" +
                                    "      <Forwarder>" + order.getString("express_code") + "</Forwarder>\n" +
                                    "      <SKUDesc></SKUDesc>\n" +
                                    "\t  <Note>"+ makeNodeByForwarder(order) +"</Note>\n" +
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
                                    "        <ConsigneePhone>" + (StringUtils.isBlank(order.getString("receiver_mobile")) ? order.get("receiver_phone") : order.get("receiver_mobile")) + "</ConsigneePhone>\n" +
                                    "        <ConsigneePost/>\n" +
                                    "        <ConsigneeAddress>" + order.get("receiver_address") + "</ConsigneeAddress>\n" +
                                    "        <ConsigneeIdentityCardType>01</ConsigneeIdentityCardType>\n" +
                                    "        <ConsigneeIdentityCardNo></ConsigneeIdentityCardNo>\n" +
                                    "        <RecipientProvincesCode></RecipientProvincesCode>\n" +
                                    "        <tcustProvince>" + areas[0] + "</tcustProvince>\n" +
                                    "        <tcustCity>" + areas[1] + "</tcustCity>\n" +
                                    "        <tcustCounty>" + areas[2] + "</tcustCounty>\n" +
                                    "      </Consignee>\n" +
                                    "      <OrderDetailList>\n";
                    for (int x = 0; x < details.size(); x++)
                    {
                        JSONObject sku = (JSONObject) details.get(x);
                        omsXml += "        <OrderDetailItem>\n" +
                                "          <SKUCode>" + sku.get("item_code") + (StringUtils.isBlank(sku.getString("sku_code")) ? "" : "_" + sku.get("sku_code")) + "</SKUCode>\n" +
                                "          <SKUName>" + sku.get("item_name") + "</SKUName>\n" +
                                "          <SKUEnglishName/>\n" +
                                "          <SKUModel>" + sku.get("sku_name") + "</SKUModel>\n" +
                                "          <SKUPrice>" + sku.get("price") + "</SKUPrice>\n" +
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
                                "        </OrderDetailItem>\n";
                    }

                    omsXml += "      </OrderDetailList>\n" +
                            "    </Order>\n" +
                            "  </ServiceData>\n" +
                            "</Service>\n";

                    System.out.println(omsXml);
                    String res = SZOmsPostUtil.PostXml(omsXml);
                    System.out.println("----------------------------------------------------------------------");
                    System.out.println(res);
                }
                if (deliverys.size() < 100) break;
            }
        }

    }

    private static String makeNodeByForwarder(JSONObject delivery) {
        //大头笔
        String bigChar = delivery.getString("bigchar");
        String forwarder = delivery.getString("express_code");
        String parcelCode = delivery.getString("express_no");
        if(StringUtils.isBlank(parcelCode))
        {
            return "";
        }
        if(forwarder.equals("YTO") || forwarder.equals("BSHT") || forwarder.equals("EMS") || forwarder.equals("ZTO"))
        {
            JSONObject code = new JSONObject();
            String packageCenterCode = delivery.getString("package_center_code");
            String packageCenterName = delivery.getString("package_center_name");
            code.put("shortAddress",bigChar);
            code.put("packageCenterCode",packageCenterCode == null ? "":packageCenterCode);
            code.put("packageCenterName",packageCenterName == null ? "":packageCenterName);
            return code.toJSONString();
        }else if(forwarder.equals("SF"))
        {
            return "";
        }
        return "";
    }
}
