<script>
    <#include "js/inner-page.js">
    <#include "FamilyManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">家族管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">家族管理</a> </li>
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
                    <!--家族列表:begin -->
                    <div class="card" id="${menuId}_list">
                        <div class="card-header">
                            <h5>家族管理</h5>
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
                                                <span class="input-group-text">开始日期：</span>
                                            </div>
                                            <input type="text" class="form-control datepicker" id="${menuId}-start-time" maxDate="${menuId}-end-time" name="startDate" autocomplete="off" placeholder="开始日期">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">截止日期：</span>
                                            </div>
                                            <input type="text" class="form-control datepicker" id="${menuId}-end-time" minDate="${menuId}-start-time" name="endDate" autocomplete="off" placeholder="截止日期">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">名称：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="名称" name="name">
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
                                    <div class="col-md-4 text-left text-md-left">
                                        <div class="form-group">
                                            <button type="button" class="btn btn-primary btn-search mr-2" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"><span class="btn-label"> <i class="fas fa-search"></i> </span> 查询 </button>
                                        </div>
                                    </div>
                                </div>
                             </form>
                        </div>
                        <div class="card-block">
                            <div class="dt-responsive table-responsive scroll-table-div">
                                <table id="${menuId}_baseTable" class="table table-striped table-bordered nowrap base-table">
                                </table>
                            </div>
                        </div>
                    </div>
                    <!--家族列表:end -->
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
    <!-- 修改家族信息 -->
    <div class="modal fade" id="${menuId}_rowModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">{{${menuId}.currentTpl._title}}<span class="text-danger">{{${menuId}.currentTpl.name}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formEdit">
                        <div class="row">
                            <div class="col-12">
                                <h6 class="form-text text-danger ml-2 mb-1">红色框体为必填项</h6>
                                <h6 class="form-text text-danger ml-2 mb-3">家族禁用后，此家族下所有主播将无法提交提现申请</h6>
                            </div>
                            <div class="col-12">
                                <h5 class="form-text ml-2 mb-3 f-18">基本信息</h5>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">家族名称：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="家族名称" name="name">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">是否启用：</span>
                                    </div>
                                    <div class="d-flex align-items-center ml-3">
                                        <input type="checkbox" id="${menuId}_form_status">
                                    </div>
                                </div>
                            </div>
                            <div class="col-12">
                                <h5 class="form-text ml-2 mb-3 f-18">分成信息</h5>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">礼物分成比例：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="礼物分成比例" name="familyGiftFee">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">门票分成比例：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="门票分成比例" name="familyTicketFee">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">金币分成比例：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="金币分成比例" name="familyChargeFee">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">守护分成比例：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="守护分成比例" name="familyGuardFee">
                                </div>
                            </div>
                            <div class="col-12">
                                <h5 class="form-text ml-2 mb-3 f-18">提现信息</h5>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">收款类型：</span>
                                    </div>
                                    <div class="w-s">
                                        <select class="form-control input-full select4" name="payeeType">
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">提现收款人：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="提现收款人姓名" name="payeeName">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">提现账户：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="提现账户" name="payeeAccount">
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

