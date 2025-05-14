<@compress single_line=true>
/**声音工具类*/
LIVEAPP.service("audioService", function($window) {
    return {
        _play : function(id, src) {
            var dom = document.getElementById(id);
            if (!dom) {
                $(document.body).append('<audio id="' + id + '" src="' + src + '"></audio>');
                dom = document.getElementById(id);
            }
            dom.play();
        },
        message : function() {
            this._play('LIVEAPP_AUDIO_MESSAGE', '${staticfile}/files/assets/audio/message.mp3');
        }
    }
});
</@compress>