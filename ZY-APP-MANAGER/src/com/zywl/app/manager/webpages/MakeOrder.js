<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, $stateParams, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.currentTab = 1;
        me.items = $rootScope.Items;
        $scope.cardList = [{}];
        $scope.orderData = [{orderType:'', itemId:'', itemNum:'', itemPrice:''}];

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
            $scope.cardList.push({});
        };
        me.subRow = function (index) {
            if($scope.cardList.length > 1) {
                $scope.cardList.splice(index, 1);
            }
        };
        me.makeOrder = function (params) {
            console.log(params);
            let itemArr = [];
            for (let k in params) {
                let item = params[k];
                /* let orderType = parseInt(item.orderType);*/

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

                let itemPrice = parseFloat(item.itemPrice);
                if(isNaN(itemPrice)) {
                    toastr.error('价格错误');
                    return;
                };
                itemArr.push({
                    orderType: 0,
                    itemId: itemId,
                    itemNum: itemNum,
                    itemPrice: itemPrice
                });
            };

            let data = {orderList:itemArr};
            websocketService.request('021081', data, function(command){
                if(command.success){
                    toastr.success("生成订单成功！");
                } else {
                    toastr.error(command.message || '生成订单异常', '系统提示');
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