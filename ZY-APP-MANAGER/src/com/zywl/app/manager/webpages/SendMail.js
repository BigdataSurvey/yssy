<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, $stateParams, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.currentTab = 1;
        me.items = $rootScope.Items;
        $scope.itemList = [{}];
        $scope.mailData = {mailType:'',userIdArr:'',title:'',context:'', items:[{itemId:'', itemNum:''}]};

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
                    if(itemId == null || isNaN(itemId)) {
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
        function initPage() {
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>