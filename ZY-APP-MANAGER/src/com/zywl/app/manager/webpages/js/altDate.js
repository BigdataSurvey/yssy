<@compress single_line=true>
/**sweetalert的工具类*/
LIVEAPP.filter("altDate", function() {
    return function (value){
        var _now = Date.now();
        var _currentValue = new Date(value);
        var diff = _now - _currentValue;
        if (diff < (60 * 60 * 1000)){
            return moment(value).fromNow();
        }else if ( diff < (60 * 60 * 24 * 1000) && new Date(_now).getDate() == _currentValue.getDate()){
            return moment(value).format('MMMDo H:mm');
        }else if ( diff < (60 * 60 * 24 * 7 * 1000) ){
            return moment(value).format('MMMDo a h:mm');
        }else{
            return moment(value).format('YYYY年MMMD日 HH:mm');
        }
    }
});
</@compress>