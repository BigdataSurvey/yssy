<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, imageUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var switchStateMap = {};
        var baseTable = null;
        var hasInitRole = false;
        var roleDic = {};
        var formSwitchState = null;
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "username", title: '账号', render: datatableUtil.renderNormal},
                    {data: "name", title: '昵称', render: datatableUtil.renderNormal},
                    {data: "roleId", title: '角色', render: function(data, type, row, setting){
                        return data == null ? '-' : (roleDic[data] || '-');
                    }},
                    {data: "state", title: '状态', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-state" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "photo", title: '头像', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="头像" data-lightbox="'+ row.id +'"><img src="' + data + '" class="td-img"></a>';
                    }},
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
            websocketService.request('011001', null, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.admins || [];
                    for(var i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i]; 
                    }
                    me.defaultFeeTpl = result.defaultFee;
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: dataList.length,
                        recordsFiltered: dataList.length,
                        data: dataList
                    };
                    if(!hasInitRole) {
                        hasInitRole = true;
                        roleDic = result.roles;
                        loadRoleDic();
                    }
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
            });
        };
        /*加载操作*/
        function loadOpearte() {
            $('#${menuId}_baseTable').find('.switch-state').each(function(i, r){
                var $this = $(r);
                switchStateMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-state').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-state');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchStateMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更状态为<span class="ml-1 ' + (_newState == 1 ? 'text-success' : 'text-danger') + '">' + (_newState == 1 ? '启用' : '禁用') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, state: _newState};
                            websocketService.request('011003', submitData, function(command){
                                if(command.success){
                                    _record.status = _newState;
                                    datatableUtil.setSwitchery(_switchery, !_switchery.isChecked());
                                    Swal.close();
                                } else {
                                    toastr.error(command.message || '提交异常', '系统提示');
                                    Swal.hideLoading();
                                }
                                me.saving = false;
                            });
                        }
                    });
                }
            });
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        me.createRow = function(){
            me.currentTpl = {_title: '新增守护'};
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
            datatableUtil.setSwitchery(formSwitchState, true);
            $('#${menuId}_rowModal').modal('show');
        };
        /*修改*/
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.name;
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                datatableUtil.setSwitchery(formSwitchState, record.state == 1 ? true : false);
                $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_formEdit').find('input[name="password"]').val('').trigger('change');
                $('#${menuId}_form_photo').val('').trigger('change');
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
            if(!submitData.username || !submitData.name || !submitData.roleId) {
                toastr.warning('请输入完整信息', '温馨提示');
                return false;
            }
            if(me.currentTpl.photo) submitData.photo = me.currentTpl.photo;
            me.saving = true;
            var methodCode = '011002';
            if(me.currentTpl.$type == 'edit') {
                methodCode = '011003';
                submitData.id = me.currentTpl.id;
            }
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
        /* 角色 */
        function loadRoleDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in roleDic) {
                strHtml +='<option value="' + key + '">' + roleDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="roleId"]').html(strHtml).trigger('change');
        };
        function initPage() {
            initTalbe();
            formSwitchState = new Switchery($('#${menuId}_form_state')[0], { color: '#4099ff', jackColor: '#fff' });
            $('#${menuId}_form_photo').on('change', function(evt){
                var $this = $(this);
                var file = evt.target.files[0];
                if(file) {
                    me.saving = true;
                    imageUtil.getImg({
                        file: file,
                        callback: function(command){
                            if(command.success) {
                                me.currentTpl.photo = command.data.content;
                            } else {
                                toastr.error(command.message, '系统提示');
                                $this.val('').trigger('change');
                            }
                            me.saving = false;
                            $scope.$apply();
                        }
                    })
                }
            });
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>