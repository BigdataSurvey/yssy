<script>
    <#include "js/inner-page.js">
    <#include "CarouselManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">店长道具管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">系统管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">店长道具管理</a> </li>
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
                            <h5>店长道具管理</h5>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="fa fa-plus" ng-click="${menuId}.createRow()"></i></li> 
                                    <li class="d-inline"><i class="fa fa-refresh reload-card" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"></i></li> 
                                </ul> 
                            </div>
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
    
    <!-- 新增/修改轮播图管理 -->
    <div class="modal fade" id="${menuId}_rowModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title" ng-if="${menuId}.currentTpl">{{${menuId}.currentTpl._title}}<span class="text-danger">{{${menuId}.currentTpl.name}}</span></h5>
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
                                        <span class="input-group-text">标题：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="标题" name="title">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">描述：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="描述" name="context">
                                </div>
                            </div>

                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">价格：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="价格" name="price">
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">链接：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="链接" name="url">
                                </div>
                            </div>
                            <div class="col-md-12" ng-show="${menuId}.currentTpl.$type=='add'">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">位置：</span>
                                    </div>
                                    <div class="form-radio d-flex pl-3 align-items-center">
                                        <div class="radio radio-inline"> 
                                            <label class="mb-0"><input type="radio" name="location" checked data-value="1"> <i class="helper"></i>首位</label> 
                                        </div> 
                                        <div class="radio radio-inline"> 
                                            <label class="mb-0"><input type="radio" name="location" data-value="2"> <i class="helper"></i>末尾</label> 
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-12">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">图片：</span>
                                    </div>
                                    <input class="form-control" accept=".jpg,.jpeg,.png,.gif" type="file" id="${menuId}_form_photo">
                                </div>
                            </div>
                            <div class="col-md-12 text-center pt-2" style="position:relative;">
                                <div>
                                <img id="${menuId}_form_img" class="ml-auto mr-auto" style="max-width:100%;">
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

