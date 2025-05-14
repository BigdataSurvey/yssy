<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var statusDic = {'0': '拒绝', '1': '通过'};
        var statusColorDic = {'0': 'text-danger', '1': 'text-success', '2': 'text-primary'};
        var sexDic = {'1': '男','0': '女'};
        var baseTable = null;
        /*初始化表格*/
        function initTable() {
            /*加载数据*/
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                        return '<span class="' + statusColorDic[data] + '">' + statusDic[data] + '</span>';
                    }},
                    {data: "userNo", title: '蜜桃号', render: datatableUtil.renderNormal},
                    {data: "userPhone", title: '用户名', render: datatableUtil.renderNormal},
                    {data: "nickname", title: '昵称', render: datatableUtil.renderNormal},
                    {data: "sex", title: '性别', render: function(data, type, row, setting){
                        return sexDic[data] || '-';
                    }},
                    {data: "wx", title: '微信', render: datatableUtil.renderNormal},
                    {data: "qq", title: 'QQ', render: datatableUtil.renderNormal},
                    {data: "phone", title: '联系电话', render: datatableUtil.renderNormal},
                    {data: "facePhoto", title: '自拍照', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="自拍照" data-lightbox="'+ row.id +'"><img src="' + data + '" class="td-img"></a>';
                    }},
                    {data: "idImg", title: '手持证件照', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="手持证件照" data-lightbox="'+ row.id +'"><img src="' + data + '" class="td-img"></a>';
                    }},
                    {data: "mark", title: '备注', render: datatableUtil.renderNormal},
                    {data: "reason", title: '原因', render: datatableUtil.renderNormal},
                    {data: "createTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "adminName", title: '审核人', render: datatableUtil.renderNormal},
                    {data: "verifyTime", title: '审核时间', render: datatableUtil.renderTime}
                ],
                drawCallback: function(){
                    loadOpearte();
                },
                ajax: function (data, callback, settings) {
                    loadData(data, callback, settings);
                }
            });
        };
        /*请求数据*/
        function loadData(data, callback, settings) {
            if(me.loading) return;
            me.loading = true;
            var searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength,
                status: '0,1'
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                if(_name == 'status') {
                    if(_value && _value.length > 0) {
                        _value = _value.join(',');
                    } else {
                        _value = null;
                    }
                }
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('020001', searchData, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.list || [];
                    for(var i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i]; 
                    }
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: result.count,
                        recordsFiltered: result.count,
                        data: dataList
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        /*加载操作*/
        function loadOpearte() {
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="status"]').append(strHtml).trigger('change');
        };
        /*初始化页面*/
        function initPage() {
            loadStatusDic();
            datatableUtil.datepicker();
            initTable();
        }
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>