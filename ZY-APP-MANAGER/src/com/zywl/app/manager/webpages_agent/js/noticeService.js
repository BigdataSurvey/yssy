<@compress single_line=true>
/**通知工具类*/
LIVEAPP.service("noticeService", function(audioService) {
    var Notification = window.Notification || window.mozNotification || window.webkitNotification;
    Notification && Notification.requestPermission();
    return {
        message : function(params) {
            audioService.message();
            if (Notification) {
                var notice = new Notification(params.title, angular.extend({
                    dir : 'auto',
                    lang : 'zh-CN',
                    icon : '${staticfile}/filse/assets/images/logo.png'
                }, params));
                return notice;
            }
        }
    }
});
</@compress>