<@compress single_line=true>
/**sweetalert的工具类*/
LIVEAPP.service("alertUtil", function($window) {
    function _sweetAlert(title, html, icon, confirm, always, onlyConfirm) {
        Swal.fire({
            title: title || '',
            html: html || '',
            type: icon,
            showCancelButton: !onlyConfirm,
            allowOutsideClick: false,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: '<span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 确定',
            cancelButtonText: '<span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 取消',
            preConfirm: function() {
                return new Promise(function(resolve, reject) {
                    confirm && confirm();
                });
            },
            onClose: function(){
                always && always();
            }
        });
    };
    function _htmlContent(html){
        var span = document.createElement("span");
        span.innerHTML = html;
        return span;
    }
    return {
        alert: function(option) {
            /*icon:warning,error,success,info*/
            _sweetAlert(option.title, option.html, option.icon, option.confirm, option.always, option.onlyConfirm);
        },
        htmlContent: function(html) {
            return _htmlContent(html);
        }
    }
});
</@compress>