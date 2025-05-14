<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var switchNoTalkMap = {};
        var switchNoOutMap = {};
        var switchDisableMap = {};
        var baseTable = null;
        var formSwitchNoTalk = null;
        var formSwitchNoOut = null;
        var formSwitchDisable = null;
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "name", title: '名称', render: datatableUtil.renderNormal},
                    {data: "price", title: '价格', render: datatableUtil.renderPrice},
                    {data: "icon", title: 'Icon', render: datatableUtil.renderIcon},
                    {data: "expireAmount", title: '有效期（天）', render: datatableUtil.renderPrice},
                    {data: "noTalk", title: '可防止房管禁言', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-noTalk" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "noOut", title: '可防止房管踢人', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-noOut" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "disable", title: '状态', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-disable" ' + (data == 1 ? '': 'checked') + ' data-id="' + row.id + '">';
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
        function loadData(data, callback, settings){
            if(me.loading) return;
            me.loading = true;
            var searchData = {};
            websocketService.request('009001', searchData, function(command){
                if(command.success){
                    var result = command.data || [];
                    for(var i = 0; i < result.length; i++) {
                        dataMap[result[i].id] = result[i]; 
                    }
                    var resultData = {
                            draw: data.draw,
                            recordsTotal: result.length,
                            recordsFiltered: result.length,
                            data: result
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
            });
        };
        /*加载操作*/
        function loadOpearte() {
            /*防止房管禁用*/
            $('#${menuId}_baseTable').find('.switch-noTalk').each(function(i, r){
                var $this = $(r);
                switchNoTalkMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-noTalk').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-noTalk');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchNoTalkMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 0 ? 'text-danger' : 'text-success') + '">' + (_newState == 0 ? '可被房管禁言' : '防止房管禁言') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, noTalk: _newState};
                            websocketService.request('009002', submitData, function(command){
                                if(command.success){
                                    _record.noTalk = _newState;
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
            /*启用禁用*/
            $('#${menuId}_baseTable').find('.switch-noOut').each(function(i, r){
                var $this = $(r);
                switchNoOutMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-noOut').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-noOut');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchNoOutMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 0 ? 'text-danger' : 'text-success') + '">' + (_newState == 0 ? '可被房管踢人' : '防止房管踢人') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, noOut: _newState};
                            websocketService.request('009002', submitData, function(command){
                                if(command.success){
                                    _record.noOut = _newState;
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
            /*启用禁用*/
            $('#${menuId}_baseTable').find('.switch-disable').each(function(i, r){
                var $this = $(r);
                switchDisableMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-disable').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-disable');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchDisableMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 1: 0;
                    alertUtil.alert({
                        html: '即将变更状态为<span class="ml-1 ' + (_newState == 0 ? 'text-success' : 'text-danger') + '">' + (_newState == 0 ? '启用' : '禁用') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, disable: _newState};
                            websocketService.request('009002', submitData, function(command){
                                if(command.success){
                                    _record.disable = _newState;
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
            $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
            datatableUtil.setSwitchery(formSwitchNoTalk, true);
            datatableUtil.setSwitchery(formSwitchNoOut, true);
            datatableUtil.setSwitchery(formSwitchDisable, true);
            $('#${menuId}_rowModal').modal('show');
        };
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                datatableUtil.setSwitchery(formSwitchNoTalk, record.noTalk == 1 ? true : false);
                datatableUtil.setSwitchery(formSwitchNoOut, record.noOut == 1 ? true : false);
                datatableUtil.setSwitchery(formSwitchDisable, record.disable == 0 ? true : false);
                me.currentTpl = record;
                me.currentTpl._title = '编辑守护';
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.name || !submitData.price || !submitData.icon || !submitData.expireAmount) {
                toastr.warning('请输入完整信息', '温馨提示');
                return false;
            }
            if(!amountUtil.testInt(submitData.price) || !amountUtil.testInt(submitData.expireAmount)) {
                toastr.warning('价格、有效期 必须是正整数', '温馨提示');
                return false;
            }
            me.saving = true;
            submitData.expireUnit = 5;
            submitData.noTalk = (formSwitchNoTalk.isChecked() ? 1 : 0);
            submitData.noOut = (formSwitchNoOut.isChecked() ? 1 : 0);
            submitData.disable = (formSwitchDisable.isChecked() ? 0 : 1);
            var methodCode = '009002';
            if(me.currentTpl.$type == 'add') {
                methodCode = '009003';
            } else {
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
        function initPage() {
            initTable();
            formSwitchNoTalk = new Switchery($('#${menuId}_form_noTalk')[0], { color: '#4099ff', jackColor: '#fff' });
            formSwitchNoOut = new Switchery($('#${menuId}_form_noOut')[0], { color: '#4099ff', jackColor: '#fff' });
            formSwitchDisable = new Switchery($('#${menuId}_form_disable')[0], { color: '#4099ff', jackColor: '#fff' });
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>