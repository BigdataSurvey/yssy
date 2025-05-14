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
                    case 'chat' :{
                        debug('客户咨询 - 新消息');
                        if($state.current.name != 'LIVE-APP.ChatManager'){
                            var data = command.data;
                            var type = data.type;
                            var message = data.message;
                            var params = {
                                title:'在线咨询',
                                body:message
                            };
                            if(type == 1){
                                params.body = '图片消息';
                                params.icon = '${staticfile}/files/assets/images/photo.png';
                            }
                            var notice = noticeService.message(params);
                            notice.onclick = function(){
                                $state.go('LIVE-APP.ChatManager', {params:command.data.senderId});
                                notice.close();
                            }
                        }
                        $scope.$broadcast('chat', command);
                        break;
                    }
                    case 'syncTaskNum' :{
                        debug('服务器负载 - 变更');
                        $scope.$broadcast('syncTaskNum', command);
                        break;
                    }
                    case 'syncAppOnline' :{
                        debug('在线状态 - 上线');
                        $scope.$broadcast('syncAppOnline', command);
                        break;
                    }
                    case 'syncAppOffline' :{
                        debug('在线状态 - 离线');
                        $scope.$broadcast('syncAppOffline', command);
                        break;
                    }
                    case 'syncAppChange' :{
                        debug('在线状态 - 变更');
                        $scope.$broadcast('syncAppChange', command);
                        break;
                    }
                    case 'syncMonitor' :{
                        debug('概况统计 - 变更');
                        $scope.$broadcast('syncMonitor', command);
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