<script>
    <#include "js/inner-page.js">
    <#include "UserManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">用户管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">用户管理</a> </li>
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
                    <!--用户列表:begin -->
                    <div class="card" id="${menuId}_list">
                        <div class="card-header">
                            <h5>用户管理</h5>
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
                                                <span class="input-group-text">用户平台ID：</span>
                                            </div>
                                            <input type="number" class="form-control" placeholder="用户平台ID" name="userId">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <div class="input-group input-group-primary mb-3">
                                            <div class="input-group-prepend col-min-7-5">
                                                <span class="input-group-text">账号状态：</span>
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
<#--                                                <span class="input-group-text">在线状态：</span>-->
<#--                                            </div>-->
<#--                                            <div class="w-s">-->
<#--                                                <select class="form-control input-full select4" name="online">-->
<#--                                                </select>-->
<#--                                            </div>-->
<#--                                        </div>-->
<#--                                    </div>-->
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">开始日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-start-time" maxDate="${menuId}-end-time" name="startDate" autocomplete="off" placeholder="开始日期">-->
<#--                                        </div>-->
<#--                                    </div>-->
<#--                                    <div class="col-md-4">-->
<#--                                        <div class="input-group input-group-primary mb-3">-->
<#--                                            <div class="input-group-prepend col-min-7-5">-->
<#--                                                <span class="input-group-text">截止日期：</span>-->
<#--                                            </div>-->
<#--                                            <input type="text" class="form-control datepicker" id="${menuId}-end-time" minDate="${menuId}-start-time" name="endDate" autocomplete="off" placeholder="截止日期">-->
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
                            <div class="dt-responsive table-responsive scroll-table-div">
                                <table id="${menuId}_baseTable" class="table table-striped table-bordered nowrap base-table">
                                </table>
                            </div>
                        </div>
                    </div>
                    <!--用户列表:end -->
                    
                    <!--流水:begin -->
                    <div class="card d-none" id="${menuId}_log">
                        <div class="card-header">
                            <h5>资产流水：<span class="text-danger d-inline-block">{{${menuId}.currentTpl._title}}</span></h5>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="ti-arrow-left" ng-disabled="${menuId}.loading" ng-click="${menuId}.backList()"></i></li>
                                    <li class="d-inline"><i class="fa fa-refresh reload-card" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData_log()"></i></li> 
                                </ul> 
                            </div>
                        </div>
                        <!--资产概况:begin-->
                        <div class="card-block p-b-0">
                            <div class="row">
                                <div class="col-md-4"> 
                                    <div class="card"> 
                                        <div class="card-block"> 
                                            <div class="row align-items-center m-l-0"> 
                                                <div class="col-auto"> 
                                                    <i class="fas fa-stop-circle f-30 text-c-yellow"></i> 
                                                </div> 
                                                <div class="col-auto"> 
                                                    <h6 class="text-muted m-b-10">总金币</h6> 
                                                    <h2 class="m-b-0">{{${menuId}.currentTpl._amountTpl.availableAmount}}</h2> 
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4"> 
                                    <div class="card"> 
                                        <div class="card-block"> 
                                            <div class="row align-items-center m-l-0"> 
                                                <div class="col-auto"> 
                                                    <i class="fa fa-money f-30 text-c-purple"></i> 
                                                </div> 
                                                <div class="col-auto"> 
                                                    <h6 class="text-muted m-b-10">总消费</h6> 
                                                    <h2 class="m-b-0">{{${menuId}.currentTpl._amountTpl.totalSpendAmount}}</h2> 
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4"> 
                                    <div class="card"> 
                                        <div class="card-block"> 
                                            <div class="row align-items-center m-l-0"> 
                                                <div class="col-auto"> 
                                                    <i class="fas fa-stop-circle f-30 text-c-purple" style="color:#ff5b9d"></i> 
                                                </div> 
                                                <div class="col-auto"> 
                                                    <h6 class="text-muted m-b-10">总收益</h6> 
                                                    <h2 class="m-b-0">{{${menuId}.currentTpl._amountTpl.totalRealIncomeAmount}}</h2> 
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!--资产概况:end-->
                        <div class="card-block p-t-0 d-none">
                            <div class="dt-responsive table-responsive">
                                <table id="${menuId}_baseTable_log" class="table table-striped table-bordered nowrap base-table">
                                </table>
                            </div>
                        </div>
                    </div>
                    <!--流水:end -->
                    
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
    <!-- 修改用户信息 -->
    <div class="modal fade" id="${menuId}_rowModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">修改用户信息：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formEdit">
                        <div class="row">
