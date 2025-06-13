package com.zywl.app.server.huifu;

import com.alibaba.fastjson2.JSONObject;
import com.huifu.bspay.sdk.opps.core.request.V2TradePaymentScanpayQueryRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易查询接口 - 示例
 *
 * @author sdk-generator
 * @Description
 */
public class V2TradePaymentScanpayQueryRequestDemo extends BaseCommon {

    public static void main(String[] args) throws Exception {

        // 1. 数据初始化
        doInit(OppsMerchantConfig.getMerchantConfig());

        // 2.组装请求参数
        V2TradePaymentScanpayQueryRequest request = new V2TradePaymentScanpayQueryRequest();
        // 原机构请求日期
        request.setOrgReqDate("20221107");
        // 商户号
        request.setHuifuId("6666000168582151");
        // 商户订单号线下POS、扫码机具发起的交易需要提供；&lt;font color&#x3D;&quot;green&quot;&gt;示例值：22577563652260773965&lt;/font&gt;
        // 交易返回的全局流水号org_hf_seq_id，org_req_seq_id，out_trans_id，party_order_id四选一；&lt;br/&gt;&lt;font color&#x3D;&quot;green&quot;&gt;示例值：00290TOP1GR210919004230P853ac13262200000&lt;/font&gt;
        // request.setOrgHfSeqId("test");
        // 原机构请求流水号org_hf_seq_id，org_req_seq_id，out_trans_id，party_order_id四选一；&lt;br/&gt;&lt;font color&#x3D;&quot;green&quot;&gt;示例值：202110210012100005&lt;/font&gt;
        request.setOrgReqSeqId("");
        // 用户账单上的交易订单号org_hf_seq_id，org_req_seq_id，out_trans_id，party_order_id四选一；&lt;br/&gt;&lt;font color&#x3D;&quot;green&quot;&gt;示例值：092021091922001451301445517582&lt;/font&gt;；参见[用户账单说明](https://paas.huifu.com/partners/api/#/czsm/api_czsm_yhzd)
        // 用户账单上的商户订单号org_hf_seq_id，org_req_seq_id，out_trans_id，party_order_id四选一；&lt;br/&gt;&lt;font color&#x3D;&quot;green&quot;&gt;示例值：03232109190255105603561&lt;/font&gt;；参见[用户账单说明](https://paas.huifu.com/partners/api/#/czsm/api_czsm_yhzd)

        // 设置非必填字段
        Map<String, Object> extendInfoMap = getExtendInfos();
        request.setExtendInfo(extendInfoMap);
        // 3. 发起API调用
        Map<String, Object> response = doExecute(request);
        System.out.println("返回数据:" + JSONObject.toJSONString(response));
    }

    /**
     * 非必填字段
     * @return
     */
    private static Map<String, Object> getExtendInfos() {
        // 设置非必填字段
        Map<String, Object> extendInfoMap = new HashMap<>();
        return extendInfoMap;
    }

}
