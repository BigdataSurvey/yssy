<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, $stateParams, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var dataMap_family = {};
        let typeDic = {'1':'私人邮件', '2':'系统邮件'};
        var statusDic = {'1':'金币', '2': '灵石', '3': '推送至微信', '3': '提现成功', '4': '提现失败'};
        var statusColorDic = {'0': 'text-gray', '1': 'text-success', '2': 'text-danger', '3': 'text-success', '4': 'text-danger'};
        var baseTable = null;
        var baseTable_family = null;
        me.currentTab = 1;
        me.items = [];
        $scope.itemList = [{}];
        $scope.mailData = {mailType:'',userIdArr:'',title:'',context:'', items:[{itemId:'', itemNum:''}]};
        me.switchTab = function(index){
            me.currentTab = index || 2;
            if(index == 1 && Object.keys(dataMap).length == 0){
                me.loadData();
            } else if (index == 2){
                /*$('#${menuId}_sendMail').show() ;*/
            }
        };
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "idx", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "id", title: 'ID', render: datatableUtil.renderNormal},
                    {data: "fromUserId", title: '发送ID', render: datatableUtil.renderNormal},
                    {data: "fromUserName", title: '发送者昵称', render: function(data, type, row, setting){
                            if(parseInt(row.fromUserId) < 0) {
                                return "系统"
                            }
                            return data;
                        }},
                    {data: "toUserId", title: '接收ID', render: datatableUtil.renderNormal},
                    {data: "toUserName", title: '接收者昵称', render: datatableUtil.renderNormal},
                    {data: "title", title: '标题', render: datatableUtil.renderNormal},
                    {data: "context", title: '邮件内容', render: datatableUtil.renderNormal},
                    {data: "type", title: '类型', render: function(data, type, row, setting){
                            return '<span ' + (typeDic[data] || '-') + '</span>';
                        }},
                    /*{data: "status", title: '状态', render: function(data, type, row, setting){
                        return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                    }},*/
                    {data: "attachmentsDetails", title: '详情', render: datatableUtil.renderNormal},
                    {data: "isRead", title: '是否读取', render: function(data, type, row, setting){
                            let str = data == "1" ? "已读" : "未读";
                            return '<span class="">' + str + '</span>';
                        }},
                    {data: "sendTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "expirationTime", title: '过期时间', render: datatableUtil.renderTime}
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
                type: 1
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                if(_name == 'status') {
                    if(_value && _value.length > 0) {
                        _value = _value.join(',');
                    } else {
                        _value = '-1';
                    }
                }
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('021001', searchData, function(command){
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
        }
        /*刷新*/
        me.loadData = function() {
            if(me.currentTab == 1) {
                baseTable.ajax.reload(null, false);
            } else if(me.currentTab == 2){
                baseTable_family.ajax.reload();
            }
        };
        function loadItems () {
            websocketService.request('021003', {}, function(command){
                if(command.success){
                    me.items = command.data || {};
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
            });
        };
        me.addRow = function () {
            $scope.itemList.push({});
        };
        me.subRow = function (index) {
            if($scope.itemList.length > 1) {
                $scope.itemList.splice(index, 1);
            }
        };
        me.sendMail = function (params) {
            let mailType = params.mailType;
            let userIdArr = params.userIdArr;
            console.log(mailType);
            let userArr = [];
            if(mailType == "1") {
                if (userIdArr == undefined || userIdArr == '') {
                    toastr.error('用户ID错误');
                    return;
                }

                userArr = userIdArr.split(' ');
                for (let i=0; i<userArr.length; i++) {
                    if( isNaN(parseInt(userArr[i])) ) {
                        toastr.error('请检查用户ID');
                        return;
                    }
                }
            }

            let title = params.title;
            if(title == undefined || title == '') {
                toastr.error('邮件标题错误');
                return;
            }
            let context = params.context;
            if(context == undefined || context == '') {
                toastr.error('邮件内容错误');
                return;
            }

            let items = params.items;
            let itemArr = [];
            if(items) {
                for (let k in items) {
                    let item = items[k];
                    let itemId = parseInt(item.itemId.itemId);
                    if(isNaN(itemId)) {
                        toastr.error('道具ID错误');
                        return;
                    }
                    let itemNum = parseInt(item.itemNum);
                    if(isNaN(itemNum)) {
                        toastr.error('道具数量错误');
                        return;
                    };
                    itemArr.push({
                        itemId: itemId,
                        itemNum: itemNum
                    });
                }
            }

            let data = {
                'mailType': mailType,
                'userArr': userArr,
                'title': title,
                'context': context,
                'itemArr': itemArr
            };

            websocketService.request('021002', data, function(command){
                if(command.success){
                    toastr.success("发送邮件成功！");
                } else {
                    toastr.error(command.message || '发送邮件异常', '系统提示');
                }
            });
        };
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="itemType"]').html(strHtml).trigger('change');
        };
        function initTemplate() {
            var template=$("#itemTemplate");
            var mobileDialogElement = $compile(template)($scope);
            $scope.itemList.push(mobileDialogElement);
        }
        function initPage() {
            loadStatusDic();
            /*日期查询*/
            datatableUtil.datepicker(true);
            var endDate = new Date();
            $('#${menuId}_content input[id$="-create-end-time"]').datepicker('setDate', endDate);
            endDate.setMonth(endDate.getMonth() - 1);
            $('#${menuId}_content input[id$="-create-start-time"]').datepicker('setDate', endDate);
            initTalbe();
            loadItems();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>