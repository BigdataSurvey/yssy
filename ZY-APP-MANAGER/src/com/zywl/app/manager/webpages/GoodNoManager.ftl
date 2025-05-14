<script>
    <#include "js/inner-page.js">
    <#include "GoodNoManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">靓号管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">靓号管理</a> </li>
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
                            <h5>靓号管理</h5>
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
                                                <span class="input-group-text">靓号号码：</span>
                                            </div>
                                            <input type="text" class="form-control" placeholder="靓号号码" name="goodNo">
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

    <div class="modal fade" id="${menuId}_rowModal" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header no-bd">
                    <h5 class="modal-title">添加靓号：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                </div>
                <div class="modal-body">
                    <form role="form" autocomplete="off" id="${menuId}_formEdit">
                        <div class="row">
                            <div class="col-12">
                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项</h6>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">靓号号码：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="靓号号码" name="goodNo">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">价格：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="价格" name="price">
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">数量：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="数量" name="number">
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">显示：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="客户端是否显示" name="status">
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

