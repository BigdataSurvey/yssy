<script>
    <#include "js/inner-page.js">
    <#include "SendMail.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">发送邮件</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">发送邮件</a> </li>
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
                                        <a class="nav-link ml-3 mr-3 text-nowrap h6 header-tab-title" data-toggle="tab" role="tab"><i class="fa fa-user mr-2"></i> 发送邮件</a>
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

                        <div class="card-block">
                            <form role="form" ng-submit="${menuId}.sendMail(mailData)" autocomplete="off" id="${menuId}_sendMail" class="mt-3">
                                <div class="row">
                                    <div class="col-md-12">
                                        <div class="form-group">
                                            <textarea rows="1" cols="5"  class="form-control" placeholder="用户ID，多个用户使用一个空格字符隔开" maxlength="20" name="userIdArr" ng-model="mailData.userIdArr"></textarea>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <div class="form-group">
                                            <textarea rows="1" cols="5"  class="form-control" placeholder="邮件标题，最多20个字符" maxlength="20" name="title" ng-model="mailData.title"></textarea>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <div class="form-group">
                                            <textarea rows="5" cols="5"  class="form-control" placeholder="邮件内容，最多100个字符" maxlength="100" name="context" ng-model="mailData.context"></textarea>
                                        </div>
                                    </div>
                                    <div class="col-md-3">
                                        <div class="input-group input-group-primary mt-3">
                                            <div class="input-group-prepend col-min-7-2">
                                                <span class="input-group-text">邮件类型：</span>
                                            </div>
                                            <div class="w-s">
                                                <select class="form-control select3" name="type" ng-model="mailData.mailType" >
                                                    <option value="">-- 请选择 --</option>
                                                    <option value="1">私人邮件</option>
                                                    <option value="2">公共邮件</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row col-md-12" id="groupItemList">
                                        <div class="card col-md-3" ng-repeat="i in itemList" id="itemTemplate" >
                                            <ul class="button-list">
                                                <li class="d-inline  float-right px-2"><i class="fa fa-plus" ng-click="${menuId}.addRow()"></i></li>
                                                <li class="d-inline  float-right px-2"><i class="fa fa-minus" ng-click="${menuId}.subRow($index)"></i></li>
                                            </ul>
                                            <div class="input-group input-group-primary mt-3">
                                                <div class="input-group-prepend col-min-7-2">
                                                    <span class="input-group-text">道具：</span>
                                                </div>
                                                <div class="w-s">
                                                    <select class="form-control select3" name="itemId" ng-model="mailData.items[$index].itemId" ng-options="item.itemName for item in ${menuId}.items">
                                                        <option value="">-- 请选择 --</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <div class="input-group input-group-primary mb-3">
                                                <div class="input-group-prepend col-min-7-2">
                                                    <span class="input-group-text">数量：</span>
                                                </div>
                                                <input type="text" class="form-control" placeholder="-- 数量 --" name="itemNum" ng-model="mailData.items[$index].itemNum">
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-12">
                                        <div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">{{${menuId}.currentTpl._errorMsg}}</div>
                                    </div>
                                    <div class="col-md-12 text-center">
                                        <div class="form-group">
                                            <button class="btn btn-success" type="submit" ng-disabled="${menuId}.saving"><span class="btn-label mr-1"> <i class="fa fa-check"></i> </span> 发送</button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>

</div>

