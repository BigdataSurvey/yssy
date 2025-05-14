<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var switchDisableMap = {};
        var switchTiaodanMap = {};
        var switchLianjiMap = {};
        /*初始化表格*/
        var baseTable = null;
        var levelMap = {
            'tiaodan/1.mp3': {name: '最小', index: 1},
            'tiaodan/2.mp3': {name: '小', index: 2},
            'tiaodan/3.mp3': {name: '中', index: 3},
            'tiaodan/4.mp3': {name: '大', index: 4},
            'tiaodan/5.mp3': {name: '最大', index: 5}
        };
        var formSwitchTiaodan = null;
        var formSwitchLianji = null;
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
                    {data: "icon", title: '图标', render: datatableUtil.renderIcon},
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
                    {data: "lianji", title: '可连击', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-lianji" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "tiaodan", title: '跳蛋', render: function(data, type, row, setting){
                        return '<span class="' + (data == 1 ? 'text-success': 'text-gray') + '">' + (data == 1 ? '启用': '禁用') + '</span>';
                    }},
                    {data: "tiaodanLevel", title: '跳蛋震动级别', render: function(data, type, row, setting){
                        var _html = null;
                        if(row.tiaodan == 1 && data) {
                            _html = data;
                        }
                        return _html || '-';
                    }},
                    {data: "audio", title: '跳蛋音乐', render: function(data, type, row, setting){
                        var _html = null;
                        if(row.tiaodan == 1 && data) {
                            var _level = levelMap[data];
                            if(_level) {
                                _html =  _level.name;
                            }
                        }
                        return _html || '-';
                    }},
                    {data: "tiaodanTime", title: '跳蛋震动时间(s)', render: function(data, type, row, setting){
                        var _html = null;
                        if(data) {
                            _html = parseInt(data/1000);;
                        }
                        return _html == null ? '-' : _html;
                    }},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>';
                        if(row.tiaodan == 1 && row.audio) {
                            _opearHtml += '<button class="btn waves-effect waves-light btn-info btn-sm mr-2" ng-click="${menuId}.palyAudio(\'' + row.audio + '\')"><i class="ti-control-play"></i>播放</button>';
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
            websocketService.request('013001', null, function(command){
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
            $('#${menuId}_baseTable').find('.switch-disable').each(function(i, r){
                var $this = $(r);
                switchDisableMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-disable').siblings('.switchery')
            .on('click', function(e){
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
                            websocketService.request('013002', submitData, function(command){
                                if(command.success){
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
            $('#${menuId}_baseTable').find('.switch-tiaodan').each(function(i, r){
                var $this = $(r);
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                if(_record && _record.svgaIcon){
                    var player = new SVGA.Player('#${menuId}_svga');
                    var parser = new SVGA.Parser('#${menuId}_svga');
                    $('#${menuId}_rowModal_svga').modal('show').on('shown.bs.modal', function(){
                        parser.load($rootScope.resourceUrl + _record.svgaIcon, function(videoItem) {
                            player.setVideoItem(videoItem);
                            player.loops = 10;
                            player.startAnimation();
                        });
                    }).on('hide.bs.modal', function(){
                        player.clear();
                        $('#${menuId}_svga').html('');
                    });
                }
            });
            /*可连击*/
            $('#${menuId}_baseTable').find('.switch-lianji').each(function(i, r){
                var $this = $(r);
                switchDisableMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-lianji').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-lianji');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchDisableMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更为<span class="ml-1 ' + (_newState == 1 ? 'text-success' : 'text-danger') + '">' + (_newState == 1 ? '可连击' : '不可连击') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, lianji: _newState};
                            websocketService.request('013002', submitData, function(command){
                                if(command.success){
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
        };
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
                    $('#${menuId}_svga').html('');
                });
            }
        };
        me.createRow = function(){
            me.currentTpl = {_title: '新增礼物'};
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
            datatableUtil.setSwitchery(formSwitchTiaodan, true);
            datatableUtil.setSwitchery(formSwitchLianji, true);
            $('#${menuId}_rowModal').modal('show');
        };
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = '编辑礼物：';
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                for(var key in record) {
                    var _value = record[key];
                    if(key == 'tiaodanTime' && _value && parseInt(_value) > 0) {
                        _value = parseInt(_value / 1000);
                    }
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(_value).trigger('change');
                }
                datatableUtil.setSwitchery(formSwitchTiaodan, record.tiaodan == 1 ? true : false);
                datatableUtil.setSwitchery(formSwitchLianji, record.lianji == 1 ? true : false);
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
            submitData.tiaodan = (formSwitchTiaodan.isChecked() ? 1 : 0);
            submitData.lianji = (formSwitchLianji.isChecked() ? 1 : 0);
            if(!submitData.name || !submitData.price || !submitData.icon) {
                toastr.warning('请输入完成信息', '温馨提示');
                return false;
            }
            if(!amountUtil.testInt(submitData.price)) {
                toastr.warning('价格必须是正整数', '温馨提示');
                return false;
            }
            if(submitData.tiaodan == 1) {
                if(!amountUtil.testInt(submitData.tiaodanLevel) || parseInt(submitData.tiaodanLevel) < 1 || parseInt(submitData.tiaodanLevel) > 20) {
                    toastr.warning('跳蛋震动级别是1 ～ 20的正整数', '温馨提示');
                    return false;
                }
                if(!amountUtil.testInt(submitData.tiaodanTime)) {
                    toastr.warning('跳蛋震动时间是正整数', '温馨提示');
                    return false;
                } else {
                    submitData.tiaodanTime = parseInt(submitData.tiaodanTime) * 1000;
                }
            } else {
                submitData.tiaodanLevel = null;
                submitData.tiaodanTime = null;
                submitData.audio = '';
            }
            me.saving = true;
            var methodCode = '013002';
            if(me.currentTpl.$type == 'add') {
                methodCode = '013003';
                submitData.disable = 0;
            } else {
                submitData.id = me.currentTpl.id;
            }
            if(submitData.svgaIcon == null) submitData.svgaIcon = '';
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
        /*播放音频*/
        me.palyAudio = function(data){
            var _val = data;
            if(!_val) {
                _val = $('#${menuId}_formEdit').find('select[name="audio"]').val();
            }
            var _level = levelMap[_val];
            if(_level) {
                document.getElementById("${menuId}_audio_" + _level.index).play();
            }
        };
        /* 跳蛋音乐 */
        function loadAudioDic() {
            var strHtml = '<option value="">--请选择--</option>';
            var audioHtml = '';
            for(var key in levelMap){
                var _level = levelMap[key];
                strHtml +='<option value="' + key + '">' + _level.name + '</option>';
                if(key) {
                    audioHtml += '<audio id="${menuId}_audio_' + _level.index + '"><source src="' + $rootScope.resourceUrl + key + '"></audio>';
                }
            }
            $('#${menuId}_content select[name="audio"]').html(strHtml).trigger('change');
            $('#${menuId}_audio_div').html(audioHtml);
        };
        function initPage() {
            initTable();
            loadAudioDic();
            formSwitchTiaodan = new Switchery($('#${menuId}_form_tiaodan')[0], { color: '#4099ff', jackColor: '#fff' });
            formSwitchLianji = new Switchery($('#${menuId}_form_lianji')[0], { color: '#4099ff', jackColor: '#fff' });
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>