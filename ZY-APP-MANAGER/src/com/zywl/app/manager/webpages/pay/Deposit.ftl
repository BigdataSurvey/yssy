<#compress>
<!doctype html>
<html lang="en">
<head>
    <title>在线充值</title>
    <!-- Meta -->
    <meta charset="utf-8">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
    <meta name="format-detection" content="telephone=no, email=no"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <!-- Favicon icon -->
    <link rel="icon" href="${staticfile}/files/assets/images/favicon.ico" type="image/x-icon">
    <link href="https://cdn.bootcss.com/jquery-modal/0.9.2/jquery.modal.min.css" rel="stylesheet">
    <style>
        <#include "pay.css">
    </style>
</head>

<body ondragstart="return false;" class="no-select">
    <div class="loader-block" id="loader-block">
        <svg id="loader2" viewbox="0 0 100 100"> 
            <circle id="circle-loader2" cx="50" cy="50" r="45"></circle> 
        </svg>
    </div>
    <div class="amount-top">
        <div class="amount-title">金币</div>
        <div class="amount-num" id="amount-num"><span>0</span></div>
    </div>
    <div class="title">购买金币</div>
    <div class="amount-list clearfix">
        <!--选择金额-->
        <div class="clearfix row" id="payAmount">
        </div>
        <!--输入框金额-->
        <div class="form-group d-none" id="payInput">
            <input class="form-control" type="text" id="inputAmount" maxlength="9" placeholder="请输入整数金额">
        </div>
    </div>
    <div class="amount-rate" id="rateAmount"></div>
    <div class="title">付款方式</div>
    <div class="channel-list" id="payChannel">
    </div>
    <div class="btn-div">
        <button class="btn btn-pay" id="btnGoPay">充值</button>
    </div>
    <div class="tips" id="payTips">
        <p>安全保障，极速到账</p>
    </div>
    <!-- 确认支付-->
    <div class="modal" id="payModal">
        <h5 class="modal-title">确定支付</h5>
        <div class="modal-body"><p id="payBody"></p></div>
        <div class="modal-footer no-bd">
             <a href="javascript:;" class="btn2 btn-default" rel="modal:close"> 重新选择</a>
             <button class="btn2 btn-primary" type="button" id="btnPay"> 确定支付</button>
         </div>
    </div>
    <div class="bottom-div"></div>
    <script src="https://cdn.bootcss.com/jquery/3.2.1/jquery.min.js"></script>
    <script src="https://cdn.bootcss.com/jquery-modal/0.9.2/jquery.modal.min.js"></script>
    <script src="https://cdn.bootcss.com/device.js/0.2.7/device.min.js"></script>
    <script>
        <#include "Deposit.js">
    </script>
</body>
</html>
</#compress>