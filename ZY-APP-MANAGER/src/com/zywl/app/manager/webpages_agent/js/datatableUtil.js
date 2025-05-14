<@compress single_line=true>
/**
 * datatable工具类
 */
LIVEAPP.service("datatableUtil", function($rootScope) {
    var _defaultOption = {
        searching: false,
        serverSide: true,
        ordering: false,
        rowId: 'id',
        autoWidth: false,
        language: {
            info: '当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录',
            emptyTable: '暂无数据',
            infoEmpty: '共0条记录',
            loadingRecords: '载入中...',
            paginate: {
                first: "首页",
                previous: "上页",
                next: "下页",
                last: "末页"
            },
            aria: {
                paginate: {
                    first: '首页',
                    previous: '上页',
                    next: '下页',
                    last: '末页'
                }
            }
        }
    };
    function _init(id, option) {
        var _option = $.extend(true, {}, _defaultOption, option);
        return $(id).DataTable(_option);
    };
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
    function _formatDate(date, fmt) {
        var o = { 
            "M+" : date.getMonth()+1,                 /*月份 */
            "d+" : date.getDate(),                    /*日 */
            "h+" : date.getHours(),                   /*小时 */
            "m+" : date.getMinutes(),                 /*分*/
            "s+" : date.getSeconds(),                 /*秒*/
            "q+" : Math.floor((date.getMonth()+3)/3), /*季度*/
            "S"  : date.getMilliseconds()             /*毫秒*/
        }; 
        if(/(y+)/.test(fmt)) {
                fmt=fmt.replace(RegExp.$1, (date.getFullYear()+"").substr(4 - RegExp.$1.length)); 
        }
         for(var k in o) {
            if(new RegExp("("+ k +")").test(fmt)){
                 fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
             }
         }
        return fmt; 
    };
    
    function _datepicker(otherDate) {
        if($('.datepicker')[0]) {
            /*日期初始化*/
            $('.datepicker').datepicker({
                autoclose: true,
                language: "zh-CN",
                format: 'yyyy-mm-dd'
            }).on('changeDate', function(e) {
                var _maxDateControl = $(e.target).attr('maxDate');
                var _minDateControl = $(e.target).attr('minDate');
                if(_maxDateControl) {
                    $('#' + _maxDateControl).datepicker('setStartDate', e.date);
                }
                if(_minDateControl) {
                    $('#' + _minDateControl).datepicker('setEndDate', e.date);
                }
            });
            if(!otherDate) {
                var endDate = new Date();
                $('input[id$="-start-time"]').datepicker('setDate', endDate);
                $('input[id$="-end-time"]').datepicker('setDate', endDate);
            }
        }
    };
    /*排序*/
    function _compare(prop) {
        return function (obj1, obj2) {
            var val1 = obj1[prop];
            var val2 = obj2[prop];
            if (val1 < val2) {
                return 1;
            } else if (val1 > val2) {
                return -1;
            } else {
                return 0;
            }
        } 
    };
    return {
        defaultOption: function() {
            return _defaultOption;
        },
        init: function(id, option) {
            return _init(id, option);
        },
        renderNormal: function(data, type, row) {
            return data || '-';
        },
        renderPrice: function(data, type, row){
            return _formatCurrency(data);
        },
        renderMoney: function(data, type, row){
            return _formatCurrency(data, 2);
        },
        renderTime: function(data, type, row){
            return data == null ? '-' : _formatDate(new Date(data), 'yyyy-MM-dd hh:mm:ss');
        },
        renderIcon: function(data, type, row){
            var _html = null;
            if(data) {
                _html = '<img class="img-30" src="' + $rootScope.resourceUrl + data + '" />';
            }
            return _html || '-';
        },
        setSwitchery: function (switchElement, checkedBool) {
            if((checkedBool && !switchElement.isChecked()) || (!checkedBool && switchElement.isChecked())) {
                switchElement.setPosition(true);
                switchElement.handleOnchange(true);
            }
        },
        datepicker: function(otherDate){
            _datepicker(otherDate);
        },
        compare: function(prop){
            return _compare(prop);
        },
        formatDate: function(data, fmt){
            return _formatDate(data, fmt);
        } 
    }
});
</@compress>