<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;

        let statusDic = {'-1':'全部', '0':'封禁', '1': '通过', '2': '申请'};
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
                    {data: "id", title: '公会ID', render: datatableUtil.renderNormal},
                    {data: "guildName", title: '公会名称', render: datatableUtil.renderNormal},
                    {data: "userId", title: '会长ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '会长昵称', render: datatableUtil.renderNormal},
                    {data: "memberNumber", title: '公会人数', render: datatableUtil.renderNormal},
                    {data: "type", title: 'type', render: function(data, type, row, setting){
                            let str = "质押";
                            if(data == "1") {
                                str = "赠送";
                            }
                            return '<span class="">' + str + '</span>';
                        }},
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                            return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                        }},
                    {data: "remark", title: '原因', render: datatableUtil.renderNormal},
                    {data: "applyTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "createTime", title: '审核时间', render: datatableUtil.renderTime},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        let _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.disGuild(\'' + row.id + '\')"><i class="ti-slice"></i>解散</button>';
                        if(row.status == "2") {
                            _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\',1)"><i class="ti-slice"></i>通过</button>'
                                + '<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\',2)"><i class="ti-close"></i>拒绝</button>';
                        }

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
            websocketService.request('021020', searchData, function(command){
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
            $('#${menuId}_content select[name="status"]').html(strHtml).trigger('change');
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