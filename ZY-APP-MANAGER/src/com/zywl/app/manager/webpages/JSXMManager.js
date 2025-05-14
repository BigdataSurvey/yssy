<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;

        let statusDic = {'1':'正常', '0':'解散'};
        let statusColorDic = {'0': 'text-gray', '1': 'text-success', '2': 'text-danger'};
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                scrollCollapse: true,
                bLengthChange: false,
                columns: [
                    {data: "idx", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "id", title: '仙门ID', render: datatableUtil.renderNormal},
                    {data: "immortalGateName", title: '仙门名称', render: datatableUtil.renderNormal},
                    {data: "userId", title: '掌门ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '掌门名称', render: datatableUtil.renderNormal},
                    {data: "immortalGateNum", title: '仙门人数', render: datatableUtil.renderNormal},
                    {data: "immortalGateLevel", title: '仙门等级', render: datatableUtil.renderNormal},
                    {data: "immortalGateMemberMax", title: '仙门最大人数', render: datatableUtil.renderNormal},
                    {data: "immortalGateBalance", title: '仙门资金', render: datatableUtil.renderNormal},
                    {data: "immortalGateExperience", title: '仙门经验值', render: datatableUtil.renderNormal},
                    {data: "immortalGateFighting", title: '仙门战力要求', render: datatableUtil.renderNormal},
                    {data: "createTime", title: '仙门创建时间', render: datatableUtil.renderTime},
                    {data: "updateTime", title: '仙门修改时间', render: datatableUtil.renderTime},
                    {data: "immortalGateStatus", title: '仙门名称修改次数', render: datatableUtil.renderNormal},
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
        function loadData(data, callback, settings){
            if(me.loading) return;
            me.loading = true;

            let searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('039020', searchData, function(command){
                if(command.success){
                    let result = command.data || {};
                    let dataList = result.list || [];
                    for(let i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i];
                    }
                    let resultData = {
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
                $scope.$apply()
            });
        };
        /*加载操作*/
        function loadOpearte() {
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        me.disGuild = function (id) {
            let _record = dataMap[id];
            if(_record) {
                alertUtil.alert({
                    html: "解散公会：" + '<span class="ml-1 text-success">' + _record.guildName + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {guildId: _record.id};
                        websocketService.request('021024', submitData, function(command){
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
        me.updateRow = function(id,status){
            let _record = dataMap[id];
            let str = "审核通过：";
            if(parseInt(status) == 0) {
                str = "审核拒绝：";
            }
            if(_record) {
                alertUtil.alert({
                    html: str + '<span class="ml-1 text-success">' + _record.userId + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {userId: _record.userId, status:status};
                        websocketService.request('021021', submitData, function(command){
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
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="immortalGateStatus"]').html(strHtml).trigger('change');
        };
        function initPage() {
            loadStatusDic();
            initTable();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>