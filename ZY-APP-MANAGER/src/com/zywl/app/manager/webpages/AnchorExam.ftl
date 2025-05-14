<script>
    <#include "js/inner-page.js">
    <#include "AnchorExam.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">渠道审核</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">主播审核</a> </li>
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
                            <h5>渠道审核</h5>
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
                                        <div class="input-group input-group-primary">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">联系电话：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="联系电话" name="phone">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">备注：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="备注" name="mark">
                                        </div>
                                    </div>
                                    <div class="col-md-4 text-left">
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
    
    <!-- 审核通过:begin -->
    <div class="modal fade" id="${menuId}_passModal" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">主播审核通过：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formPass">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="input-group input-group-danger mb-3">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">频道：</span>
                                    </div>
                                    <div class="w-s">
                                        <select class="form-control input-full select4" name="playerLiveType">
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group mb-3">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">所属家族：</span>
                                    </div>
                                    <div class="w-s">
                                        <select class="form-control input-full select2" name="familyId">
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.passData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 通过</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    <!-- 审核通过:end -->
    
    <!-- 审核拒绝:begin -->
    <div class="modal fade" id="${menuId}_examModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">拒绝：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formRefuse">
                        <div class="row">
                            <div class="col-md-12">
                                <div class="form-group">
                                    <textarea rows="5" cols="5"  class="form-control" placeholder="拒绝原因，最多100个字符" maxlength="100" name="reason"></textarea>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.examData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 拒绝</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    <!-- 审核拒绝:end -->
</div>

