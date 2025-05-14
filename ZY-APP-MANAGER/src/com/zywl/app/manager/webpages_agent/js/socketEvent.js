<@compress single_line=true>
/**socketEventService类*/
LIVEAPP.service("socketEventService", function($rootScope, $state, $filter, noticeService) {
    function debug(msg){
        consoleLog && console.log('[socketEventService ' + $filter('date')(new Date(),'yyyy-MM-dd HH:mm:ss') + ']' + ' | ' + "接收到服务端事件推送：" + msg);
    }
    return {
        process : function(command, $scope){
            try{
                switch (command.code){
                    case 'syncAgentMonitor' :{
                        debug('代理概况统计 - 变更');
                        $scope.$broadcast('syncAgentMonitor', command);
                        break;
                    }
                }
            } catch (e){
                
            } finally {
                
            }
        }
    }
});
</@compress>