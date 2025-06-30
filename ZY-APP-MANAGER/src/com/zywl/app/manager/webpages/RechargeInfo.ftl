<script>
    <#include "js/inner-page.js">
    <#include "RechargeInfo.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">云上书苑角色支付订单管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
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
                            <div class="card-header-left">
                                <ul class="nav nav-tabs md-tabs" role="tablist"> 
                                    <li class="nav-item">
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ><i class="fa fa-user mr-2"></i> 订单列表</a>
                                        <div class="slide"></div> 
                                    </li>
                                 </ul> 
                            </div>
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
                                                <span class="input-group-text">用户ID：</span>
                                            </div>
                                            <input type="number" class="form-control" placeholder="用户ID" name="userNo">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">用户昵称：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="用户昵称" name="userName">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">平台ID：</span>
                                            </div>
                                            <input type="number" class="form-control" placeholder="平台ID" name="userId">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">状态：</span>
                                            </div>
                                            <div class="w-s">
                                                <select class="form-control input-full select3" name="status">
                                                </select>
                                            </div>
                                        </div>
                                    </div>
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">开始日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-start-time" maxDate="${menuId}-end-time" name="startDate" ng-model="searchModel.startDate" autocomplete="off" placeholder="开始日期">-->
<#--                                        </div>-->
<#--                                    </div>-->
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">截止日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-end-time" minDate="${menuId}-start-time" name="endDate" ng-model="searchModel.endDate" autocomplete="off" placeholder="截止日期">-->
<#--                                        </div>-->
<#--                                    </div>-->

                                    <div class="col-md-12 text-center">
                                        <div class="form-group">
                                            <button type="button" class="btn btn-primary btn-search mr-2" ng-disabled="${menuId}.loading" ng-click="${menuId}.search()"><span class="btn-label"> <i class="fas fa-search"></i> </span> 查询 </button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                        <div class="card-block tab-content">
                            <!-- 充值记录:begin -->
                            <div class="dt-responsive table-responsive">
                                <table id="${menuId}_baseTable" class="table table-striped table-bordered nowrap base-table">
                                </table>
                            </div>
                            <!-- 充值记录:end -->
                        </div>
                    </div>

                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>

</div>

