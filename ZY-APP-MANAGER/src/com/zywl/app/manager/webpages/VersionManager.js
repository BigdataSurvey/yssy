<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, storageService, websocketService, toastr){
        var me = this;
        me.currentTab = 2;
        me.switchTab = function(index){
            me.currentTab = index || 2;
            if(index == 2 && Object.keys(dataMap_android).length == 0){
                me.loadData();
            } else if (index == 1 && Object.keys(dataMap_ios).length == 0){
                me.loadData();
            }
        };
        var dataMap_android = {};
        var dataMap_ios = {};
        var dataList_android = [];
        var dataList_ios = [];
        var typeDic = {1: 'IOS',2: 'Android'};
        var switchFcMap = {};
        var switchReleaseMap = {};
        var baseTable_android = null;
        var baseTable_ios = null;
        var currentQrCode = null;
        /*初始化表格*/
        function initTable(){
            baseTable_android = datatableUtil.init('#${menuId}_baseTable_android', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return data;
                    }},
                    {data: "versionName", title: '版本名称', render: datatableUtil.renderNormal},
                    {data: "versionNo", title: '版本号', render: datatableUtil.renderNormal},
                    {data: "fc", title: '强制升级', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-fc" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '" data-rowIndex="' + setting.row+ '">';
                    }},
                    {data: "release", title: '发布', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-release" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '" data-rowIndex="' + setting.row+ '">';
                    }},
                    {data: "updateTime", title: '发布时间', render: datatableUtil.renderNormal},
                    {data: "updateUrl", title: '下载', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a class="text-primary" href="' + data + '" target="_blank">下载</a>';
                    }},
                    {data: "description", title: '版本描述', render: datatableUtil.renderNormal},
                    {data: "key", title: '操作', width: '220px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>'
                            + '<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-click="${menuId}.deleteRow(\'' + row.id + '\')"><i class="ti-close"></i>删除</button>'
                            + '<button class="btn waves-effect waves-light btn-info btn-sm mr-2" ng-click="${menuId}.showQrCode(\'' + row.id + '\')"><i class="fa fa-qrcode"></i>二维码</button>';
                        return _opearHtml;
                    }},
                ],
                drawCallback: function(){
                    loadOpearte_android();
                },
                ajax: function (data, callback, settings) {
                    loadData_android(data, callback, settings);
                }
            });
            baseTable_ios = datatableUtil.init('#${menuId}_baseTable_ios', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return data;
                    }},
                    {data: "versionName", title: '版本名称', render: datatableUtil.renderNormal},
                    {data: "versionNo", title: '版本号', render: datatableUtil.renderNormal},
                    {data: "fc", title: '强制升级', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-fc" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '" data-rowIndex="' + setting.row+ '">';
                    }},
                    {data: "release", title: '发布', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-release" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '" data-rowIndex="' + setting.row+ '">';
                    }},
                    {data: "updateTime", title: '发布时间', render: datatableUtil.renderNormal},
                    {data: "updateUrl", title: '下载', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a class="text-primary" href="' + data + '" target="_blank">下载</a>';
                    }},
                    {data: "description", title: '版本描述', render: datatableUtil.renderNormal},
                    {data: "key", title: '操作', width: '220px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>'
                            + '<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-click="${menuId}.deleteRow(\'' + row.id + '\')"><i class="ti-close"></i>删除</button>'
                            + '<button class="btn waves-effect waves-light btn-info btn-sm mr-2" ng-click="${menuId}.showQrCode(\'' + row.id + '\')"><i class="ti-cloud-down"></i>二维码</button>';
                        return _opearHtml;
                    }},
                ],
                drawCallback: function(){
                    loadOpearte_ios();
                },
                ajax: function (data, callback, settings) {
                    loadData_ios(data, callback, settings);
                }
            });
        };
        /*请求数据*/
        function loadData_android(data, callback, settings){
            if(me.loading) return;
            me.loading = true;
            websocketService.request('005001', {type: 2}, function(command){
                if(command.success){
                    dataList_android = command.data || [];
                    for(var i = 0; i < dataList_android.length; i++) {
                        dataMap_android[dataList_android[i].id] = dataList_android[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: dataList_android.length,
                        recordsFiltered: dataList_android.length,
                        data: dataList_android
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        /*请求数据*/
        function loadData_ios(data, callback, settings){
            if(me.loading) return;
            me.loading = true;
            websocketService.request('005001', {type: 1}, function(command){
                if(command.success){
                    dataList_ios = command.data || [];
                    for(var i = 0; i < dataList_ios.length; i++) {
                        dataMap_ios[dataList_ios[i].id] = dataList_ios[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: dataList_ios.length,
                        recordsFiltered: dataList_ios.length,
                        data: dataList_ios
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
        function loadOpearte_android() {
            /*强制升级*/
            $('#${menuId}_baseTable_android').find('.switch-fc').each(function(i, r){
                var $this = $(r);
                switchFcMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable_android').find('.switch-fc').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-fc');
                var _id = $this.attr('data-id');
                var _record = dataMap_android[_id];
                var _switchery = switchFcMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 0 ? 'text-danger' : 'text-success') + '">' + (_newState == 0 ? '不强制升级' : '强制升级') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, fc: _newState};
                            websocketService.request('005004', submitData, function(command){
                                if(command.success){
                                    _record.fc = _newState;
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
            $('#${menuId}_baseTable_android').find('.switch-release').each(function(i, r){
                var $this = $(r);
                switchReleaseMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable_android').find('.switch-release').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-release');
                var _id = $this.attr('data-id');
                var _rowIndex = $this.attr('data-rowIndex');
                var _record = dataMap_android[_id];
                var _switchery = switchReleaseMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 0 ? 'text-danger' : 'text-success') + '">' + (_newState == 0 ? '下架' : '上架') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, release: _newState};
                            websocketService.request('005003', submitData, function(command){
                                if(command.success){
                                    _record.release = _newState;
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
            $compile($('#${menuId}_baseTable_android'))($scope);
        };
        /*加载操作*/
        function loadOpearte_ios() {
            /*强制升级*/
            $('#${menuId}_baseTable_ios').find('.switch-fc').each(function(i, r){
                var $this = $(r);
                switchFcMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable_ios').find('.switch-fc').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-fc');
                var _id = $this.attr('data-id');
                var _record = dataMap_ios[_id];
                var _switchery = switchFcMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 0 ? 'text-danger' : 'text-success') + '">' + (_newState == 0 ? '不强制升级' : '强制升级') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, fc: _newState};
                            websocketService.request('005004', submitData, function(command){
                                if(command.success){
                                    _record.fc = _newState;
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
            $('#${menuId}_baseTable_ios').find('.switch-release').each(function(i, r){
                var $this = $(r);
                switchReleaseMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable_ios').find('.switch-release').siblings('.switchery').on('click', function(e){
                var $this = $(this).siblings('.switch-release');
                var _id = $this.attr('data-id');
                var _rowIndex = $this.attr('data-rowIndex');
                var _record = dataMap_ios[_id];
                var _switchery = switchReleaseMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 0 ? 'text-danger' : 'text-success') + '">' + (_newState == 0 ? '下架' : '上架') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, release: _newState};
                            websocketService.request('005003', submitData, function(command){
                                if(command.success){
                                    _record.release = _newState;
                                    _record.updateTime = (_newState = 0 ? null : Math.round(new Date().getTime()/1000).toString());
                                    baseTable_ios.row(_rowIndex).invalidate().draw();
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
            $compile($('#${menuId}_baseTable_ios'))($scope);
        };
        /*刷新*/
        me.loadData = function() {
            if(me.currentTab == 2) {
                baseTable_android.ajax.reload();
            } else if(me.currentTab == 1){
                baseTable_ios.ajax.reload();
            }
        };
        me.uuid = function(){
            var s = [];
            var hexDigits = "0123456789abcdef";
            for (var i = 0; i < 36; i++) {
                s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
            }
            s[14] = "4";
            s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
            s[8] = s[13] = s[18] = s[23] = "";
            var uuid = s.join("");
            return uuid;
        };
        /*新增*/
        me.createRow = function(){
            me.currentTpl = {_title: '新增版本'};
            me.currentTpl.$type = 'add';
            var versionNo = 0;
            if(me.currentTab == 2) {
                versionNo = dataList_android.length > 0 ? parseInt(dataList_android[0].versionNo) + 1 : 0;
                $('#${menuId}_uploadfile').attr('accept', '.apk');
            } else if(me.currentTab == 1){
                versionNo = dataList_ios.length > 0 ? parseInt(dataList_ios[0].versionNo) + 1 : 0;
                $('#${menuId}_uploadfile').attr('accept', '.ipa');
            }
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
            $('#${menuId}_formEdit').find('input[name="id"]').val(me.uuid()).trigger('change');
            $('#${menuId}_formEdit').find('input[name="versionNo"]').val(versionNo).trigger('change');
            $('#${menuId}_formEdit').find('input[name="type"]').val(me.currentTab).trigger('change');
            $('#${menuId}_rowModal').modal('show');
        };
        /*修改*/
        me.updateRow = function(id){
            var record = null;
            if(me.currentTab == 2) {
                record = dataMap_android[id];
                me.currentTpl = record;
                me.currentTpl._title = '修改' + (typeDic[me.currentTab] || '') + '版本：';
                $('#${menuId}_uploadfile').attr('accept', '.apk');
            } else if(me.currentTab == 1) {
                record = dataMap_ios[id];
                me.currentTpl = record;
                me.currentTpl._title = '修改' + (typeDic[me.currentTab] || '') + '版本：';
                $('#${menuId}_uploadfile').attr('accept', '.ipa');
            }
            if(record && me.currentTpl) {
                me.currentTpl.$type = 'edit';
                me.currentTpl._errorMsg = null;
                $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        /*删除*/
        me.deleteRow = function(id) {
            var record = null;
            if(me.currentTab == 2) {
                record = dataMap_android[id];
            } else if(me.currentTab == 1) {
                record = dataMap_ios[id];
            }
            if(record) {
                alertUtil.alert({
                    html: '即将删除 ' + (typeDic[me.currentTab] || '') + ' <span class="ml-1 text-danger">' + record.versionName + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {id: record.id};
                        websocketService.request('005002', submitData, function(command){
                            if(command.success){
                                if(me.currentTab == 2) {
                                    baseTable_android.ajax.reload();
                                } else if(me.currentTab == 1) {
                                    baseTable_ios.ajax.reload();
                                }
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
        /*查看二维码*/
        me.showQrCode = function(id){
            var record = null;
            if(me.currentTab == 2) {
                record = dataMap_android[id];
            } else if(me.currentTab == 1) {
                record = dataMap_ios[id];
            }
            if(record) {
                me.currentTpl = record;
                $("#${menuId}_qrCode").html('').qrcode({ 
                    width: 272,
                    height: 272,
                    text: record.updateUrl,
                    render: !!document.createElement('canvas').getContext ? 'canvas' : 'table'
                });
                $('#${menuId}_rowModal_qrCode').modal('show');
            }
        };
        /*保存*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.versionName || !submitData.versionNo || !submitData.description) {
                toastr.warning('请输入版本名称、版本号、版本描述', '温馨提示');
                return false;
            }
            me.saving = true;
            var _postUrl = '//${host}/releaseApp';
            if(me.currentTpl.$type == 'edit') {
                _postUrl = '//${host}/updateApp';
            }
            $('#${menuId}_form_loading').removeClass('d-none');
            $("#${menuId}_formEdit").ajaxSubmit({
                success: function (data) {
                    if(data){
                        me.currentTpl._errorMsg = data || '提交异常';
                        toastr.error(data || '提交异常', '系统提示');
                    }else{
                        if(me.currentTab == 2) {
                            baseTable_android.ajax.reload();
                        } else if(me.currentTab == 1) {
                            baseTable_ios.ajax.reload();
                        }
                        $('#${menuId}_rowModal').modal('hide');
                    }
                    me.saving = false;
                    $('#${menuId}_form_loading').addClass('d-none');
                    $scope.$apply();
                },
                error:function(error){
                    me.currentTpl._errorMsg = error || '提交异常';
                    toastr.error(error || '提交异常', '系统提示');
                    me.saving = false;
                    $('#${menuId}_form_loading').addClass('d-none');
                    $scope.$apply();
                },
                url: _postUrl,
                type:"post"
            });
        };
        /* 类型 */
        function loadTypeDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in typeDic) {
                strHtml +='<option value="' + key + '">' + typeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="type"]').html(strHtml).trigger('change');
        };
        /*初始化*/
        function initPage() {
            loadTypeDic();
            initTable();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>