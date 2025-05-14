<script>
    <#include "js/inner-page.js">
    <#include "DrawHistory.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">提现记录</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">微信提现管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">提现记录</a> </li>
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
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ng-class="{'active': ${menuId}.currentTab == 1}" ng-click="${menuId}.switchTab(1)"><i class="fa fa-user mr-2"></i> 个人提现记录</a> 
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
                        <div class="card-block" ng-show="${menuId}.currentTab == 1">
                             <form role="form" autocomplete="off" id="${menuId}_search" class="mt-3">
                                <div class="row">
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">申请开始日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-create-start-time" maxDate="${menuId}-create-end-time" name="startDate" autocomplete="off" placeholder="申请开始日期">-->
<#--                                        </div>-->
<#--                                    </div>-->
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">申请截止日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-create-end-time" minDate="${menuId}-create-start-time" name="endDate" autocomplete="off" placeholder="申请截止日期">-->
<#--                                        </div>-->
<#--                                    </div>-->
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">用户ID：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="用户ID" name="userNo">
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
                                            <input type="text" class="form-control" placeholder="平台ID" name="userId">
                                        </div>
                                    </div>
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">审核开始日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-exam-start-time" maxDate="${menuId}-exam-end-time" name="operateStartDate" autocomplete="off" placeholder="审核开始日期">-->
<#--                                        </div>-->
<#--                                    </div>-->
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">审核截止日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-exam-end-time" minDate="${menuId}-exam-start-time" name="operateEndDate" autocomplete="off" placeholder="审核截止日期">-->
<#--                                        </div>-->
<#--                                    </div>-->
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
                                    <div class="col-md-12 text-center">
                                        <div class="form-group">
                                            <button type="button" class="btn btn-primary btn-search mr-2" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"><span class="btn-label"> <i class="fas fa-search"></i> </span> 查询 </button>
                                        </div>
                                    </div>
                                </div>
                             </form>
                        </div>

                        <div class="card-block tab-content">
                            <!-- 个人提现记录:begin -->
                            <div class="tab-pane" role="tabpanel" ng-class="{'active': ${menuId}.currentTab == 1}"> 
                                <div class="dt-responsive table-responsive">
                                    <table id="${menuId}_baseTable" class="table table-striped table-bordered nowrap base-table">
                                    </table>
                                </div>
                            </div>
                            <!-- 个人提现记录:end -->

                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>

    <!-- 提现审核 -->
    <div class="modal fade" id="${menuId}_familyModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document" style="max-width:1200px;">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">家族提现详细：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <div class="dt-responsive table-responsive scroll-table-div">
                        <table id="${menuId}_baseTable_familyDetail" class="table table-striped table-bordered nowrap base-table">
                        </table>
                    </div>
                 </div>
                 <div class="modal-footer no-bd">
                    <button class="btn btn-success" type="button" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData_family()"><span class="btn-label mr-1"> <i class="fa fa-search"></i> </span> 刷新</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
</div>

