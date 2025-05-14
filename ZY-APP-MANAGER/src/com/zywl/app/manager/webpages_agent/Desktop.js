<@compress  single_line=true>
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
                    me.Admin = result.agent;
                    if(window._defualtMenuId){
                        me.gotoMenu({id: window._defualtMenuId});
                    }
                    $rootScope.Agent = result.agent;
                    $rootScope.agentData = result.agentData;
                    $rootScope.agentUrl = result.agentUrl;
                    window._hasLogin = true;
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
    };
    var me = this;
    me.menuList = menuTree || [];
    me.hasInitMenu = false;
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
    };
    /*退出系统*/
    me.loginout = function() {
        alertUtil.alert({
            html: '即将退出系统？',
            icon: 'warning',
            confirm: function() {
                if(me.saving) return;
                me.saving = true;
                websocketService.request('036002', null, function(command){
                    window.location.reload();
                    me.saving = false;
                    Swal.close();
                });
            }
        });
    };
    /*概况统计*/
    $rootScope.$on('syncAgentMonitor', function(e, command) {
        var result = command.data || {};
        $rootScope.agentUrl = result.agentUrl;
        var agentData = result.agentData || {};
        for(var key in agentData){
            $rootScope.agentData[key] = agentData[key];
        }
        $scope.$apply();
    });
    /*页面初始化*/
    function initPage() {
        
    };
    initPage();
})
</@compress>