<#--                             <div class="col-12">-->
<#--                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项</h6>-->
<#--                            </div>-->
<#--                            <div class="col-md-6">-->
<#--                                <div class="input-group input-group-danger">-->
<#--                                    <div class="input-group-prepend col-min-7-5">-->
<#--                                        <span class="input-group-text">昵称：</span>-->
<#--                                    </div>-->
<#--                                    <input type="text" class="form-control" placeholder="昵称" name="userName">-->
<#--                                </div>-->
<#--                            </div>-->
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">输入新的上级id</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="输入新的上级id" name="cno">
                                </div>
                            </div>

                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveEditData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>

    <!-- 修改用户信息为渠道 -->
    <div class="modal fade" id="${menuId}_rowModal_channel" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header no-bd">
                    <h5 class="modal-title">修改用户信息：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                </div>
                <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formEdit">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">确定设置为渠道吗？</span>
                                    </div>
                                    <#-- <input type="text" class="form-control" placeholder="cno" name="cno">-->
                                </div>
                            </div>

                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer no-bd">
                    <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveEditData_channel()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                    <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                </div>
            </div>
        </div>
    </div>


    <!-- 封号 -->
    <div class="modal fade" id="${menuId}_banModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header no-bd">
                    <h5 class="modal-title">标题：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                </div>
                <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_banEdit">
                        <div class="row">
                            <#--                             <div class="col-12">-->
                            <#--                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项</h6>-->
                            <#--                            </div>-->
                            <#--                            <div class="col-md-6">-->
                            <#--                                <div class="input-group input-group-danger">-->
                            <#--                                    <div class="input-group-prepend col-min-7-5">-->
                            <#--                                        <span class="input-group-text">昵称：</span>-->
                            <#--                                    </div>-->
                            <#--                                    <input type="text" class="form-control" placeholder="昵称" name="userName">-->
                            <#--                                </div>-->
                            <#--                            </div>-->
                            <div class="col-md-4">
                                <div class="input-group input-group-primary mb-3">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">操作：</span>
                                    </div>
                                    <div class="w-s">
                                        <select class="form-control input-full select3" name="operation">
                                            <option value="">---请选择</option>
                                            <option value="0">封禁登录</option>
                                            <option value="1">解禁登录</option>
                                            <option value="2">封禁功能</option>
                                            <option value="3">解禁功能</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="form-group">
                                    <textarea rows="5" cols="5"  class="form-control" id="${menuId}_form_ban_mark" placeholder="原因，最多100个字符" maxlength="100" name="mark"></textarea>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer no-bd">
                    <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.banLogin()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                    <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                </div>
            </div>
        </div>
    </div>
     <!-- 地图定位 -->
    <div class="modal fade" id="${menuId}_mapModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">查看用户定位：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <div id="${menuId}_map" style="width:100%; height: 500px;"></div> 
                 </div>
                 <div class="modal-footer no-bd">
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    <!-- 金币校正 -->
    <div class="modal fade" id="${menuId}_rowModal_amount" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">金币校正：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formEdit_amount">
                        <div class="row">
                            <div class="col-md-12">
                                <p>当前总金币：<span class="text-danger">{{${menuId}.currentTpl._amount}}</span></p>
                                <p>当前总收益：<span class="text-danger">{{${menuId}.currentTpl._income}}</span></p>
                            </div>
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label class="text-danger d-none">操作类型：</label>
                                    <div class="w-s">
                                        <select class="form-control input-full select4" name="type">
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label class="text-danger d-none">金币：</label>
                                    <input type="text" class="form-control" placeholder="金币" name="amount">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="form-group">
                                    <textarea rows="5" cols="5"  class="form-control" placeholder="备注，最多100个字符" maxlength="100" name="mark"></textarea>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                        </div>
                     </form>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveData_amount()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
</div>

