<style>
    #${menuId}_content .dataTables_empty{line-height:70px;}
    #${menuId}_content .table td, #${menuId}_content .table th{padding: 0.9rem 0.75rem;}
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
						<h5 class="m-b-10">系统概况</h5>
						<!--<p class="m-b-0">Welcome to Material Able</p>-->
					</div>
				</div>
				<div class="col-md-4">
					<ul class="breadcrumb">
						<li class="breadcrumb-item"><a href="javascript:;"> <i class="fa fa-home"></i> </a> </li>
						<li class="breadcrumb-item"><a href="javascript:;">总览</a> </li>
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
						<div class="col-xl-4 col-md-12">
							<div class="card mat-stat-card">
								<div class="card-block">
									<div class="row align-items-center b-b-default">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="far fa-user text-c-purple f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.totalUser}}</h5>
													<p class="text-muted m-b-0">总人数</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-caret-square-o-right text-c-green f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.todayRegister}}</h5>
													<p class="text-muted m-b-0">今日注册数</p>
												</div>
											</div>
										</div>
									</div>
									<div class="row align-items-center">
										<div class="col-sm-6 p-b-20 p-t-20 b-r-default">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-video-camera text-c-red f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.todayLogin}}</h5>
													<p class="text-muted m-b-0">今日登录数</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-sign-language text-c-blue f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.serverTpl.people}}</h5>
													<p class="text-muted m-b-0">实时在线</p>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						<div class="col-xl-4 col-md-12">
							<div class="card mat-stat-card">
								<div class="card-block">
									<div class="row align-items-center b-b-default">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-stop-circle text-c-yellow f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.totalCur2.toFixed(4)}}</h5>
													<p class="text-muted m-b-0">用户总通宝</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-stop-circle f-24" style="color:#ff5b9d"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.no1}}</h5>
													<p class="text-muted m-b-0">排名第一通宝数</p>
												</div>
											</div>
										</div>
									</div>
									<div class="row align-items-center">
										<div class="col-sm-6 p-b-20 p-t-20 b-r-default">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-dollar text-c-red f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.totalCanUseCur2 || 0}}</h5>
													<p class="text-muted m-b-0">流通通宝</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-money text-c-blue f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.totalNo10 || 0}}</h5>
													<p class="text-muted m-b-0">前10通宝总和</p>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						<div class="col-xl-4 col-md-12">
							<div class="card mat-stat-card">
								<div class="card-block">
									<div class="row align-items-center b-b-default">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-sitemap text-c-purple f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.serverTpl.node}}</h5>
													<p class="text-muted m-b-0">节点数</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-users text-c-green f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.serverTpl.people}}</h5>
													<p class="text-muted m-b-0">在线设备数</p>
												</div>
											</div>
										</div>
									</div>
									<div class="row align-items-center">
										<div class="col-sm-6 b-r-default">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-server text-c-red f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left d-flex justify-content-center"
													ng-class="{'text-c-red': ${menuId}.serverTpl.hasError, 'text-c-purple': ${menuId}.serverTpl.hasBusy}"
													style="height: 82px;flex-direction: column;">
													<!--<h5>{{${menuId}.serverTpl.status}}</h5>
                                                    <p class="text-muted m-b-0">集群状态</p>-->
													<h5>{{${menuId}.serverTpl.totalQps}}</h5>
													<p class="text-muted m-b-0">QPS</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-exclamation-circle text-c-blue f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left d-flex justify-content-center"
													ng-class="{'text-c-red': ${menuId}.serverTpl.hasError, 'text-c-purple': ${menuId}.serverTpl.hasBusy}"
													style="overflow: auto;height: 82px;padding: 10px;flex-direction: column;">
													<div class="f-12">{{${menuId}.serverTpl.error}}</div>
													<p class="text-muted m-b-0"
														ng-hide="${menuId}.serverTpl.hasError || ${menuId}.serverTpl.hasBusy">
														问题节点</p>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						<div class="col-xl-4 col-md-12">
							<div class="card mat-stat-card">
								<div class="card-block">
									<div class="row align-items-center b-b-default">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-money text-c-purple f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.waitCashCount}}</h5>
													<p class="text-muted m-b-0">待提现订单数量</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-money text-c-green f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.waitCashAmount}}</h5>
													<p class="text-muted m-b-0">待提现订单金额</p>
												</div>
											</div>
										</div>
									</div>
									<div class="row align-items-center">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-money text-c-red f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.cashCount}}</h5>
													<p class="text-muted m-b-0">已提现订单数</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-money text-c-blue f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.cashAmount}}</h5>
													<p class="text-muted m-b-0">已提现订单金额</p>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>

						<div class="col-xl-4 col-md-12">
							<div class="card mat-stat-card">
								<div class="card-block">
									<div class="row align-items-center b-b-default">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-stop-circle text-c-yellow f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.gift99All}}</h5>
													<p class="text-muted m-b-0">99礼包订单数量</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-stop-circle f-24" style="color:#ff5b9d"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.gift499All}}</h5>
													<p class="text-muted m-b-0">499礼包订单数量</p>
												</div>
											</div>
										</div>
									</div>
									<div class="row align-items-center">
										<div class="col-sm-6 p-b-20 p-t-20 b-r-default">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-dollar text-c-red f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.gift99Pay}}</h5>
													<p class="text-muted m-b-0">99礼包成交数量</p>
												</div>
											</div>
										</div>
										<div class="col-sm-6 p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-money text-c-blue f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.gift499Pay}}</h5>
													<p class="text-muted m-b-0">499礼包成交数量</p>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>






						<div class="col-xl-4 col-md-12">
							<div class="card mat-stat-card">
								<div class="card-block">
									<div class="row align-items-center b-b-default">
										<div class="col-sm-6 b-r-default p-b-20 p-t-20">
											<div class="row align-items-center text-center">
												<div class="col-4 p-r-0">
													<i class="fas fa-stop-circle text-c-yellow f-24"></i>
												</div>
												<div class="col-8 p-l-0 text-left">
													<h5>{{${menuId}.monitorData.todayAdNum || 0}}</h5>
													<p class="text-muted m-b-0">文房四宝数量</p>
												</div>
											</div>
										</div>

									</div>
								</div>
							</div>
						</div>
					</div>

					<!-- 节点概况-->
					<div class="row">
						<div class="col-xl-3 col-lg-6 col-md-12"
							ng-repeat="(key, record) in ${menuId}.serverMap | orderObjectBy:'id':false"
							ng-init="$last && ${menuId}.serverFinished()">
							<div class="card total-card bg-c-green">
								<div class="card-block pt-0 pb-0">
									<div class="row">
										<div class="col-4 pr-0">
											<div class="text-left d-flex"
												style="flex-direction: column;justify-content: center;height: 100%;">
												<h4 ng-class="record.nameCss"
													style="font-size: 16px;margin-bottom: .5rem!important;font-weight: 600;">
													{{record.id}}
												</h4>
												<p class="m-0 f-12 mb-2" style="font-size:13px;">在线：{{record.count}}</p>
												<p class="m-0 f-12 mb-2" style="font-size:13px;">QPS：{{record.qps}}</p>
												<p class="m-0 f-12 mb-2" style="font-size:13px;">负载：{{record._task}}</p>
												<p class="m-0 f-12 mb-2" style="font-size:13px;"><a class="text-c-white"
														href="javascript:;"
														ng-click="${menuId}.updateRow_weight(record)">权重：{{record.serverWeight}}</a>
												</p>
											</div>
										</div>
										<div class="col-8 pl-0 pr-0">
											<div id="${menuId}_node_{{record.index}}" style="width:100%;height:200px;">
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!-- Page-body end -->
		</div>

	</div>

	<!-- 修改服务器权重:begin -->
	<div class="modal fade" id="${menuId}_rowModal_weight" tabindex="-1" role="dialog" aria-hidden="true"
		data-backdrop="static">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header no-bd">
					<h5 class="modal-title">修改权重：<span class="text-danger">{{${menuId}.currentTpl._title}}</span></h5>
					<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
							aria-hidden="true">×</span> </button>
				</div>
				<div class="modal-body">
					<form role="form" autocomplete="off" id="${menuId}_formEdit_weight">
						<div class="row">
							<div class="col-md-12">
								<div class="form-group">
									<label class="text-danger d-none">权重：</label>
									<input type="number" class="form-control" placeholder="权重" name="weight">
								</div>
							</div>
							<div class="col-md-12">
								<div class="alert alert-danger border-danger" ng-if="${menuId}.currentTpl._errorMsg">
									{{${menuId}.currentTpl._errorMsg}}
								</div>
							</div>
						</div>
					</form>
				</div>
				<div class="modal-footer no-bd">
					<button class="btn btn-success" type="button" ng-disabled="${menuId}.saving"
						ng-click="${menuId}.saveData_weight()"><span class="btn-label mr-1"> <i class="fa fa-check"></i>
						</span> 提交</button>
					<button type="button" class="btn btn-danger" data-dismiss="modal"><span class="btn-label mr-1"> <i
								class="fas fa-times"></i> </span> 关闭</button>
				</div>
			</div>
		</div>
	</div>
	<!-- 修改服务器权重:end -->


</div>
