<script>
    <#include "js/inner-page.js">
    <#include "MakeOrder.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">交易行生成订单</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">交易行生成订单</a> </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <!-- Page-header end -->
    <div class="pcoded-inner-content">
        <!-- Main-body start -->
        <div class="main-body">
            <div class="page-wrapper">
                <!-- Page-body start -->
                <div class="page-body">
                    <div class="card">
                        <div class="card-header">
                            <div class="card-header-left">
                            </div>
                        </div>

                        <div class="card-block">
                            <form role="form" ng-submit="${menuId}.makeOrder(orderData)" autocomplete="off" id="${menuId}makeOrder" class="mt-3">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="form-group">
                                            <div class="row col-md-12" id="groupItemList">
                                                <div class="card col-md-4" ng-repeat="i in cardList" id="itemTemplate" >
                                                    <ul class="button-list">
                                                        <li class="d-inline  float-right px-2"><i class="fa fa-plus" ng-click="${menuId}.addRow()"></i></li>
                                                        <li class="d-inline  float-right px-2"><i class="fa fa-minus" ng-click="${menuId}.subRow($index)"></i></li>
                                                    </ul>
                                                    <div class="input-group input-group-primary mt-3">
                                                        <div class="input-group-prepend col-min-7-2">
                                                            <span class="input-group-text">订单类型：</span>
                                                        </div>
                                                        <div class="w-s">
                                                            <select class="form-control select3" name="type" ng-model="orderData[$index].orderType" >
                                                                <option value="">-- 请选择 --</option>
                                                                <option value="0">出售</option>
                                                                <option value="1">求购</option>
                                                            </select>
                                                        </div>
                                                    </div>
                                                    <div class="input-group input-group-primary mt-3">
                                                        <div class="input-group-prepend col-min-7-2">
                                                            <span class="input-group-text">道具：</span>
                                                        </div>
                                                        <div class="w-s">
                                                            <select class="form-control select3" name="itemId" ng-model="orderData[$index].itemId" ng-options="item.itemName for item in ${menuId}.items">
                                                                <option value="">-- 请选择 --</option>
                                                            </select>
                                                        </div>
                                                    </div>
                                                    <div class="input-group input-group-primary mb-3">
                                                        <div class="input-group-prepend col-min-7-2">
                                                            <span class="input-group-text">数量：</span>
                                                        </div>
                                                        <input type="text" class="form-control" placeholder="-- 数量 --" name="itemNum" ng-model="orderData[$index].itemNum">
                                                    </div>
                                                    <div class="input-group input-group-primary mb-3">
                                                        <div class="input-group-prepend col-min-7-2">
                                                            <span class="input-group-text">价格：</span>
                                                        </div>
                                                        <input type="text" class="form-control" placeholder="-- 价格 --" name="itemPrice" ng-model="orderData[$index].itemPrice">
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                                    </div>
                                    <div class="col-md-12 text-center">
                                        <div class="form-group">
                                            <button class="btn btn-success" type="submit" ng-disabled="${menuId}.saving"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 上架</button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>

</div>

