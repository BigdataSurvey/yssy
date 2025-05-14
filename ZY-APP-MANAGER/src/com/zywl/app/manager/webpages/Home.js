<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $compile, $filter, $interval,$timeout, $templateCache, datatableUtil, amountUtil, storageService, websocketService, toastr, ngTableParams){
        var me = this;
        var serverWeight = $rootScope.serverWeight;
        me.monitorData = $rootScope.monitorData;
        var _totalAvailableAmount = parseInt(('' + me.monitorData.totalAvailableAmount).replace(/,/g, ''));
        var _totalRealIncomeAmount = parseInt(('' + me.monitorData.totalRealIncomeAmount).replace(/,/g, ''));
        me.monitorData.totalAvailableAmount = amountUtil.formatCurrency(_totalAvailableAmount);
        me.monitorData.totalRealIncomeAmount = amountUtil.formatCurrency(_totalRealIncomeAmount);
        me.serverTpl = {
            node: 0,
            people: 0,
            status: null,
            error: null,
            hasError: false,
            hasBusy: false,
            totalQps: 0
        };
        me.serverMap = $rootScope.serverMap;
        /*折线图插件*/
        var echartsMap = {};
        /*折线图数据*/
        var echartsDataMap = $rootScope.echartsDataMap;
        var totalTpl = {
            weight: 0,
            people: 0
        };
        var statusDic = {'0': '待支付', '1': '已支付', '2': '已结算', '3': '已失效'};
        var statusColorDic = {'0': 'text-muted', '1': 'text-success', '2': 'text-success', '3': 'text-gray'};
        var dataMap_deposit = {};
        /*请求数据*/
        function requestData_deposit() {
            return ;
            if(me.loading) return;
            me.loading = true;
            websocketService.request('028002', null, function(command){
                window._task = $timeout(function(){requestData_deposit();}, 5000);
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.list || [];
                    var _totalAmount = 0;
                    for(var i = 0; i < dataList.length; i++) {
                        var _record = dataList[i];
                        renderDeposit(_record);
                        dataMap_deposit[_record.id] = _record;
                        _totalAmount += +(_record.amount);
                    }
                    me.totalAmount = amountUtil.formatCurrency(_totalAmount, 2);
                    me.recordList_deposity = dataList;
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        function renderDeposit(record) {
            record.statusColor = statusColorDic[record.status] || '';
            record.statusName = statusDic[record.status] || '';
            record._goldAmount = datatableUtil.renderPrice(record.goldAmount);
            /*创建时间*/
            var _createTime = '-';
            if(record.createTime != null) {
                _createTime = datatableUtil.formatDate(new Date(record.createTime), 'MM-dd hh:mm:ss');
            }
            record._createTime = _createTime;
            /*支付时间*/
            var _payTime = '-';
            if(record.payTime != null) {
                _payTime = datatableUtil.formatDate(new Date(record.payTime), 'MM-dd hh:mm:ss');
            }
            record._payTime = _payTime;
            /*超时时间*/
            var _expiredTime = '-';
            if(record.expiredTime != null) {
                _expiredTime = datatableUtil.formatDate(new Date(record.expiredTime), 'MM-dd hh:mm:ss');
            }
            record._expiredTime = _expiredTime;
        };
        me.depositFinished = function(){};
        /*初始化总权重*/
        function loadWeight() {
            totalTpl.weight = 0;
            for(var key in serverWeight){
                totalTpl.weight += +(serverWeight[key] || '0');
            }
        };
        /*初始化集群信息*/
        var bgDic = ['bg-c-green', 'bg-c-blue', 'bg-c-purple', 'bg-c-red'];
        var errorMsgDic = ['', '在线人数不均衡', '节点繁忙', '负载过高'];
        var nameColorDic = ['text-c-white', 'text-c-blue', 'text-c-purple', 'text-c-red'];
        /*初始化集群负载*/
        function loadServerBg() {
            loadPeople();
            var _status = null, _error = '', _busy = '';
            if(totalTpl.weight > 0){
                for(var key in me.serverMap) {
                    var _currentServer = me.serverMap[key];
                    _currentServer._weight = _currentServer.serverWeight / totalTpl.weight;
                    _currentServer._minWeight = _currentServer._weight - 0.1;
                    if(_currentServer._minWeight < 0) _currentServer._minWeight = 0;
                    _currentServer._maxWeight = _currentServer._weight + 0.1;
                    if(_currentServer._maxWeight > 1) _currentServer._maxWeight = 1;
                    /*在线人数*/
                    _currentServer._minPeople = parseInt(totalTpl.people * _currentServer._minWeight);
                    _currentServer._maxPeople = parseInt(totalTpl.people * _currentServer._maxWeight);
                    if(_currentServer.count < 100 || (_currentServer.count >= _currentServer._minPeople && _currentServer.count <= _currentServer._maxPeople)) {
                        _currentServer._level = 0;
                    } else {
                        _currentServer._level = 1;
                    }
                    /*负载*/
                    if(_currentServer.task > 20 && _currentServer.task <= 50) {
                        _currentServer._level = 2;
                    } else if(_currentServer.task > 50) {
                        _currentServer._level = 3;
                        _busy = '负载过高';
                    }
                    /*背景*/
                    _currentServer.bgCss = bgDic[_currentServer._level] || '';
                    _currentServer.nameCss = nameColorDic[_currentServer._level] || '';
                    if(_currentServer._level > 0) {
                        _error += key + '：' + errorMsgDic[_currentServer._level] + '\r\n';
                        _status = '集群异常';
                    }
                }
            }
            /*更新集群信息*/
            me.serverTpl.node = Object.keys(me.serverMap).length;
            me.serverTpl.people = totalTpl.people;
            me.serverTpl.status = _status || '良好';
            me.serverTpl.error = _error || '暂无';
            me.serverTpl.hasError = _error ? true : false;
            me.serverTpl.hasBusy = _busy ? true : false;
        };
        /*初始化人数*/
        function loadPeople() {
            totalTpl.people = 0;
            for(var key in me.serverMap){
                totalTpl.people += +(me.serverMap[key].count || 0);
            }
        };
        me.serverFinished = function(){
            $timeout(function(){
                initChart();
            });
        };
        function initChart(){
            for(var key in echartsMap){
                var _oldEcharts = echartsMap[key];
                _oldEcharts.dispose();
            }
            echartsMap = {};
            for(var key in me.serverMap) {
                var _currentServer = me.serverMap[key];
                var _currentEcharts = echarts.init(document.getElementById('${menuId}_node_' + _currentServer.index));
                var _echartsData = echartsDataMap[key];
                var _xData = [], _yData = [], _yData1 = [];
                for(var i = 0; i < _echartsData.length; i++) {
                    var _currentEchartsData = _echartsData[i];
                    _xData.push(_currentEchartsData.datetime.substring(11, 19));
                    _yData.push(_currentEchartsData.task);
                    _yData1.push(_currentEchartsData.qps || 0);
                }
                var option = {
                    grid: {
                        top: 35,
                        bottom: 40,
                        left: 35
                    },
                    xAxis: {
                        type: 'category',
                        data: _xData,
                        axisLine: {
                            lineStyle: {
                                color: '#FFF'
                            }
                        }
                    },
                    yAxis: {
                        type: 'value',
                        minInterval: 1,
                        axisLine: {
                            lineStyle: {
                                color: '#FFF'
                            }
                        },
                        splitLine: {
                            show: false
                        }
                    },
                    series: [{
                        data: _yData,
                        type: 'line',
                        smooth: true,
                        lineStyle: {color: '#FFF'}
                    },{
                        data: _yData1,
                        type: 'line',
                        smooth: true,
                        lineStyle: {color: '#f5ffa8'}
                    }],
                    color:['#FFF'],
                    tooltip: {
                        show: true,
                        trigger: 'axis',
                        axisPointer: {
                            lineStyle: {color: '#fff'}
                        },
                        formatter: function(params){
                            var y0 = params[0], y1 = params[1];
                            var html = y0.name + '<br><div >QPS：' + (y1.data || 0) + '</div><div>负载：' + getQpsTask(y1.data, y0.data) + '</div>任务：' + y0.data;
                            return html;
                        }
                    },
                    dataZoom: [{
                        type: 'inside',
                        start: 0,
                        end: 100
                    }, {
                        start: 0,
                        end: 100,

                    }]
                };
                _currentEcharts.setOption(option);
                echartsMap[key] = _currentEcharts;
            }
        };
        /*删除用户*/
        function deleteUser(sessionId) {
            var id = dataMap_user[sessionId];
            if(id) {
                delete dataMap_user[sessionId];
                userDelete.push(sessionId);
                updateUserList();
            } else {
                id = dataMap_player[sessionId];
                if(id) {
                    delete dataMap_player[sessionId];
                    playerDelete.push(sessionId);
                    updatePlayerList();
                }
            }
        };
        /*服务负载变更*/
        window._syncTaskNum = function(command) {
            if(command._updateEcharts) {
                updateEchartsSimple(command.data);
            }
            loadWeight();
            loadServerBg();
            getTotalQps();
            $scope.$apply();
        };
        function updateEchartsSimple(data) {
            var _currentServer = me.serverMap[data.key];
            if(!_currentServer) return;
            var _echart = echartsMap[data.key];
            if(!_echart) return;
            var _echartsData = echartsDataMap[data.key] || [];
            var _xData = [], _yData = [], _yData1 = [];
            for(var i = 0; i < _echartsData.length; i++) {
                var _currentEchartsData = _echartsData[i];
                var _xTime = _currentEchartsData.datetime.substr(11, 19);
                _xData.push(_xTime);
                _yData.push(_currentEchartsData.task);
                _yData1.push(_currentEchartsData.qps);
            }
            var option = _echart.getOption();
            option.xAxis[0].data = _xData;
            option.series[0].data = _yData;
            option.series[1].data = _yData1;
            (_echart._lastcleartime || (_echart._lastcleartime = Date.now())) && Date.now() - _echart._lastcleartime > 60 * 30 * 1000 && (_echart._lastcleartime = Date.now() && _echart.clear());
            _echart.setOption(option);
        };
        /*App上线*/
        window._syncAppOnline = function(command) {
            loadServerBg();
        };
        /*App状态变更*/
        window._syncAppChange = function(command) {
            loadServerBg();
        };
        /*App离线*/
        window._syncAppOffline = function(command) {
            loadServerBg();
        };
        /*修改权重*/
        me.updateRow_weight = function(record) {
            me.currentTpl = record;
            me.currentTpl._title = record.id;
            me.currentTpl._errorMsg = null;
            $('#${menuId}_formEdit_weight').find('input[name="weight"]').val(record.serverWeight).trigger('change');
            $('#${menuId}_rowModal_weight').modal('show');
        };
        /*保存权重*/
        me.saveData_weight = function() {
            if(me.saving) return;
            var submitData = {
                name: me.currentTpl.id
            };
            $('#${menuId}_formEdit_weight').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.weight || parseFloat(submitData.weight) <= 0) {
                toastr.warning('权重必须大于0', '温馨提示');
                return false;
            }
            submitData.weight = +(submitData.weight);
            me.saving = true;
            websocketService.request('006001', submitData, function(command){
                if(command.success){
                    var record = me.serverMap[submitData.name];
                    if(record) {
                        record.serverWeight = submitData.weight;
                    }
                    me.currentTpl = null;
                    $('#${menuId}_formEdit_weight').find('input[name],select[name],textarea[name]').val('').trigger('change');
                    $('#${menuId}_rowModal_weight').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };

        function getTotalQps() {
            var totalNum = 0;
            for(var key in me.serverMap) {
                totalNum += (+(me.serverMap[key].qps || '0'));
            }
            me.serverTpl.totalQps = totalNum;
        };
        function getQpsTask(qps, task) {
            return ((qps + task) /qps - 1).toFixed(2) * 100 + ' %';
        };
        function initPage() {
            loadWeight();
            loadServerBg();
            requestData_deposit();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>