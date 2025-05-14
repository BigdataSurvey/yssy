<@compress single_line=true>
/**金额处理工具类*/
LIVEAPP.service("amountUtil", function($window) {
    /* 金额处理 */
    function _formatCurrency(number, decimals, spliter, dot) {
        number = (number + '').replace(/[^0-9+\-Ee.]/g, '');
        var n = !isFinite(+number) ? 0 : +number;/*判断是否是无穷数，不是无穷数n赋值为0*/
        var prec = !isFinite(+decimals) ? 0: Math.abs(decimals);
        var sep = (typeof spliter === 'undefined') ? ',': spliter;
        var dec = (typeof dot === 'undefined') ? '.': dot;
        var s = '';
        var toFixedFix = function(n, prec) {
            var k = Math.pow(10, prec);
            return '' + Math.round(n * k) / k;
        };
        s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
        if (s[0].length > 3) {
            s[0] = s[0].replace(/\B(?=(?:\d{3})+(?!\d))/g, sep);
        }
        if ((s[1] || '').length < prec) {
            s[1] = s[1] || '';
            s[1] += new Array(prec - s[1].length + 1).join('0');
        }
        return s.join(dec);
    };
    return {
        positive: function(data){
            /*正数,2位小数*/
            return /^(([0-9]\d*)(\.\d{1,2})?)$|(0\.0?([0-9]\d?))$/.test(data);
        },
        verification: function(data) {
            /*正负数*/
            return  /(^([-]?)[1-9]([0-9]+)?(\.[0-9]{1,2})?$)|(^([-]?)(0){1}$)|(^([-]?)[0-9]\.[0-9]([0-9])?$)/.test(data); 
        },
        testInt: function(data) {
            /*正整数*/
            return /^\+?[1-9][0-9]*$/.test(data);
        },
        formatCurrency: function(number, decimals, spliter, dot) {
            return _formatCurrency(number, decimals, spliter, dot);
        }
    }
});
</@compress>