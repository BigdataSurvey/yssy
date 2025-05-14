<@compress single_line=true>
/**分页工具类*/
LIVEAPP.service("pageUtil", function($window) {
    return {
        init: function(id, callback) {
            if(!id) return false;
            $('#' + id).bootpag({
                total: 0,
                maxVisible: 10,
                wrapClass: 'pagination pg-primary'
            }).on("page", function(event, num){
                callback && callback(num);
            });
            $('#' + id).find('a').addClass('page-link');
        },
        change: function(option) {
            if(!option || !option.id || !option.pageSize) return false;
            var totalPages = parseInt((option.totalCount + option.pageSize - 1) / option.pageSize);
            $('#' + option.id).bootpag({total: (totalPages == 0 ? 1 : totalPages), wrapClass: 'pagination pg-primary', page: option.page, maxVisible: 10});
            $('#' + option.id).find('a').addClass('page-link');
        }
    }
});
</@compress>