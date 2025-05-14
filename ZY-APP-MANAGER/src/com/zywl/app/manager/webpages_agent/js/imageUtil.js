<@compress single_line=true>
/**图片处理工具类*/
LIVEAPP.service("imageUtil", function($window) {
    var _maxFileSize = 5 * 1024 * 1024;
    var _maxWidth = 2000, _maxHeight = 2000;
    /*压缩*/
    function _compression(command, file, maxWidth, maxHeight, callback) {
        var canvas = document.createElement('canvas');
        var context = canvas.getContext('2d');
        var reader = new FileReader(), img = new Image();
        reader.readAsDataURL(file);
        reader.onload = function(evt){
            if(evt.target.readyState == FileReader.DONE) {
                img.src = evt.target.result;
            }
        };
        img.onload = function () {
            var originWidth = this.width;
            var originHeight = this.height;
            var targetWidth = originWidth, targetHeight = originHeight;
            /*图片尺寸超过限制*/ 
            if (originWidth > maxWidth || originHeight > maxHeight) {
                if (originWidth / originHeight > maxWidth / maxHeight) {
                    targetWidth = maxWidth;
                    targetHeight = Math.round(maxWidth * (originHeight / originWidth));
                } else {
                    targetHeight = maxHeight;
                    targetWidth = Math.round(maxHeight * (originWidth / originHeight));
                }
            }
            canvas.width = targetWidth;
            canvas.height = targetHeight;
            context.clearRect(0, 0, targetWidth, targetHeight);
            context.drawImage(img, 0, 0, targetWidth, targetHeight);
            /*回调*/
            command.success = true;
            command.data = {content: canvas.toDataURL(file.type || 'image/png'), name: file.name, size: file.size};
            callback(command);
        };
    };
    /*原图*/
    function _getOriginal(command, file, callback){
        var reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = function(evt){
            if(evt.target.readyState == FileReader.DONE) {
                /*回调*/
                command.success = true;
                command.data = {content: evt.target.result, name: file.name, size: file.size};
                callback && callback(command);
            }
        };
    };
    /*验证*/
    function _verification(option){
        var _error = null;
        if(option.file.size > _maxFileSize){
            _error = '您当前选择的图片过大，请勿超过5M';
        } else if(!option.file.type.match('image.*')) {
            _error = '请选择一个图片';
        }
        if(option.maxWidth && option.maxHeight) {
            if(!/^\+?[1-9][0-9]*$/.test(option.maxWidth) || !/^\+?[1-9][0-9]*$/.test(option.maxHeight)) {
                _error = '图片缩放宽高不合法';
            }
        }
        return _error; 
    };
    /*裁剪*/
    function _scale(command, file, scaleWidth, scaleHeight, callback) {
        var canvas = document.createElement('canvas');
        var context = canvas.getContext('2d');
        var reader = new FileReader(), img = new Image();
        reader.readAsDataURL(file);
        reader.onload = function(evt){
            if(evt.target.readyState == FileReader.DONE) {
                img.src = evt.target.result;
            }
        };
        img.onload = function () {
            var originWidth = this.width;
            var originHeight = this.height;
            var targetWidth = originWidth, targetHeight = originHeight;
            var dw = 0, dh = 0;/*宽高比*/
            /*图片尺寸超过限制*/
            if (originWidth / originHeight > scaleWidth / scaleHeight) {
                targetHeight = originHeight;
                targetWidth = Math.round((originHeight / scaleHeight) * scaleWidth);
            } else {
                targetWidth = originWidth;
                targetHeight = Math.round((originWidth / scaleWidth) * scaleHeight);
            }
            dw = targetWidth / originWidth;
            dh = targetHeight / originHeight;
            canvas.width = targetWidth;
            canvas.height = targetHeight;
            context.clearRect(0, 0, targetWidth, targetHeight);
            if (dw > dh) {
                context.drawImage(img, 0, (originHeight - targetHeight/dw)/2, originWidth, targetHeight/dw, 0, 0, targetWidth, targetHeight);
            } else {
                context.drawImage(img, (originWidth - targetWidth/dh)/2, 0, targetWidth/dh, targetHeight, 0, 0, targetWidth, targetHeight);
            }
            /*回调*/
            command.success = true;
            command.data = {content: canvas.toDataURL(file.type || 'image/png'), name: file.name, size: file.size};
            callback(command);
        };
    };
    return {
        getImg: function(option){
            var command = {success: false};
            command.message = _verification(option);
            if(!option.original) {
                if(option.maxWidth && option.maxHeight) {
                    if(!/^\+?[1-9][0-9]*$/.test(option.maxWidth) || !/^\+?[1-9][0-9]*$/.test(option.maxHeight)) {
                        command.message = '图片缩放宽高不合法';
                    }
                } else {
                    option.maxWidth = _maxWidth;
                    option.maxHeight = _maxHeight;
                }
            }
            if(!command.message) {
                if(option.original) {
                    _getOriginal(command, option.file, option.callback);
                } else {
                    _compression(command, option.file, option.maxWidth, option.maxHeight, option.callback);
                }
            } else {
                option.callback && option.callback(command);
            }
        },
        scale: function(option){
            var command = {success: false};
            command.message = _verification(option);
            if(!command.message) {
                _scale(command, option.file, option.scaleWidth, option.scaleHeight, option.callback);
            } else {
                option.callback && option.callback(command);
            }
        }
    }
});
</@compress>