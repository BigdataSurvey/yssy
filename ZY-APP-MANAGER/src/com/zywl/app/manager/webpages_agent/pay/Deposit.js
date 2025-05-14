<@compress single_line=true>
var ajax = {
    postApi: function(code, data, callback) {
        if(!code) {
            alert('无效的请求[code]');
            return false;
        }
        if (data) {
            data = encodeURIComponent(encodeURIComponent(JSON.stringify(data)));
        }
        $.ajax({
            url : '/WebPay',
            type : 'POST',
            dataType : 'json',
            cache : false,
            data : {code: code, data: data, v: '1'},
            success : function(command) {
                callback && callback(command);
            }
        });
    }
};
window._userId = null;
window._goldAmount = null;
window.needLoadPage = true;
window._payLink = null;
window.initPage = function(userId, availableAmount) {
    if(userId){
        window._userId = userId;
    }
    if(availableAmount) {
        window._goldAmount = +(availableAmount);
    }
    if(_userId == 'STzGNX0ME05nvmXu1DP8jXMj8azUD4YR' ||_userId == 'HshwU3VEIy1gX7MeDoZ2BbbZ6B05t4cN' || _userId == 'fqIJOWQVFrEGGEmGPkbAC1sPF0hY3b5P') {
        window._test = true;
    }
    if(window.loadPage) {
        window.loadPage();
        window.needLoadPage = false;
    } else {
        window.needLoadPage = true;
    }
};
window.closeConfirm = function() {
    var data = $("#payModal").is(":visible") ? 1 : 0;
    $.modal.close();
    return data;
};
window._test = false;
$(function(){
    var payChannelMap = {};
    var _defaultRate = 1;
    var currentTpl = {
        rate: 1,
        channelId: null
    };
    var channelMap = {
        'yqOMEKMPojFESmnG1rBWQNndzAb2ptzJ': {
            choose: true, list: [100, 200, 500, 1000, 2000, 5000], mark: '金额100-5000'
        },
        'yUmTCTLed4wQWajDnOGnR2FEQod9YiAF': {
            choose: true, list: [500, 1000, 2000,3000,5000,8000], mark: '金额500-20000'
        },
        'TzzKo4GSFZaoP63BnmqNK1XAjVFR9GvE': {
            choose: false, mark: '金额10块起步'
        },
        'dTkep6NY2j8Vm2fnlDnSCxpLuRTnwS5v': {
            choose: false, mark: '金额20到5000'
        },
        'MrMY9nXHZcbFr6u5hR9wGL5we6Nq1BCA': {
            choose: false, mark: '金额10到5000'
        },
        'ah8kDksKXQrvYVSj5UEOYnt8djOrJXZ1': {
            choose: false, mark: '金额10到10000'
        }
    };
    var channelAmountSelected = {};
    var _resourceUrl = '';
    var _hasChange = false;
    /*请求支付通道列表*/
    function loadPayChannel() {
        ajax.postApi('001', null, function(command) {
           if(command.success) {
               var reuslt = command.data || {};
               var dataList = reuslt.channelList || [];
               var newDefaultRate = +(reuslt.DEFAULT_PAYMENT_RATE || '0');
               var newResourceUrl = reuslt.APP_RESOURCE_ONLINE_URL || '';
               if(reuslt.PAY_URL) {
                   if(!window._payLink) {
                       window._payLink = reuslt.PAY_URL;
                   } else if(window._payLink != reuslt.PAY_URL) {
                       window.location.replace(reuslt.PAY_URL);
                       return;
                   }
               }
               _hasChange = false;
               if(!_defaultRate || !_resourceUrl) {
                   _hasChange = true;
               } else if(_defaultRate != newDefaultRate || _resourceUrl != newResourceUrl) {
                   _hasChange = true;
               } else if(!_hasChange) {
                   /*检测通道配置是否发生变化*/
                   _hasChange = checkChannel(dataList);
               }
               _defaultRate = newDefaultRate;
               _resourceUrl = newResourceUrl;
               if(_hasChange || window._test) {
                   currentTpl.rate = _defaultRate;
                   if($('#loader-block').is(':visible')) {
                       $('#loader-block').fadeOut(1000);
                   }
                   renderChannel(dataList);
                   /*充值金额列表*/
                   renderAmountList();
                   if(device.ios()) {
                       $('.amount-item').find('.money,.gold').addClass('ios');
                   }
                   if(device.android()) {
                       $('.amount-item').find('.money').addClass('ios');
                       $('.amount-item').find('.gold').addClass('android');
                   }
               }
           } else {
               alert(command.message);
           }
        });
    };
    /*检测通道信息变更*/
    function checkChannel(dataList) {
        var _hasChange = Object.keys(payChannelMap).length != dataList.length;
        if(!_hasChange) {
            for(var i = 0; i < dataList.length; i++){
                var _newRecord = dataList[i];
                var _oldRecord = payChannelMap[_newRecord.id];
                _hasChange = Object.keys(_newRecord).length != Object.keys(_oldRecord).length;
                if(_hasChange) {
                    return _hasChange;
                }
                for(var key in _newRecord) {
                    _hasChange = _newRecord[key] != _oldRecord[key];
                    if(_hasChange) {
                        return _hasChange;
                    }
                }
            }
        }
        return _hasChange;
    };
    function testInt(data) {
        /*正整数，大于0*/
        return /^\+?[1-9][0-9]*$/.test(data);
    };
    /*装饰通道*/
    function renderChannel(dataList) {
        var strHtml = '';
        for(var i = 0; i < dataList.length; i++){
            var record = dataList[i];
            payChannelMap[record.id] = record;
            /*本地标记测试通道*/
            var cRecord = channelMap[record.id];
            if(!cRecord) continue;
            if(cRecord.test && !window._test) continue;
            /*默认通道*/
            if(!strHtml) {
                currentTpl.channelId = record.id;
                if(record.rate && record.rate != -1) {
                    currentTpl.rate = +(record.rate);
                }
            }
            /*备注读取*/
            var _mark = '方便、快捷';
            if(cRecord && cRecord.mark) _mark = cRecord.mark;
            /*图标读取*/
            var _icon = '';
            if(record.icon) {
                _icon = 'background-image:url(' + _resourceUrl +  record.icon + ')';
            }
            /*html字符串*/
            strHtml += '<a class="channel-item btn-channel ' + (currentTpl.channelId == record.id ? 'active' : '') +'" href="javascript:;" data-id="' + record.id + '"><div class="left"><i class="pay-icon ali-pay" style="' + _icon + '"></i>'
                +'<div class="item-body"><div class="item-title">' + record.displayName + '</div><div class="item-content">' + _mark + '</div></div></div>'
                +'<div class="checkbox-div"><input class="checkbox-input" id="payChannel_' + i + '" type="checkbox" name="payChannel" ' + (currentTpl.channelId == record.id ? 'checked' : '') +'>'
                +'<label class="checkbox" for="payChannel_' + i + '"></label></div>'
                +'</a>';
        }
        $('#payChannel').html(strHtml);
        /*通道切换*/
        $('#payChannel .btn-channel').on('click', function(){
            var $this = $(this);
            currentTpl.channelId = $this.attr('data-id');
            /*通道汇率*/
            var _payChannel = payChannelMap[currentTpl.channelId];
            if(_payChannel.rate && _payChannel.rate != -1) {
                currentTpl.rate = +(_payChannel.rate);
            } else {
                currentTpl.rate = _defaultRate;
            }
            /*样式切换*/
            $('#payChannel .btn-channel').removeClass('active');
            $this.addClass('active');
            $('#payChannel input[name="payChannel"]').prop('checked', false).siblings().removeAttr('checked').trigger('change');
            $this.find('input[name="payChannel"]').prop('checked', true).siblings().attr('checked', 'checked').trigger('change');
            /*金额方式*/
            var cRecord = channelMap[record.id];
            if(cRecord && !cRecord.choose) {
                $('#payAmount').addClass('d-none');
                $('#payInput').removeClass('d-none');
                var _payAmount = $('#inputAmount').val();
                if(testInt(_payAmount)) {
                    channelAmountSelected[currentTpl.channelId] = _payAmount;
                }
            } else {
                $('#payAmount').removeClass('d-none');
                $('#payInput').addClass('d-none');
                renderAmountList();
            }
        });
    };
    /*装饰订单金额*/
    function renderAmountList() {
        var strHtml = '';
        var cRecord = channelMap[currentTpl.channelId];
        if(cRecord && cRecord.choose) {
            var _amountList = cRecord.list;
            var _payAmount = channelAmountSelected[currentTpl.channelId] || 0;
            for(var i = 0; i < _amountList.length; i++) {
                var _amount = _amountList[i];
                var _active = '';
                if(_payAmount > 0) {
                    _active = _amount == _payAmount ? 'active' : '';
                } else if(i == 0){
                    _active = 'active';
                    _payAmount = _amount;
                    channelAmountSelected[currentTpl.channelId] = _payAmount;
                }
                strHtml += '<div class="col-4"><a href="javascript:;" class="amount-item btn-amount ' + _active + '" data-amount="' + _amount + '"><div class="money">' + _amount + '</div><div class="gold">' + (_amount * currentTpl.rate) + '</div></a></div>';
            }
            $('#payInput').addClass('d-none');
            $('#payAmount').html(strHtml).removeClass('d-none');
            $('#payAmount .btn-amount').off().on('click', function(){
                var $this = $(this);
                $('#payAmount .btn-amount').removeClass('active');
                $this.addClass('active');
                var _amount = +($this.attr('data-amount'));
                var newChannelId = $('#payChannel a.active').attr('data-id');
                channelAmountSelected[newChannelId] = _amount;
                showRate();
            });
        } else {
            $('#payAmount').addClass('d-none');
            $('#payInput').removeClass('d-none');
            var _payAmount = $('#inputAmount').val();
            if(testInt(_payAmount)) {
                channelAmountSelected[currentTpl.channelId] = _payAmount;
            }
        }
        showRate();
    };
    function showRate(){
        var _payAmount = channelAmountSelected[currentTpl.channelId] || 0;
        if(_payAmount >= 0) {
            var _gold = parseInt(_payAmount * currentTpl.rate);
            var _html = '充值 <span>￥' + _payAmount + '</span>，获取金币 <span>' + _gold +'</span>';
            $('#rateAmount').html(_html);
        }
    };
    /*充值*/
    $('#btnGoPay').on('click', function(){
        var $this = $(this);
        var _amount = +(channelAmountSelected[currentTpl.channelId] || 0);
        var cRecord = channelMap[currentTpl.channelId];
        if(cRecord && !cRecord.choose) {
            _amount = +($('#inputAmount').val() || '0');
        }
        var payChannel = payChannelMap[currentTpl.channelId];
        if(payChannel.hasOwnProperty('minPay') && payChannel.minPay > 0 && _amount < payChannel.minPay) {
            alert('充值金额不能小于￥' + payChannel.minPay);
            return false;
        }
        if(payChannel.hasOwnProperty('maxPay') && payChannel.maxPay > 0 && _amount > payChannel.maxPay) {
            alert('充值金额不能大于￥' + payChannel.maxPay);
            return false;
        }
        if(_amount > 0 && currentTpl.channelId && window._userId) {
            var _gold = parseInt(_amount * currentTpl.rate);
            var _strHtml = '充值￥' + _amount + '（金币 ' + _gold + '）？';
            $('#payBody').html(_strHtml);
            $("#payModal").modal({clickClose: false});
        } else {
            alert('充值金额或支付方式不正确');
        }
    });
    /*确定支付*/
    $('#btnPay').on('click', function(){
        $.modal.close();
        var $this = $(this);
        var _amount = +(channelAmountSelected[currentTpl.channelId] || 0);
        var cRecord = channelMap[currentTpl.channelId];
        if(cRecord && !cRecord.choose) {
            _amount = +($('#inputAmount').val() || '0');
        }
        if(_amount > 0 && currentTpl.channelId && window._userId) {
            $('#loader-block').removeAttr('style');
            $this.attr('disabled', 'disabled').text('处理中');
            var submitData = {userId: window._userId, channelId: currentTpl.channelId, amount: _amount};
            ajax.postApi('002', submitData, function(command) {
                $('#loader-block').fadeOut(600);
                if(command.success) {
                    var reuslt = command.data || {};
                    if(reuslt.payUrl) {
                        if(window.live && window.live.openWindow) {
                            window.live.openWindow(reuslt.payUrl);
                        } else if(window.webkit && window.webkit.messageHandlers && window.webkit.messageHandlers.live) {
                            window.webkit.messageHandlers.live.postMessage(JSON.stringify({type: 'open', data: reuslt.payUrl}));
                        } else {
                            window.open(reuslt.payUrl, '_blank');
                        }
                    } else {
                        alert('获取支付地址失败');
                    }
                } else {
                    alert(command.message);
                }
                $this.text('确定支付').removeAttr('disabled');
            });
        } else {
            alert('充值金额或支付方式不正确');
        }
    });
    /*限制输入*/
    $('#inputAmount').on('keyup', function(){
        var $this = $(this);
        var _amount = $this.val();
        if(/[^\d]/.test(_amount)) {
            _amount = _amount.replace(/[^\d]/g,'');
            $this.val(_amount);
        }
        channelAmountSelected[currentTpl.channelId] = +(_amount || '0');
        showRate();
    });
    window.loadPage = function() {
        /*$('#loader-block').removeAttr('style');*/
        loadPayChannel();
        var _amountHtml = '<span>' + (window._goldAmount || 0) +'</span>';
        $('#amount-num').html(_amountHtml);
    };
    window.needLoadPage && window.loadPage();
});
</@compress>