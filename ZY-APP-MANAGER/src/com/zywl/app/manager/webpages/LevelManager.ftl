<script>
    <#include "js/inner-page.js">
    <#include "LevelManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">等级管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">等级管理</a> </li>
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
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ng-class="{'active': ${menuId}.currentTab == 1}" ng-click="${menuId}.switchTab(1)"><i class="ti-user mr-2"></i> 观众等级</a> 
                                        <div class="slide"></div> 
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ng-class="{'active': ${menuId}.currentTab == 2}" ng-click="${menuId}.switchTab(2)"><i class="ti-control-play mr-2"></i> 主播等级</a> 
                                        <div class="slide"></div> 
                                    </li>
                                 </ul> 
                            </div>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="fa fa-first-order" title="初始化等级" ng-click="${menuId}.createRow()"></i></li> 
                                    <li class="d-inline"><i class="fa fa-refresh reload-card" title="刷新" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"></i></li> 
                                </ul> 
                            </div>
                        </div>
                        <div class="card-block tab-content">
                            <!-- 观众等级:begin -->
                            <div class="tab-pane" role="tabpanel" ng-class="{'active': ${menuId}.currentTab == 1}"> 
                                <div class="dt-responsive table-responsive">
                                    <table id="${menuId}_baseTable_customer" class="table table-striped table-bordered nowrap base-table">
                                    </table>
                                </div>
                            </div>
                            <!-- 观众等级:end -->
                            <!-- 主播等级:begin -->
                            <div class="tab-pane" role="tabpanel" ng-class="{'active': ${menuId}.currentTab == 2}"> 
                                <div class="dt-responsive table-responsive">
                                    <table id="${menuId}_baseTable_player" class="table table-striped table-bordered nowrap base-table">
                                    </table>
                                </div>
                            </div>
                            <!-- 主播等级:end -->
                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
    <!-- 初始化等级 -->
    <div class="modal fade" id="${menuId}_rowModal_init" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">初始化：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off">
                        <div class="row">
                            <div class="col-12">
                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项<br>系统上线后请勿执行此操作</h6>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">最大等级：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="最大等级" ng-model="${menuId}.currentTpl.maxLevel">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveData_init()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    
    <!-- 修改等级 -->
    <div class="modal fade" id="${menuId}_rowModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">编辑等级：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formEdit">
                        <div class="row">
                            <div class="col-12">
                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项</h6>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">名称：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="名称" name="alias">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">升级所需：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="升级所需" name="require">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">底色：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="底色" name="color">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">图标：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="图标" name="icon">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    
</div>

