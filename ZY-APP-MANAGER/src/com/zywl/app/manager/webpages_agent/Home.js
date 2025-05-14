<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $compile, $filter, $interval,$timeout, $templateCache, datatableUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        me.monitorData = $rootScope.agentData;
        me.agentUrl = $rootScope.agentUrl;
        me.agent = $rootScope.Agent;
        me.agent._createTime = datatableUtil.formatDate(new Date(me.agent.createTime), 'yyyy-MM-dd hh:mm');
        /*概况统计*/
        $scope.$on('syncAgentMonitor', function(e, command) {
            var result = command.data || {};
            var agentData = result.agentData || {};
            for(var key in agentData){
                me.monitorData[key] = agentData[key];
            }
            if(me.agentUrl != result.agentUrl) {
                me.agentUrl = result.agentUrl;
                loadQrcode(me.agentUrl);
            }
            $scope.$apply();
        });
        function loadQrcode(url) {
            $("#${menuId}_qrCode").html('').qrcode({ 
                width: 272,
                height: 272,
                text: url,
                render: !!document.createElement('canvas').getContext ? 'canvas' : 'table'
            });
        };
        function initPage() {
            loadQrcode(me.agentUrl);
            if(window.___clipboard) window.___clipboard.destroy();
            window.___clipboard = new ClipboardJS('#${menuId}_content .btn-copy');
            hasClipboard = true;
            window.___clipboard.on('success', function(e) {
                toastr.success('复制成功');
                e.clearSelection();
            });
            $('#${menuId}_content [data-toggle="tooltip"]').tooltip();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>