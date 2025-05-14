<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var switchFcMap = {};
        var switchReleaseMap = {};
        var statusDic = {};
        var statusColorDic = {};
        var sexDic = {'1': '男','0': '女'};
        var playerLiveTypeDic = {'1': '直播', '2': '热门', '4': '帅哥'};
        var baseTable = null;
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
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
                    {data: "createTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.passRow(\'' + row.id + '\')"><i class="ti-check"></i>通过</button>'
                            +'<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-click="${menuId}.refuseRow(\'' + row.id + '\')"><i class="ti-close"></i>拒绝</button>';
                        return _opearHtml;
                    }},
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
                status: 2
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
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
            $compile($('#${menuId}_baseTable'))($scope);
        };
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /*通过*/
        me.passRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.userPhone;
                me.currentTpl._errorMsg = null;
                $('#${menuId}_formPass').find('input[name],select[name],textarea[name]').val('').trigger('change');
                $('#${menuId}_passModal').modal('show');
            }
        };
        /*拒绝*/
        me.refuseRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.userPhone;
                me.currentTpl._errorMsg = null;
                $('#${menuId}_formRefuse').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formRefuse').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_examModal').modal('show');
            }
        };
        /*通过*/
        me.passData = function() {
            if(me.saving) return;
            var submitData = {id: me.currentTpl.id};
            $('#${menuId}_formPass').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(submitData.familyId == null) {
                submitData.familyId = '';
            }
            if(!submitData.playerLiveType) {
                toastr.warning('请选择频道', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request('020003', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_passModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        /*拒绝*/
        me.examData = function() {
            if(me.saving) return;
            var submitData = {id: me.currentTpl.id};
            $('#${menuId}_formRefuse').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.reason) {
                toastr.warning('请输入拒绝原因', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request('020002', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_examModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        /* 频道 */
        function loadPlayerLiveTypeDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in playerLiveTypeDic) {
                strHtml +='<option value="' + key + '">' + playerLiveTypeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="playerLiveType"]').html(strHtml).trigger('change');
        };
        /* 所属家族 */
        function loadFamilyDic() {
            websocketService.request('034004', null, function(command){
                var strHtml = '-';
                if(command.success){
                    var dataList = command.data || [];
                    var strHtml = '<option value="">--请选择--</option>';
                    for(var i = 0; i < dataList.length; i++) {
                        var record = dataList[i];
                        strHtml +='<option value="' + record.id + '">' + record.name + '</option>';
                    }
                    $('#${menuId}_content select[name="familyId"]').html(strHtml).trigger('change');
                } else {
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                $scope.$apply();
            });
        };
        /*初始化页面*/
        function initPage(){
            loadPlayerLiveTypeDic();
            loadFamilyDic();
            initTable();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>