<script>
    <#include "js/inner-page.js">
    <#include "DrawExam.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">提现审核</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">微信提现管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">提现审核</a> </li>
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
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ng-class="{'active': ${menuId}.currentTab == 1}" ng-click="${menuId}.switchTab(1)"><i class="fa fa-user mr-2"></i> 个人提现审核</a> 
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
                        <div class="card-block tab-content">
                            <!-- 个人提现审核:begin -->
                            <div class="tab-pane" role="tabpanel" ng-class="{'active': ${menuId}.currentTab == 1}"> 
                                <div class="dt-responsive table-responsive">
                                    <table id="${menuId}_baseTable" class="table table-striped table-bordered nowrap base-table">
                                    </table>
                                </div>
                            </div>
                            <!-- 个人提现审核:end -->
                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>

    <!-- 提现审核 -->
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
<#--                            <div class="col-md-12">-->
<#--                                <div class="form-group">-->
<#--                                    <textarea rows="5" cols="5"  class="form-control" placeholder="拒绝原因，最多100个字符" maxlength="100" name="mark"></textarea>-->
<#--                                </div>-->
<#--                            </div>-->
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.examData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>

</div>

