package com.zywl.app.server.huifu.adapay;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.huifu.adapay.model.AdapayTools;
import com.huifu.adapay.model.Bill;

/**
 * @author yingyong.wang
 */
public class BillDemo extends BaseDemo{

    /**
     * 运行账单下载接口
     * @throws Exception 异常
     */
    public static void executeBillTest(String merchantKey) throws Exception{
        BillDemo demo = new BillDemo();
        
        Map<String, Object> download = demo.executeBillDownLoad(merchantKey);
       
    }

    /**
     * 运行账单下载接口
     * @throws Exception 异常
     */
    public static void executeBillTest() throws Exception{
        BillDemo demo = new BillDemo();
        
        Map<String, Object> download = demo.executeBillDownLoad();
       
    }

    /**
     * 执行一个下载对账文件操作
     * @return 下载链接
     * @throws Exception 异常
     */
    public Map<String, Object> executeBillDownLoad(String merchantKey) throws Exception {
        System.out.println("=======execute download begin=======");

        Map<String, Object> downloadParam = new  HashMap<String, Object>(2);
        downloadParam.put("bill_date", "20190912");
        Map<String, Object> download = Bill.download(downloadParam, merchantKey);
        
        String errorCode = (String)download.get("error_code");
        
        if(null != errorCode){

            System.out.println("对账单下载，请求参数：" + JSON.toJSONString(downloadParam));
            System.out.println("对账单下载，返回参数：" + JSON.toJSONString(download));

        }else{
            System.out.println("对账单下载，成功");
        }
        

        return download;
    }

    /**
     * 执行一个下载对账文件操作
     * @return 下载链接
     * @throws Exception 异常
     */
    public Map<String, Object> executeBillDownLoad() throws Exception {
        

        Map<String, Object> downloadParam = new  HashMap<String, Object>(2);
        downloadParam.put("bill_date", "20190912");
        
        Map<String, Object> download = AdapayTools.downloadBill(downloadParam);
        
        String errorCode = (String)download.get("error_code");
        
        if(null != errorCode){

            System.out.println("对账单下载，请求参数：" + JSON.toJSONString(downloadParam));
            System.out.println("对账单下载，返回参数：" + JSON.toJSONString(download));

        }else{
            System.out.println("对账单下载，成功");
        }
        return download;
    }
    
}
