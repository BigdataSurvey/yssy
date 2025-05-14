<script>
    <#include "js/inner-page.js">
    <#include "AdminLog.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">管理员日志</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">管理员日志</a> </li>
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
                            <h5>管理员日志</h5>
                            <div class="card-header-right"> 
                                <ul class="list-unstyled">
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

    
</div>

