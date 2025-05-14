<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        me.currentTab = 1;/*1,2*/
        me.switchTab = function(index){
            me.currentTab = index || 1;
            if(index == 1 && Object.keys(dataMap_customer).length == 0){
                me.loadData();
            } else if (index == 2 && Object.keys(dataMap_player).length == 0){
                me.loadData();
            }
        };
        var dataMap_customer = {};
        var dataMap_player = {};
        var switchDisableMap_customer = {};
        var switchDisableMap_player = {};
        /*初始化表格*/
        var baseTable_customer = null;
        var baseTable_player = null;
        function initTable() {
            baseTable_customer = datatableUtil.init('#${menuId}_baseTable_customer', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "level", title: '等级', render: datatableUtil.renderPrice},
                    {data: "alias", title: '名称', render: datatableUtil.renderNormal},
                    {data: "icon", title: '图标', render: datatableUtil.renderIcon},
                    {data: "color", title: '底色', render: datatableUtil.renderNormal},
                    {data: "color", title: '预览', render: function(data, type, row, setting){
                        var _html = null;
                        if(row.icon && row.color) {
                            
                            _html = '<span class="td-look" style="background-color:'+ row.color +'"><i style="background-image:url(\'' + $rootScope.resourceUrl + row.icon + '\')"></i> ' + row.alias +'</span>';
                        }
                        return _html || '-';
                    }},
                    {data: "require", title: '升级所需', render: datatableUtil.renderPrice},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>';
                        return _opearHtml;
                    }},
                    ],
                    drawCallback: function(){
                        loadOpearte_customer();
                    },
                    ajax: function (data, callback, settings) {
                        loadData_customer(data, callback, settings);
                    }
            });
            baseTable_player = datatableUtil.init('#${menuId}_baseTable_player', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "level", title: '等级', render: datatableUtil.renderPrice},
                    {data: "alias", title: '名称', render: datatableUtil.renderNormal},
                    {data: "icon", title: '图标', render: datatableUtil.renderIcon},
                    {data: "color", title: '底色', render: datatableUtil.renderNormal},
                    {data: "color", title: '预览', render: function(data, type, row, setting){
                        var _html = null;
                        if(row.icon && row.color) {
                            
                            _html = '<span class="td-look" style="background-color:'+ row.color +'"><i style="background-image:url(\'' + $rootScope.resourceUrl + row.icon + '\')"></i> ' + row.alias +'</span>';
                        }
                        return _html || '-';
                    }},
                    {data: "require", title: '升级所需', render: datatableUtil.renderPrice},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>';
                        return _opearHtml;
                    }},
                    ],
                    drawCallback: function(){
                        loadOpearte_player();
                    },
                    ajax: function (data, callback, settings) {
                        loadData_player(data, callback, settings);
                    }
            });
        };
        /*请求数据：客户*/
        function loadData_customer(data, callback, settings){
            if(me.loading) return;
            me.loading = true;
            var searchData = {type: 1};
            websocketService.request('021001', searchData, function(command){
                if(command.success){
                    var result = command.data || [];
                    for(var i = 0; i < result.length; i++) {
                        dataMap_customer[result[i].id] = result[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: result.length,
                        recordsFiltered: result.length,
                        data: result
                    };
                    callback(resultData);
                    if(result.length == 0) {
                        showTips(searchData.type);
                    }
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        /*请求数据：主播*/
        function loadData_player(data, callback, settings){
            if(me.loading) return;
            me.loading = true;
            var searchData = {type: 2};
            websocketService.request('021001', searchData, function(command){
                if(command.success){
                    var result = command.data || [];
                    for(var i = 0; i < result.length; i++) {
                        dataMap_player[result[i].id] = result[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: result.length,
                        recordsFiltered: result.length,
                        data: result
                    };
                    callback(resultData);
                    if(result.length == 0) {
                        showTips(searchData.type);
                    }
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        function showTips(type){
            alertUtil.alert({
                html: '暂无<span class="text-danger">' + (type == 1 ? '观众等级': '主播等级') +'</span>，是否现在设置 ？',
                icon: 'warning',
                confirm: function() {
                    me.createRow();
                    Swal.close();
                    $scope.$apply();
                }
            });
        };
        /*加载操作*/
        function loadOpearte_customer() {
            $compile($('#${menuId}_baseTable_customer'))($scope);
        };
        /*加载操作*/
        function loadOpearte_player() {
            $compile($('#${menuId}_baseTable_player'))($scope);
        };
        /*刷新*/
        me.loadData = function() {
            if(me.currentTab == 1) {
                baseTable_customer.ajax.reload();
            } else if(me.currentTab == 2){
                baseTable_player.ajax.reload();
            }
        };
        /*创建*/
        me.createRow = function(){
            me.currentTpl = {type: me.currentTab};
            if(me.currentTab == 1) {
                me.currentTpl._title = '观众等级';
            } else if(me.currentTab == 2){
                me.currentTpl._title = '主播等级';
            }
            $('#${menuId}_rowModal_init').modal('show');
        };
        /*修改*/
        me.updateRow = function(id){
            var record = null;
            if(me.currentTab == 1) {
                record = dataMap_customer[id];
                me.currentTpl = record;
                me.currentTpl._title = '观众等级 - ' + record.alias;
            } else if(me.currentTab == 2) {
                record = dataMap_player[id];
                me.currentTpl = record;
                me.currentTpl._title = '主播等级 - ' + record.alias;
            }
            if(record && me.currentTpl) {
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        /*初始化等级*/
        me.saveData_init = function() {
            if(me.saving) return;
            var submitData = {
                type: me.currentTpl.type,
                maxLevel: me.currentTpl.maxLevel
            };
            if(!amountUtil.testInt(submitData.maxLevel)) {
                toastr.warning('最大等级必须为正整数', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request('021002', submitData, function(command){
                if(command.success){
                    if(submitData.type == 1) {
                        baseTable_customer.ajax.reload();
                    } else if(submitData.type == 2) {
                        baseTable_player.ajax.reload();
                    }
                    me.currentTpl = null;
                    $('#${menuId}_rowModal_init').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        /*保存*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {id: me.currentTpl.id};
            $('#${menuId}_formEdit').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.alias || !submitData.require) {
                toastr.warning('请输入名称、升级所需', '温馨提示');
                return false;
            }
            if(!amountUtil.testInt(submitData.require) && submitData.require != '0') {
                toastr.warning('升级所需必须为正整数', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request('021003', submitData, function(command){
                if(command.success){
                    if(me.currentTab == 1) {
                        baseTable_customer.ajax.reload();
                    } else if(me.currentTab == 2) {
                        baseTable_player.ajax.reload();
                    }
                    me.currentTpl = null;
                    $('#${menuId}_rowModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        function initPage() {
            initTable();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>