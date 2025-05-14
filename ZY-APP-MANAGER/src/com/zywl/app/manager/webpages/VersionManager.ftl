<script>
    <#include "js/inner-page.js">
    <#include "VersionManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">版本管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">系统管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">版本管理</a> </li>
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
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ng-class="{'active': ${menuId}.currentTab == 2}" ng-click="${menuId}.switchTab(2)"><i class="ti-android mr-2"></i> Android</a> 
                                        <div class="slide"></div> 
                                    </li>
                                    <li class="nav-item">
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab" ng-class="{'active': ${menuId}.currentTab == 1}" ng-click="${menuId}.switchTab(1)"><i class="ti-apple mr-2"></i> IOS</a> 
                                        <div class="slide"></div> 
                                    </li>
                                 </ul> 
                            </div>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="fa fa-plus" ng-click="${menuId}.createRow()"></i></li> 
                                    <li class="d-inline"><i class="fa fa-refresh reload-card" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"></i></li> 
                                </ul> 
                            </div>
                        </div>
                        <div class="card-block tab-content">
                            <!-- Android:begin -->
                            <div class="tab-pane" role="tabpanel" ng-class="{'active': ${menuId}.currentTab == 2}"> 
                                <div class="dt-responsive table-responsive">
                                    <table id="${menuId}_baseTable_android" class="table table-striped table-bordered nowrap base-table">
                                    </table>
                                </div>
                            </div>
                            <!-- Android:end -->
                            <!-- IOS:begin -->
                            <div class="tab-pane" role="tabpanel" ng-class="{'active': ${menuId}.currentTab == 1}"> 
                                <div class="dt-responsive table-responsive">
                                    <table id="${menuId}_baseTable_ios" class="table table-striped table-bordered nowrap base-table">
                                    </table>
                                </div>
                            </div>
                            <!-- IOS:end -->
                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
    
    <!-- 新增/修改版本管理 -->
    <div class="modal fade" id="${menuId}_rowModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title" ng-if="${menuId}.currentTpl">{{${menuId}.currentTpl._title}}<span class="text-danger">{{${menuId}.currentTpl.versionName}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body">
                    <form role="form" autocomplete="off" enctype="multipart/form-data" id="${menuId}_formEdit">
                        <div class="row">
                            <div class="col-12">
                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项</h6>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">版本id：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="版本id" name="id">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">版本名称：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="版本名称" name="versionName">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">版本号：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="版本号" name="versionNo">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group" ng-class="{'input-group-danger': ${menuId}.currentTpl.$type =='add'}">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">选择文件：</span>
                                    </div>
                                    <div class="d-flex align-items-center ml-3">
                                        <input class="form-control" accept=".apk,.ipa" type="file" name="file" id="${menuId}_uploadfile">
                                    </div> 
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="form-group">
                                    <label class="text-danger">版本描述：</label>
                                    <textarea rows="5" cols="5"  class="form-control" placeholder="版本描述，最多200个字符" maxlength="200" name="description"></textarea>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                            </div>
                            <input type="hidden" name="type"/>
                        </div>
                     </form>
                     <div class="preloader3 loader-block d-none form-loading" id="${menuId}_form_loading"> 
                        <div class="circ1"></div> 
                        <div class="circ2"></div> 
                        <div class="circ3"></div> 
                        <div class="circ4"></div> 
                     </div>
                 </div>
                 <div class="modal-footer no-bd">
                     <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    
    <!-- 查看二维码 -->
    <div class="modal fade" id="${menuId}_rowModal_qrCode" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">查看二维码：<span class="text-danger">{{${menuId}.currentTpl.versionName}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body text-center">
                    <div id="${menuId}_qrCode"></div>
                 </div>
                 <div class="modal-footer no-bd">
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
</div>

