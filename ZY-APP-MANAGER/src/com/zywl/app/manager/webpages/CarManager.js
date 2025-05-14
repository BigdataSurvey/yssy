<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var switchDisableMap = {};
        var baseTable = null;
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
                    {data: "price", title: '每日单价', render: datatableUtil.renderPrice},
                    {data: "weekPrice", title: '周价格', render: function(data, type, row, setting){
                        var _html = null;
                        if(data && data != 0) {
                            _html = amountUtil.formatCurrency(data);
                        }
                        return _html || '-';
                    }},
                    {data: "monthPrice", title: '月价格', render: function(data, type, row, setting){
                        var _html = null;
                        if(data && data != 0) {
                            _html = amountUtil.formatCurrency(data);
                        }
                        return _html || '-';
                    }},
                    {data: "yearPrice", title: '年价格', render: function(data, type, row, setting){
                        var _html = null;
                        if(data && data != 0) {
                            _html = amountUtil.formatCurrency(data);
                        }
                        return _html || '-';
                    }},
                    {data: "icon", title: 'Icon', render: datatableUtil.renderIcon},
                    {data: "svgaIcon", title: 'svga图标', render: function(data, type, row, setting){
                        var _html = null;
                        if(data) {
                            _html = '<button class="btn waves-effect waves-light btn-link btn-sm" ng-click="${menuId}.showSvga(\'' + row.id + '\')"><i class="ti-image"></i>查看</button>';
                        }
                        return _html || '-';
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
            websocketService.request('022001', null, function(command){
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
                            websocketService.request('022002', submitData, function(command){
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
        me.showSvga = function(id){
            var _record = dataMap[id];
            if(_record && _record.svgaIcon){
                me.currentTpl = _record;
                me.currentTpl._title = '查看svga图标：';
                $('#${menuId}_loader-block').removeClass('d-none');
                $('#${menuId}_svga').addClass('d-none');
                var player = new SVGA.Player('#${menuId}_svga');
                var parser = new SVGA.Parser('#${menuId}_svga');
                $('#${menuId}_rowModal_svga').modal('show').on('shown.bs.modal', function(){
                    parser.load($rootScope.resourceUrl + _record.svgaIcon, function(videoItem) {
                        $('#${menuId}_loader-block').addClass('d-none');
                        $('#${menuId}_svga').removeClass('d-none');
                        player.setVideoItem(videoItem);
                        player.loops = 10;
                        player.startAnimation();
                    });
                }).on('hide.bs.modal', function(){
                    player.clear();
                });
            }
        };
        me.createRow = function(){
            me.currentTpl = {_title: '新增座驾'};
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
            datatableUtil.setSwitchery(formSwitchDisable, true);
            $('#${menuId}_rowModal').modal('show');
        };
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                datatableUtil.setSwitchery(formSwitchDisable, record.disable == 0 ? true : false);
                me.currentTpl = record;
                me.currentTpl._title = '编辑座驾';
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                for(var key in record) {
                    var _value = record[key];
                    if(_value == 0 && (key == 'weekPrice' || key == 'monthPrice' || key == 'yearPrice')) {
                        _value = '';
                    }
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(_value).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {disable: (formSwitchDisable.isChecked() ? 0 : 1)};
            $('#${menuId}_formEdit').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.name || !submitData.price || !submitData.icon) {
                toastr.warning('请输入完成信息', '温馨提示');
                return false;
            }
            if(submitData.price != null && !amountUtil.testInt(submitData.price)) {
                toastr.warning('每日单价 必须是正整数', '温馨提示');
                return false;
            }
            if(submitData.weekPrice != null && !amountUtil.testInt(submitData.weekPrice)) {
                toastr.warning('周价格必须是正整数', '温馨提示');
                return false;
            }
            if(submitData.monthPrice != null && !amountUtil.testInt(submitData.monthPrice)) {
                toastr.warning('月价格必须是正整数', '温馨提示');
                return false;
            }
            if(submitData.yearPrice != null && !amountUtil.testInt(submitData.yearPrice)) {
                toastr.warning('年价格必须是正整数', '温馨提示');
                return false;
            }
            if(!submitData.weekPrice) submitData.weekPrice = 0;
            if(!submitData.monthPrice) submitData.monthPrice = 0;
            if(!submitData.yearPrice) submitData.yearPrice = 0;
            submitData.expireAmount = 1;
            submitData.expireUnit = 5;
            me.saving = true;
            var methodCode = '022003';
            if(me.currentTpl.$type == 'edit') {
                methodCode = '022002';
                submitData.id = me.currentTpl.id;
            }
            websocketService.request(methodCode, submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
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
            formSwitchDisable = new Switchery($('#${menuId}_form_disable')[0], { color: '#4099ff', jackColor: '#fff' });
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>