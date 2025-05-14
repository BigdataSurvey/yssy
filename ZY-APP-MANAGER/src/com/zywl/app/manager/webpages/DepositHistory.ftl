<script>
    <#include "js/inner-page.js">
    <#include "DepositHistory.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">充值记录</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">充值记录</a> </li>
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
                            <h5>充值记录 <small class="ml-1 text-gray" ng-if="${menuId}.totalAmount">合计金额：{{${menuId}.totalAmount}}</small></h5>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="fa fa-refresh reload-card" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"></i></li> 
                                </ul> 
                            </div>
                        </div>
                        <div class="card-block">
                             <form role="form" autocomplete="off" id="${menuId}_search" class="mt-3">
                                <div class="row">
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">创建开始日期：</span>
                                            </div>
                                            <input type="text" class="form-control datepicker" id="${menuId}-start-time" maxDate="${menuId}-end-time" name="startDate" autocomplete="off" placeholder="创建开始日期">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">创建截止日期：</span>
                                            </div>
                                            <input type="text" class="form-control datepicker" id="${menuId}-end-time" minDate="${menuId}-start-time" name="endDate" autocomplete="off" placeholder="创建截止日期">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">联系电话：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="联系电话" name="userPhone">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">支付开始日期：</span>
                                            </div>
                                            <input type="text" class="form-control datepicker" id="${menuId}-pay-start-time" maxDate="${menuId}-pay-end-time" name="payStartDate" autocomplete="off" placeholder="支付开始日期">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">支付截止日期：</span>
                                            </div>
                                            <input type="text" class="form-control datepicker" id="${menuId}-pay-end-time" minDate="${menuId}-pay-start-time" name="payEndDate" autocomplete="off" placeholder="支付截止日期">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">订单号：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="订单号" name="orderNo">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">状态：</span>
                                            </div>
                                            <div class="w-s">
                                                <select class="form-control input-full select3" name="status" multiple="multiple">
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">通道列表：</span>
                                            </div>
                                            <div class="w-s">
                                                <select class="form-control input-full select4" name="channelId">
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-4 text-center text-md-left">
                                        <div class="form-group">
                                            <button type="button" class="btn btn-primary btn-search mr-2" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"><span class="btn-label"> <i class="fas fa-search"></i> </span> 查询 </button>
                                        </div>
                                    </div>
                                </div>
                             </form>
                        </div>
                        <div class="card-block">
                            <div class="dt-responsive table-responsive">
                                <table id="${menuId}_baseTable" class="table table-striped table-bordered nowrap base-table">
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>

</div>

