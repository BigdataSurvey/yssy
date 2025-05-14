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
                    {data: "id", title: 'ID', render: datatableUtil.renderNormal, visible:false},
                    {data: "userId", title: '用户ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '用户昵称', render: datatableUtil.renderNormal},
                    {data: "guildId", title: '公会ID', render: datatableUtil.renderNormal},
                    {data: "profitRate", title: '比例', render: datatableUtil.renderNormal},
                    {data: "profitBalance", title: '余额', render: datatableUtil.renderNormal},
                    {data: "roleId", title: '职位', render: function(data, type, row, setting){
                        let str = "会员";
                        if(parseInt(data) == 3) {
                            str = "会长";
                        }
                        return str;
                    }},
                    {data: "createUserId", title: '添加人ID', render: datatableUtil.renderNormal},
                    {data: "bailAmount", title: '保证金', render: datatableUtil.renderNormal},
                    {data: "status", title: '状态', render: datatableUtil.renderNormal},
                    {data: "remark", title: '原因', render: datatableUtil.renderNormal},
                    {data: "createTime", title: '审核时间', render: datatableUtil.renderTime},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                            if(parseInt(row.roleId) == 3) {
                                return '';
                            }
                            var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>踢出</button>';
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
            websocketService.request('021022', searchData, function(command){
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

        me.updateRow = function(id){
            let _record = dataMap[id];
            if(_record) {
                alertUtil.alert({
                    html: '踢出公会 ID: ' + '<span class="ml-1 text-success">' + _record.userId + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {userId: _record.userId};
                        websocketService.request('021023', submitData, function(command){
                            if(command.success){
                                baseTable.ajax.reload(null, false);
                                Swal.close();
                            } else {
                                toastr.error(command.message || '操作异常', '系统提示');
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