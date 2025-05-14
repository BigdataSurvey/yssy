<style>
#${menuId}_content .dataTables_empty{line-height:70px;}
</style>
<script>
    <#include "js/inner-page.js">
    <#include "Home.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">代理数据</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"><a href="javascript:;"> <i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">代理数据</a> </li>
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
                    <div class="row">
                        <div class="col-md-4">
                            <div class="card mat-stat-card">
                                <div class="card-block">
                                    <div class="row align-items-center text-center p-b-20 p-t-20">
                                        <div class="col-4 p-r-0"> 
                                            <i class="fa fa-cloud-download text-c-purple f-24"></i> 
                                        </div>
                                        <div class="col-8 p-l-0 text-left"> 
                                            <h5>{{${menuId}.monitorData.downloadNum}}</h5> 
                                            <p class="text-muted m-b-0">下载次数</p> 
                                        </div>
                                    </div>
                                </div> 
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="card mat-stat-card">
                                <div class="card-block">
                                    <div class="row align-items-center text-center p-b-20 p-t-20">
                                        <div class="col-4 p-r-0"> 
                                            <i class="far fa-user text-c-blue f-24"></i> 
                                        </div>
                                        <div class="col-8 p-l-0 text-left"> 
                                            <h5>{{${menuId}.monitorData.registNum}}</h5> 
                                            <p class="text-muted m-b-0">注册人数</p> 
                                        </div>
                                    </div>
                                </div> 
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="card mat-stat-card">
                                <div class="card-block">
                                    <div class="row align-items-center text-center p-b-20 p-t-20">
                                        <div class="col-4 p-r-0"> 
                                            <i class="fa fa-dollar text-c-orenge f-24"></i> 
                                        </div>
                                        <div class="col-8 p-l-0 text-left"> 
                                            <h5>{{${menuId}.monitorData.paymentNum}}</h5> 
                                            <p class="text-muted m-b-0">充值人数</p> 
                                        </div>
                                    </div>
                                </div> 
                            </div>
                        </div>
                    </div>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header"><h5>个人信息</h5></div>
                                <div class="card-block" style="min-height:360px">
                                    <div class="row m-b-20">
                                        <div class="col-sm-6"> 
                                            <p class="m-b-10">姓名：</p> 
                                            <h6>{{${menuId}.agent.name}}</h6> 
                                        </div> 
                                        <div class="col-sm-6"> 
                                            <p class="m-b-10">层级：</p> 
                                            <h6>{{${menuId}.agent.level}}级代理：</h6> 
                                        </div>
                                    </div>
                                    <div class="row m-b-20">
                                        <div class="col-sm-6"> 
                                            <p class="m-b-10">上级代理：</p> 
                                            <h6>{{${menuId}.agent.parentName || '顶级'}}</h6> 
                                        </div>
                                        <div class="col-sm-6"> 
                                            <p class="m-b-10">创建时间：</p> 
                                            <h6>{{${menuId}.agent._createTime}}</h6> 
                                        </div>
                                     </div>
                                </div> 
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header"><h5>推广链接</h5></div>
                                <div class="card-block" style="min-height:360px">
                                    <div class="ml-auto mr-auto text-center" id="${menuId}_qrCode"></div>
                                    <div class="text-center pb-2 pt-2">
                                        <a href="javascript:;" class="text-c-blue b-b-primary btn-copy" data-toggle="tooltip" data-placement="top" data-original-title="点击复制" data-clipboard-text="{{${menuId}.agentUrl}}">{{${menuId}.agentUrl}}</a>
                                    </div>
                                </div> 
                            </div>
                        </div>
                    </div>
                    
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
</div>
