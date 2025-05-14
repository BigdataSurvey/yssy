<script>
    <#include "js/inner-page.js">
    <#include "JSXMManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">仙门管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">仙门管理</a> </li>
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
                            <h5>仙门管理</h5>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="fa fa-plus" ng-click="${menuId}.createRow()"></i></li> 
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
                                                <span class="input-group-text">仙门ID：</span>
                                            </div>
                                            <input type="number" class="form-control" placeholder="仙门ID" name="id">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">仙门名称：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="仙门名称" name="immortalGateName">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">掌门ID：</span>
                                            </div>
                                            <input type="number" class="form-control" placeholder="掌门ID" name="userId">
                                        </div>
                                    </div>
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">状态：</span>-->
<#--                                            </div>-->
<#--                                            <div class="w-s">-->
<#--                                                <select class="form-control input-full select3" name="immortalGateStatus">-->
<#--                                                </select>-->
<#--                                            </div>-->
<#--                                        </div>-->
<#--                                    </div>-->
                                    <div class="col-md-12 text-center">
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

