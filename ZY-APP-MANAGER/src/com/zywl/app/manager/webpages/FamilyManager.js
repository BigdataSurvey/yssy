<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var statusDic = {'0': '禁用', '1': '启用'};
        var statusColorDic = {'0': 'text-danger', '1': 'text-success'};
        var payeeTypeDic = {'1': '微信','2': '支付宝', '3': '银行卡'};
        var payeeTypeClassDic = {'1': 'bg-wxpay', '2': 'bg-alipay', '3': 'bg-bankpay'};
        var switchStatusMap = {};
        var baseTable = null;
        var hasClipboard = false;
        var clipboard = null;
        var formSwitchStatus = null;
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                scrollY: '50vh',
                scrollX: true,
                scrollCollapse: true,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "name", title: '家族名称', render: datatableUtil.renderNormal},
                    {data: "familyGiftFee", title: '礼物分成比例', render: datatableUtil.renderNormal},
                    {data: "familyTicketFee", title: '门票分成比例', render: datatableUtil.renderNormal},
                    {data: "familyChargeFee", title: '金币分成比例', render: datatableUtil.renderNormal},
                    {data: "familyGuardFee", title: '守护分成比例', render: datatableUtil.renderNormal},
                    {data: "payeeType", title: '收款类型', render: function(data, type, row, setting){
                        var _html = '';
                        if(payeeTypeDic[data]) {
                            _html = '<span class="' + (payeeTypeClassDic[data] || '')+'">' + payeeTypeDic[data] +'</span>';
                        }
                        return _html || '-';
                    }},
                    {data: "payeeName", title: '提现收款人姓名', render: datatableUtil.renderNormal},
                    {data: "payeeAccount", title: '提现账户', render: datatableUtil.renderNormal},
                    {data: "status", title: '启用', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-status" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "createTime", title: '创建时间', render: datatableUtil.renderTime},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>';
                        return _opearHtml;
                    }},
                    ],
                    drawCallback: function(){
                        loadOpearte();
                    },
                    ajax: function (data, callback, settings) {
                        loadData(data, callback, settings);
                    }
            });
        };
        /*请求数据*/
        function loadData(data, callback, settings) {
            if(me.loading) return;
            me.loading = true;
            var searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength,
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                if(_name == 'status') {
                    if(_value && _value.length > 0) {
                        _value = _value.join(',');
                    } else {
                        _value = null;
                    }
                }
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('034001', searchData, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.list || [];
                    for(var i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: result.count,
                        recordsFiltered: result.count,
                        data: dataList
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        /*加载操作*/
        function loadOpearte() {
            $('#${menuId}_baseTable').find('.switch-status').each(function(i, r){
                var $this = $(r);
                switchStatusMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-status').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-status');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchStatusMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更状态为<span class="ml-1 ' + (_newState == 1 ? 'text-success' : 'text-danger') + '">' + (_newState == 1 ? '启用' : '禁用') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, status: _newState};
                            websocketService.request('034003', submitData, function(command){
                                if(command.success){
                                    _record.status = _newState;
                                    datatableUtil.setSwitchery(_switchery, !_switchery.isChecked());
                                    Swal.close();
                                } else {
                                    toastr.error(command.message || '提交异常', '系统提示');
                                    Swal.hideLoading();
                                }
                                me.saving = false;
                                $scope.$apply();
                            });
                        }
                    });
                }
            });
            baseTable && baseTable.columns.adjust();
            $compile($('#${menuId}_baseTable'))($scope);
            if(!hasClipboard) {
                clipboard = new ClipboardJS('.btn-copy');
                hasClipboard = true;
                clipboard.on('success', function(e) {
                    toastr.success('复制成功');
                    e.clearSelection();
                });
            }
        };
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload(null, false);
        };
        /*新增*/
        me.createRow = function(){
            me.currentTpl = {_title: '新增家族'};
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
            datatableUtil.setSwitchery(formSwitchStatus, true);
            $('#${menuId}_rowModal').modal('show');
        };
        /*修改*/
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = '修改家族信息：';
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                datatableUtil.setSwitchery(formSwitchStatus, (record.status == 1 ? true : false));
                $('#${menuId}_rowModal').modal('show');
            }
        };
        function testFee(data) {
            var result = false;
            if(data != null && data !='' && data != -1) {
                data = parseFloat(data);
                if(!isNaN(data) && data >= 0.1 && data <= 1){
                    result = true;
                }
            } else {
                result = true;
            }
            return result;
        }
        /*修改*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.familyGiftFee || !submitData.familyTicketFee || !submitData.familyChargeFee || !submitData.familyGuardFee) {
                toastr.warning('请完善分成比例', '温馨提示');
                return false;
            }
            if(!testFee(submitData.familyGiftFee) || !testFee(submitData.familyTicketFee) || !testFee(submitData.familyChargeFee)|| !testFee(submitData.familyGuardFee)) {
                toastr.warning('分成比例必须在0.1 ～ 1', '温馨提示');
                return false;
            }
            submitData.familyGiftFee = +(submitData.familyGiftFee);
            submitData.familyTicketFee = +(submitData.familyTicketFee);
            submitData.familyChargeFee = +(submitData.familyChargeFee);
            submitData.familyGuardFee = +(submitData.familyGuardFee);
            if(!submitData.name || !submitData.payeeName || !submitData.payeeAccount || !submitData.payeeType) {
                toastr.warning('请完善所有信息', '温馨提示');
                return false;
            }
            submitData.status = formSwitchStatus.isChecked() ? 1: 0;
            var methodCode = '034002';
            if(me.currentTpl.$type == 'edit') {
                methodCode = '034003';
                submitData.id = me.currentTpl.id;
            }
            me.saving = true;
            websocketService.request(methodCode, submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload(null, false);
                    me.currentTpl = null;
                    $('#${menuId}_rowModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="status"]').html(strHtml).trigger('change');
        };
        /* 收款类型 */
        function loadPayeeTypeDicDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in payeeTypeDic) {
                strHtml +='<option value="' + key + '">' + payeeTypeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="payeeType"]').html(strHtml).trigger('change');
        };
        function initPage() {
            loadStatusDic();
            loadPayeeTypeDicDic();
            /*日期查询*/
            datatableUtil.datepicker(true);
            initTalbe();
            /*伸缩表格头*/
            $(window).on('resize', function(){
                baseTable && baseTable.columns.adjust();
            });
            formSwitchStatus = new Switchery($('#${menuId}_form_status')[0], { color: '#4099ff', jackColor: '#fff' });
        };
        initPage();
        
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>