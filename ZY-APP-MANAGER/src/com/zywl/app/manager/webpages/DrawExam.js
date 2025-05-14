<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var statusDic = {'0':'未审核', '1': '等待推送', '2': '推送至微信', '3': '提现成功', '4': '提现失败'};
        var statusColorDic = {'0': 'text-gray', '1': 'text-danger', '2': 'text-danger', '3': 'text-success', '4': 'text-danger'};
        var baseTable = null;
        me.currentTab = 1;
        me.switchTab = function(index){
            me.currentTab = index || 2;
            if(index == 1 && Object.keys(dataMap).length == 0){
                me.loadData();
            }
        };
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                select: 'single',
                columns: [
                    /*{data : "userId", targets : [0], orderable : false, render : function(data, type, row, setting) {
                            return '<input type="checkbox" value="'+ data + '" ng-change="${menuId}.onCheckChange(\'' + row.id + '\')" name="selectid"/>';
                        }
                    },*/
                    {data: "idx", title: '#', render: function(data, type, row, setting){
                            return setting.row + 1;
                        }},
                    {data: "id", title: 'ID', render: datatableUtil.renderNormal},
                    {data: "userId", title: '用户平台ID', render: datatableUtil.renderNormal},
                    {data: "userNo", title: '用户ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '用户昵称', render: datatableUtil.renderNormal},
                    {data: "realName", title: '真实姓名', render: datatableUtil.renderNormal},
                    {data: "openId", title: 'openID', render: datatableUtil.renderNormal},
                    {data: "orderNo", title: '订单号码', render: datatableUtil.renderNormal},
                    /*{data: "batchOrderNo", title: '微信批号', render: datatableUtil.renderNormal},*/
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                            return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                        }},
                    {data: "amount", title: '提现金额', render: datatableUtil.renderMoney},
                    {data: "receivedAmount", title: '收到金额', render: datatableUtil.renderMoney},
                    {data: "fee", title: '手续费', render: datatableUtil.renderMoney},
                    {data: "createTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "updateTime", title: '审核时间', render: datatableUtil.renderTime},
                    {data: "remark", title: '原因', render: datatableUtil.renderNormal},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.passRow(\'' + row.id + '\')"><i class="ti-check"></i>通过</button>'
                            +'<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-click="${menuId}.refuseRow(\'' + row.id + '\')"><i class="ti-close"></i>拒绝</button>';
                        return _opearHtml;
                    }}
                    ],
                    drawCallback: function(){
                        loadOpearte();
                    },
                    ajax: function (data, callback, settings) {
                        loadData(data, callback, settings);
                    }
            });

            /*baseTable.on('click','tr',function(e,d){
                console.log(e);
                console.log(d);
            });*/

        };
        /*请求数据*/
        function loadData(data, callback, settings) {
            if(me.loading) return;
            me.loading = true;
            var searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength,
                status: 0,
                type: 1
            };
            websocketService.request('021111', searchData, function(command){
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
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            if(me.currentTab == 1) {
                baseTable.ajax.reload(null, false);
            }
        };
        /*通过：个人*/
        me.passRow = function(id){
            var _record = dataMap[id];
            if(_record) {
                alertUtil.alert({
                    html: '审核通过：<span class="ml-1 text-success">' + _record.userNo + ' - ' + (_record.userName || '') + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {id: _record.id, action:1};
                        websocketService.request('021112', submitData, function(command){
                            if(command.success){
                                baseTable.ajax.reload(null, false);
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
        };
        function showPassTips(tips) {
            alertUtil.alert({
                html: tips,
                icon: 'success',
                onlyConfirm: true,
                confirm: function() {
                    Swal.close();
                }
            });
        }

        /*拒绝*/
        me.refuseRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.userNo + ' - ' + (record.userName || '') ;
                me.currentTpl._errorMsg = null;
                me.currentTpl._type = 1;
                $('#${menuId}_formRefuse').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formRefuse').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_examModal').modal('show');
            }
        };
        /*拒绝*/
        me.examData = function() {
            if(me.saving) return;
            var submitData = {}, methodCode = null;
            if(me.currentTpl._type == 1) {
                methodCode = '021112';
                submitData.id = me.currentTpl.id;
                submitData.action = 0;
            }
            submitData.mark = $('#${menuId}_formRefuse').find('textarea[name="mark"]').val();
            /*if(!submitData.mark) {
                toastr.warning('请输入拒绝原因', '温馨提示');
                return false;
            }*/
            if(!methodCode) {
                toastr.warning('无效的请求code', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request(methodCode, submitData, function(command){
                if(command.success){
                    toastr.success('申请已拒绝', '系统提示');
                    if(me.currentTpl._type == 1) {
                        baseTable.ajax.reload(null, false);
                    }
                    me.currentTpl = null;
                    $('#${menuId}_examModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        function onCheckChange(data) {
            console.log(data);
        }
        function initPage() {
            initTalbe();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId})
}
</@compress>