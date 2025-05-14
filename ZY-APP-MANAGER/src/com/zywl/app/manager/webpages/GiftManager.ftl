<script>
    <#include "js/inner-page.js">
    <#include "GiftManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">礼物管理</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">业务管理</a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">礼物管理</a> </li>
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
                            <h5>礼物管理</h5>
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
    
    <!-- 新增/修改礼物管理 -->
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
                                <h6 class="form-text text-danger ml-2 mb-3">红色框体为必填项</h6>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">名称：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="名称" name="name">
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
                                <div class="input-group input-group-danger">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">Icon：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="Icon" name="icon">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">svgaIcon：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="svgaIcon" name="svgaIcon">
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">可连击：</span>
                                    </div>
                                    <div class="d-flex align-items-center ml-3">
                                        <input type="checkbox" id="${menuId}_form_lianji">
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">开启跳蛋：</span>
                                    </div>
                                    <div class="d-flex align-items-center ml-3">
                                        <input type="checkbox" id="${menuId}_form_tiaodan">
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">跳蛋震动级别：</span>
                                    </div>
                                    <div class="w-s">
                                        <input type="text" class="form-control" placeholder="跳蛋震动级别(1-20)" name="tiaodanLevel">
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">跳蛋音乐：</span>
                                    </div>
                                    <div class="w-s">
                                        <select class="form-control input-full select4" name="audio" id="${menuId}_form_audio">
                                        </select>
                                    </div>
                                    <div class="input-group-append">
                                        <button class="btn btn-info" type="button" ng-click="${menuId}.palyAudio()"><span class="btn-label mr-1"> <i class="ti-control-play"></i> </span></button>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="input-group">
                                    <div class="input-group-prepend col-min-7-5">
                                        <span class="input-group-text">跳蛋震动时间(秒)：</span>
                                    </div>
                                    <input type="text" class="form-control" placeholder="跳蛋震动时间(秒)" name="tiaodanTime">
                                </div>
                            </div>
                            <div class="d-none" id="${menuId}_audio_div"></div>
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
    
    <!-- 查看SVGA图标 -->
    <div class="modal fade" id="${menuId}_rowModal_svga" tabindex="-1" role="dialog" aria-hidden="true" data-backdrop="static">
        <div class="modal-dialog modal-lg" role="document">
             <div class="modal-content">
                 <div class="modal-header no-bd">
                     <h5 class="modal-title">{{${menuId}.currentTpl._title}}<span class="text-danger">{{${menuId}.currentTpl.name}}</span></h5>
                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span> </button>
                 </div>
                 <div class="modal-body text-center">
                    <div id="${menuId}_svga" class="ml-auto mr-auto" loops="0" clearsAfterStop="true" style="width:600px;height:500px;overflow: hidden;"></div>
                    
                    <div class="loader-block" id="${menuId}_loader-block" style="height:500px;">
                        <svg id="loader2" viewbox="0 0 100 100"> 
                            <circle id="circle-loader2" cx="50" cy="50" r="45"></circle> 
                        </svg> 
                    </div>
                 </div>
                 <div class="modal-footer no-bd">
                     <button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i class="fas fa-times"></i> </span> 关闭</button>
                 </div>
             </div>
         </div>
    </div>
    
</div>

