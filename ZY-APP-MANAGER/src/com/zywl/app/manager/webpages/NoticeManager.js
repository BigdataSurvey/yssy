<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var switchDisplayMap = {};
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
                    {data: "title", title: '标题', render: datatableUtil.renderNormal},
                    {data: "index", title: '排序', render: datatableUtil.renderNormal},
                    {data: "display", title: '显示', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-display" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "createTime", title: '添加时间', render: datatableUtil.renderTime},
                    {data: "releaseTime", title: '发布时间', render: datatableUtil.renderTime},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-disabled="${menuId}.saving" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>'
                            +'<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-disabled="${menuId}.saving" ng-click="${menuId}.deleteRow(\'' + row.id + '\')"><i class="ti-close"></i>删除</button>';
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
            var searchData = {type: 1};
            websocketService.request('004001', searchData, function(command){
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
            });
        };
        /*加载操作*/
        function loadOpearte() {
            $('#${menuId}_baseTable').find('.switch-display').each(function(i, r){
                var $this = $(r);
                switchDisplayMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-display').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-display');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchDisplayMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 1 ? 'text-success' : 'text-danger') + '">' + (_newState == 1 ? '显示' : '隐藏') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, display: _newState};
                            websocketService.request('004006', submitData, function(command){
                                if(command.success){
                                    _record.display = _newState;
                                    _record.releaseTime = Math.round(new Date().getTime()/1000).toString();
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
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        me.createRow = function(){
            me.currentTpl = {_title: '新增公告'};
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
            $('#${menuId}_context').froalaEditor('html.set', '');
            $('#${menuId}_formEdit').find('input[name="index"]').val('1').trigger('change');
            $('#${menuId}_rowModal').modal('show');
        };
        /*删除*/
        me.deleteRow = function(id) {
            var record = dataMap[id];
            if(record) {
                alertUtil.alert({
                    html: '即将删除<span class="ml-1 text-danger">' + record.title + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        websocketService.request('004005', {id: record.id}, function(command){
                            if(command.success){
                                baseTable.ajax.reload();
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
        };
        /*修改*/
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.saving = true;
                websocketService.request('004002', {id: record.id}, function(command){
                    if(command.success){
                        me.currentTpl = record;
                        me.currentTpl._title = '编辑公告：';
                        me.currentTpl._errorMsg = null;
                        me.currentTpl.$type = 'edit';
                        $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                        for(var key in record) {
                            $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(record[key]).trigger('change');
                        }
                        $('#${menuId}_context').froalaEditor('html.set', command.data || '');
                        $('#${menuId}_rowModal').modal('show');
                    } else {
                        toastr.error(command.message || '提交异常', '系统提示');
                    }
                    me.saving = false;
                    $scope.$apply();
                });
            }
        };
        /*修改*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {context: $('#${menuId}_context').froalaEditor('html.get')};
            $('#${menuId}_formEdit').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.title || !submitData.context) {
                toastr.warning('请输入标题、内容', '温馨提示');
                return false;
            }
            if(!amountUtil.testInt(submitData.index)) {
                toastr.warning('排序必须为正整数', '温馨提示');
                return false;
            }
            if(me.currentTpl.$type == 'edit'){
                submitData.id = me.currentTpl.id;
                submitData.display = +(me.currentTpl.display);
            } else {
                submitData.display = 0;
            }
            submitData.type = 1;
            submitData.readOnly = 2;
            me.saving = true;
            websocketService.request('004003', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                    $('#${menuId}_context').froalaEditor('html.set', '');
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
            initTalbe();
            $('#${menuId}_context').froalaEditor({
                language: 'zh_cn',
                height: 400,
                imageUploadURL: '//${host}/newsImageUpload',
                imageUploadMethod: 'POST',
                imageMaxSize: 5 * 1024 * 1024, /*5M*/
                placeholderText: '请输入内容...'
            }).on('froalaEditor.image.uploaded', function (e, editor, response) {});
            
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>