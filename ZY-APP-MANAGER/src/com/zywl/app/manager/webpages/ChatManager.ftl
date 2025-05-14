<script>
    <#include "js/inner-page.js">
    <#include "ChatManager.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <!-- Page-header start -->
    <div class="page-header">
        <div class="page-block">
            <div class="row align-items-center">
                <div class="col-md-8">
                    <div class="page-header-title">
                        <h5 class="m-b-10">客服</h5>
                        <!--<p class="m-b-0">Welcome to Material Able</p>-->
                    </div>
                </div>
                <div class="col-md-4">
                    <ul class="breadcrumb">
                        <li class="breadcrumb-item"> <a href="javascript:;"><i class="fa fa-home"></i> </a> </li>
                        <li class="breadcrumb-item"><a href="javascript:;">客服</a> </li>
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
                            <h5>客服</h5>
                        </div>
                        <div class="card-block">
                            <div class="row">
                                <!--user list:begin-->
                                <div class="col-md-3">
                                    <div class="user-body user-Messages-card">
                                        <div id="${menuId}_contactList">
                                            <ul class="page-list nav nav-tabs flex-column">
                                                <li class="nav-item mail-section" ng-class="{'chat-selected': ${menuId}.currentContact.id == record.id, 'unread': record.unread}"
                                                    ng-repeat="record in ${menuId}.contactList | orderBy:'lastMessage.time':true" ng-init="$last && ${menuId}.contactFinished()">
                                                    <a class="u-img nav-link waves-effect d-flex row ml-0 mr-0 pl-0 pr-0" ng-click="${menuId}.changeChat(record)"> 
                                                        <div class="col-auto p-r-0">
                                                            <div class="u-img">
                                                                <img ng-src="{{record.photo}}" alt="{{record._title}}" class="img-radius profile-img img-50">
                                                                <span class="tot-msg" ng-if="record.unread">{{record.unreadNum}}</span>
                                                            </div>
                                                        </div>
                                                        <div class="col">
                                                            <h6 class="m-b-5">{{record._name}} <span class="text-muted f-right f-13">{{record.lastMessage.time | altDate}}</span></h6>
                                                            <p class="text-muted m-b-0 short-message" ng-if="record.lastMessage.type == 0"><span class="mr-2" ng-if="record.lastMessage.sending">发送中...</span>{{record.lastMessage.message}}</p>
                                                            <p class="text-muted m-b-0" ng-if="record.lastMessage.type == 1"><span class="mr-2" ng-if="record.lastMessage.sending">发送中...</span>[图片]</p>
                                                        </div>
                                                    </a>
                                                </li>
                                            </ul>
                                            <div class="p-2 text-center" id="${menuId}_contactBottom">
                                                <a class="" ng-class="{'text-primary': ${menuId}.contactHasMore, 'text-gray': !${menuId}.contactHasMore}" ng-click="${menuId}.loadContactData()" href="javascript:;">&gt;&gt;&gt;<span class="pl-1 pr-1">{{${menuId}.contactHasMore ? '点击加载更多' : '没有更多了'}}</span>&lt;&lt;&lt;</a>
                                            </div>
                                        </div>
                                        <div class="clearfix">
                                            <div class="pull-left" id="${menuId}_pageSelection"></div>
                                        </div>
                                    </div>
                                </div>
                                <!--user list:end-->
                                <!--chat list:begin-->
                                <div class="col-md-9">
                                    <div class="card chat-card">
                                        <div class="card-header">
                                            <h5>{{${menuId}.currentContact._name}}</h5>
                                        </div>
                                        <div class="card-block p-0 pl-3 o-hidden">
                                            <div class="chat-list" id="${menuId}_chatList">
                                                <div class="p-2 text-center" id="${menuId}_chatTop">
                                                    <a class="" ng-class="{'text-primary': ${menuId}.currentContact._hasMore, 'text-gray': !${menuId}.currentContact._hasMore}" ng-click="${menuId}.loadChatMore()" href="javascript:;">&gt;&gt;&gt;<span class="pl-1 pr-1">{{${menuId}.currentContact._hasMore ? '点击加载更多' : '没有更多了'}}</span>&lt;&lt;&lt;</a>
                                                </div>
                                                <div class="row m-b-20" ng-class="{'received-chat': record.senderId == ${menuId}.currentContact.id, 'send-chat': record.senderId != ${menuId}.currentContact.id}" 
                                                    ng-repeat="record in ${menuId}.currentContact.chatLog | orderBy:'time':false" ng-init="$last && ${menuId}.chatFinished()">
                                                    <div class="col-auto p-r-0" ng-if="record.senderId != ${menuId}.Admin.id">
                                                        <img ng-src="{{${menuId}.currentContact.photo || ${menuId}.defaultAvatar}}" class="img-radius img-40">
                                                    </div>
                                                    <div class="col">
                                                        <div class="msg">
                                                            <p class="m-b-0" ng-if="record.type == 0">{{record.message}}</p>
                                                            <p class="m-b-0" ng-if="record.type == 1"><a href="{{record.message.real}}" data-toggle="lightbox" data-lightbox="{{record.senderId}}"><img ng-src="{{record.message.thumbnail || record.message.real}}" class="chat-img"></a></p>
                                                        </div>
                                                        <p class="text-muted m-b-0"><i class="fa fa-clock-o m-r-10"></i>{{record.time | altDate}}</p>
                                                    </div>
                                                    <div class="col-auto p-r-0" ng-if="record.senderId == ${menuId}.Admin.id">
                                                        <img ng-src="{{${menuId}.Admin.photo || ${menuId}.defaultAvatar}}" class="img-radius img-40">
                                                    </div>
                                                </div>
                                                <div class="p-2" id="${menuId}_chatBottom"></div>
                                            </div>
                                        </div>
                                        <div class="card-block">
                                            <div class="form-group form-primary">
                                                <textarea type="text" class="form-control" rows="5" cols="20" autocomplete="off" placeholder="回车发送、Shift + 回车换行..." id="${menuId}_replyContent" 
                                                    ng-keydown="${menuId}.keydownReply($event)" ng-keyup="${menuId}.keyupReply($event)" ng-model="${menuId}.currentContact._replyContent">
                                                </textarea>
                                            </div>
                                            <div class="d-flex align-items-center justify-content-end">
                                                <label for="${menuId}_imageFile" class="btn btn-round btn-primary btn-icon waves-effect waves-light mb-0 mr-4" ng-disabled="${menuId}.saving"> <i class="fa fa-file-image-o mr-0"></i>
                                                    <input class="d-none" type="file" id="${menuId}_imageFile" accept=".png,.jpg,.jpeg,.gif" />
                                                </label>
                                                <button class="btn btn-round btn-info btn-icon waves-effect waves-light mr-1" ng-disabled="${menuId}.saving" ng-click="${menuId}.sendData()"> <i class="fa fa-paper-plane mr-0"></i></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <!--chat list:end-->
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Page-body end -->
            </div>
        </div>
    </div>
    
</div>

