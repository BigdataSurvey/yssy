<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var baseTable = null;
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "username", title: '登录账号', render: datatableUtil.renderNormal},
                    {data: "name", title: '名称', render: datatableUtil.renderNormal},
                    {data: "downloadNum", title: '下载次数', render: datatableUtil.renderNormal},
                    {data: "registNum", title: '注册人数', render: datatableUtil.renderNormal},
                    {data: "paymentNum", title: '充值人数', render: datatableUtil.renderNormal},
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
            var searchData = {};
            websocketService.request('035001', searchData, function(command){
                if(command.success){
                    var dataList = command.data || [];
                    for(var i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: dataList.length,
                        recordsFiltered: dataList.length,
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
        };
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /*新增*/
        me.createRow = function(id){
            me.currentTpl = {};
            me.currentTpl._title = '新增代理信息';
            me.currentTpl._errorMsg = null;
            me.currentTpl._pwdTips = '请输入密码';
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
            $('#${menuId}_formEdit').find('input[name="password"]').val(uuid()).trigger('change');
            $('#${menuId}_rowModal').modal('show');
        };
        var _CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
        function uuid(len, radix){
            len = len || 10;
            var chars = _CHARS, uuid = [], i;
            radix = radix || chars.length;
            for (i = 0; i < len; i++)
                uuid[i] = chars[0 | Math.random() * radix];
            return uuid.join('');
        };
        /*修改*/
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = '修改代理信息：';
                me.currentTpl._errorMsg = null;
                me.currentTpl._pwdTips = '留空则不修改';
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_formEdit').find('input[name="password"]').val('').trigger('change');
                $('#${menuId}_rowModal').modal('show');
            }
        };
        /*修改*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.username) {
                toastr.warning('请输入登录账号', '温馨提示');
                return false;
            }
            var methodCode = '';
            if(me.currentTpl.$type == 'edit') {
                methodCode = '035003';
                submitData.id = me.currentTpl.id;
            } else if(me.currentTpl.$type =='add') {
                methodCode = '035002';
                if(!submitData.password) {
                    toastr.warning('请输入密码', '温馨提示');
                    return false;
                }
            }
            me.saving = true;
            websocketService.request(methodCode, submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_rowModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        function initPage() {
            initTalbe();
        };
        initPage();
        
        
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>