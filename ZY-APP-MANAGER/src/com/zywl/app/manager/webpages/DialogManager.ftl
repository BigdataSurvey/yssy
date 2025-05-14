<script>
    <#include "js/inner-page.js">
    <#include "DialogManager.js">
</script>
<style>#${menuId}_content .fr-element a{color: #007bff;}</style>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">弹窗公告管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">弹窗公告管理</a> </li>
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
                            <h5>弹窗公告管理</h5>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
                                    <li class="d-inline"><i class="fa fa-refresh reload-card" ng-disabled="${menuId}.loading" ng-click="${menuId}.loadData()"></i></li> 
                                </ul> 
                            </div>
                        </div>
                        <div class="card-block">
                            <form role="form" autocomplete="off" id="${menuId}_formEdit">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="form-group">
                                            <label>公告内容：</label>
                                            <textarea rows="5" cols="5"  class="form-control" placeholder="公告内容" id="${menuId}_context"></textarea>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <div class="alert alert-danger border-danger" ng-if="${menuId}._errorMsg">{{${menuId}._errorMsg}}</div>
                                    </div>
                                </div>
                             </form>
                        </div>
                        <div class="card-footer">
                            <button class="btn btn-success" type="button" ng-disabled="${menuId}.saving" ng-click="${menuId}.saveData()"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 提交</button>
                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
</div>

