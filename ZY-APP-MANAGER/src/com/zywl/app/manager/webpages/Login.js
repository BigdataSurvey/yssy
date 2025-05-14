<@compress single_line=true>
LIVEAPP.controller('loginCtrl', function($scope, $interval, storageService, toastr, websocketService) {
    var me = this;
    $(document).ready(function() {
        websocketService.connectServer(null, socketHandler);
    });
    function socketHandler() {
        if (websocketService.serverConnected()) {
            $scope.connected = true;
            $scope.$apply();
            $(".theme-loader").animate({
                opacity: "0"
            },1000);
            setTimeout(function() {
                $(".theme-loader").remove();
            }, 1000);
            setTimeout(function() {
                if (me.account)
                    $("input[name='password']").focus();
                else
                    $("input[name='account']").focus();
            }, 560);
        }
    };
    me.keyupLogin = function($event) {
        if($event.keyCode === 13 && me.account && me.password) {
            me.doLogin();
        }
    };
    /*登录*/
    me.doLogin = function(){
        if(me.loading) return;
        if(me.account && me.password) {
            me.loading = true;
            var submitData = {
                username: me.account,
                password: me.password
            };
            websocketService.request('002001', submitData, function(command) {
                if(command.success) {
                    storageService.set('__account', submitData.username);
                    storageService.set('__rememberMe', (me.rememberMe ? '1' : '0'));
                    window.location.reload();
                } else {
                    toastr.error(command.message || '登录失败', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        } else {
            toastr.warning('请输入账号和密码', '温馨提示');
        }
    };
    function initPage(){
        var account = storageService.get('__account');
        if(account) {
            me.account = account;
            me.hasAccount = true;
        }
        me.rememberMe = storageService.get('__rememberMe') == '1';
    };
    initPage();
});
</@compress>