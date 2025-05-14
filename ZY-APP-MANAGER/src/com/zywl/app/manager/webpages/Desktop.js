<@compress single_line=true>
LIVEAPP.controller('desktopCtrl', function($scope, $rootScope, $state, $timeout, $interval, $filter, socketEventService, toastr, alertUtil, amountUtil, storageService, websocketService, menuTree) {
    $(document).ready(function() {
        websocketService.connectServer(null, socketHandler);
    });
    function socketHandler(command) {
        if (websocketService.serverConnected()) {
            $scope.connected = true;
            var result = command.data || {};
            if(command.code === '0000'){
                if(result.login) {
                    $scope.$apply();
                    /*去掉遮罩层*/
                    $(".theme-loader").animate({
                        opacity: "0"
                    },1000);
                    setTimeout(function() {
                        $(".theme-loader").remove();
                    }, 1000);
                    /*默认菜单*/
                    me.Admin = result.admin;
                    if(window._defualtMenuId){
                        me.gotoMenu({id: window._defualtMenuId});
                    }
                    $rootScope.Admin = result.admin;
                    $rootScope.resourceUrl = result.resourceUrl;
                    $rootScope.serverOnlineNum = result.serverOnlineNum;
                    $rootScope.serverTask = result.serverTask;
                    $rootScope.monitorData = result.monitorData;
                    $rootScope.serverWeight = result.serverWeight;
                    initServerMap();
                    window._hasLogin = true;
                    if($rootScope.Items == null || $rootScope.Items == undefined) {
                        loadItems();
                    }
                } else {
                    toastr.error('登录超时', '系统提示');
                    setTimeout(function(){
                        window.location.reload();
                    }, 3000);
                }
            } else {
                socketEventService.process(command, $rootScope);
            }
        }
    };
    $rootScope.$on('$stateChangeSuccess', stateChangeSuccess);
    $rootScope.$on('$stateChangeStart', stateChangeStart);
    function stateChangeSuccess(){
        if(!me.hasInitMenu && me.currentMenuId) {
            me.hasInitMenu = true;
            $('li[data-id="' + me.currentMenuId + '"]').parents('li[data-id]').addClass('active pcoded-trigger');
        }
    };
    function stateChangeStart(){
        if(window._task) {
            $timeout.cancel(window._task);
            window._task = null;
        }
        /*对应Home.js方法*/
        window._syncTaskNum = null;
        window._syncAppOnline = null;
        window._syncAppChange = null;
        window._syncAppOffline = null;
        window._syncMonitor = null;
    };
    var me = this;
    me.menuList = menuTree || [];
    me.hasInitMenu = false;
    var monitorLastUpdate = new Date();
    var echartLastUpdateMap = {};
    me.getMenu = function (id) {
        for(k in me.menuList) {
            if(me.menuList[k].hasChild) {
                let childrens = me.menuList[k].children;
                for(key in childrens)
                if (childrens[key].id == id) {
                    return childrens[key];
                }
            }
        }
        return null;
    };
    /*菜单跳转*/
    me.gotoMenu = function(menu, e, params) {
        if(menu.hasChild) return;
        e && e.stopPropagation();
        if(params){
            $state.go("LIVE-APP." + menu.id, params);
        }else{
            $state.go("LIVE-APP." + menu.id);
        }
        $('.main-menu li[data-id]').removeClass('active');
        $('.main-menu li[data-id="' + menu.id + '"]').addClass('active').parents('li[data-id]').addClass('active pcoded-trigger');
        $('.main-menu li.pcoded-trigger').not('.active').removeClass('pcoded-trigger').find('.pcoded-submenu').slideUp();
        me.currentMenuId = menu.id;
    };
    me.menuFinished = function(){
        $.fn.pcodedmenu();
        <#if '${User.roleId}' =='fafa9fa225644438937feb755d5eae2f'>
        $('#mobile-collapse').trigger('click');
        </#if>
    };
    /*退出系统*/
    me.loginout = function() {
        alertUtil.alert({
            html: '即将退出系统？',
            icon: 'warning',
            confirm: function() {
                if(me.saving) return;
                me.saving = true;
                websocketService.request('002002', null, function(command){
                    window.location.reload();
                    me.saving = false;
                    Swal.close();
                });
            }
        });
    };
    function initServerMap() {
        $rootScope.serverMap = {};
        $rootScope.echartsDataMap = {};
        if($rootScope.serverOnlineNum) {
            var i = 1;
            for(var key in $rootScope.serverOnlineNum) {
                var _currentServer = {id: key, count: $rootScope.serverOnlineNum[key], index: i};
                var _currentTask = $rootScope.serverTask[key];
                $rootScope.echartsDataMap[key] = [];
                if(_currentTask) {
                    _currentServer.datetime = _currentTask.datetime;
                    _currentServer.task = _currentTask.task;
                    _currentServer.qps = _currentTask.qps;
                    _currentServer._task = parseInt(((_currentServer.qps + _currentServer.task) / _currentServer.qps - 1).toFixed(2) * 100) + ' %';
                    $rootScope.echartsDataMap[key].push({datetime: _currentTask.datetime, task: _currentTask.task, qps:  _currentTask.qps});
                }
                _currentServer.serverWeight = +($rootScope.serverWeight[key] || '0');
                $rootScope.serverMap[key] = _currentServer;
                i++;
            }
        }
    };
    /*服务负载变更*/
    $rootScope.$on('syncTaskNum', function(e, command) {
        var result = command.data || {};
        var nowTime = new Date();
        var chartLastUpdate = echartLastUpdateMap[result.key];
        if(chartLastUpdate && nowTime.getTime() < (chartLastUpdate.getTime() + 300)) {
            return;
        }
        echartLastUpdateMap[result.key] = new Date();
        var _currentServer = $rootScope.serverMap[result.key];
        if(!_currentServer) {
            _currentServer = {id: result.key, count: 0, index: Object.keys($rootScope.serverMap).length + 1};
            $rootScope.serverMap[result.key] = _currentServer;
            $rootScope.echartsDataMap[result.key] = [];
            $rootScope.echartsDataMap[result.key].push(result.value)
        } else {
            var _echartsData = $rootScope.echartsDataMap[result.key] || [];
            if(_echartsData.length > 50) {
                _echartsData.shift();
            }
            _echartsData.push(result.value);
            command._updateEcharts = true;
        }
        _currentServer.datetime = result.value.datetime;
        _currentServer.task = result.value.task;
        _currentServer.qps = result.value.qps || 0;
        _currentServer._task = parseInt(((_currentServer.qps + _currentServer.task) / _currentServer.qps - 1).toFixed(2) * 100) + ' %';
        if(result.value.serverWeight) {
            _currentServer.serverWeight = result.value.serverWeight;
        }
        window._syncTaskNum && window._syncTaskNum(command);
        $scope.$apply();
    });
    /*App上线*/
    $rootScope.$on('syncAppOnline', function(e, command) {
        var result = command.data || {};
        var _currentServer = $rootScope.serverMap[result.server];
        if(!_currentServer) {
            _currentServer = {id: result.server, count: 1, index: Object.keys($rootScope.serverMap).length + 1};
            $rootScope.serverMap[result.server] = _currentServer;
        }
        _currentServer.count = result.onlineNum;
        window._syncAppOnline && window._syncAppOnline(command);
        $scope.$apply();
    });
    /*App状态变更*/
    $rootScope.$on('syncAppChange', function(e, command) {
        var result = command.data || {};
        var _currentServer = $rootScope.serverMap[result.server];
        if(!_currentServer) {
            _currentServer = {id: result.server, count: 1, index: Object.keys($rootScope.serverMap).length + 1};
            $rootScope.serverMap[result.server] = _currentServer;
        }
        window._syncAppChange && window._syncAppChange(command);
        $scope.$apply();
    });
    /*App离线*/
    $rootScope.$on('syncAppOffline', function(e, command) {
        var result = command.data || {};
        var _currentServer = $rootScope.serverMap[result.server];
        if(_currentServer) {
            _currentServer.count = result.onlineNum;
        }
        window._syncAppOffline && window._syncAppOffline(command);
        $scope.$apply();
    });
    /*概况统计*/
    $rootScope.$on('syncMonitor', function(e, command) {
        var nowTime = new Date();
        if(nowTime.getTime() < (monitorLastUpdate.getTime() + 200)) return;
        monitorLastUpdate = new Date();
        var result = command.data || {};
        for(var key in result){
            if(key == 'totalAvailableAmount' || key == 'totalRealIncomeAmount') {
                var _amount = parseInt(('' + result[key]).replace(/,/g, ''));
                result[key] = amountUtil.formatCurrency(_amount);
            } else if(key == 'todayAmount') {
                result[key] = amountUtil.formatCurrency(result[key], 2);
            }
            $rootScope.monitorData[key] = result[key];
        }
        window._syncMonitor && window._syncMonitor(command);
        $scope.$apply();
    });
    function loadItems () {
        websocketService.request('021003', {}, function(command){
            if(command.success){
                $rootScope.Items = command.data || {};
            }
        });
    };
    function initMenuTree(data) {
        data.sort((a,b)=>{return a.sortId - b.sortId});
        let menuInfo = [];
        let idxInfo = {};
        menuInfo.push({id: 'Home', name: '总览', icon: 'ti-home'});
        for (k in data) {
            let menu = data[k];
            let info = {id:menu.menuKey,name:menu.menuName};
            if(menu.menuType == 0) {
                info = {id: menu.menuKey, name: menu.menuName, icon: 'ti-view-grid', hasChild: true, children:[]};
                let idx = menuInfo.push(info) - 1;
                idxInfo[menu.menuId] = idx;
            }else{
                let idx = idxInfo[menu.parentId];
                menuInfo[idx].children.push(info);
            }
        }
    }
    /*页面初始化*/
    function initPage() {
    };
    initPage();
});
</@compress>