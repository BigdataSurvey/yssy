<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, $stateParams, datatableUtil, alertUtil, imageUtil, storageService, websocketService, toastr){
        var me = this;
        me.Admin = $rootScope.Admin;
        me.defaultAvatar = '${staticfile}/files/assets/images/avatar-3.jpg';
        var userMap = {};
        /*联系人列表*/
        me.contactList = [];
        /*当前联系人*/
        me.currentContact = null;
        var $chatListScrollbar = null;
        var $contactListScrollbar = null;
        me.contactPage = 1;
        me.contactHasMore = true;
        /*请求数据*/
        function loadData(){
            if(!me.contactHasMore) return;
            if(me.loading) return;
            me.loading = true;
            var _pageLimit = 10;
            var searchData = {
                page: me.contactPage,
                limit: _pageLimit
            };
            websocketService.request('003003', searchData, function(command){
                if(command.success){
                    var dataList = command.data || [];
                    var _id = null;
                    var _record = null;
                    if($stateParams.params) {
                        _id = $stateParams.params;
                    }
                    for(var i = 0; i < dataList.length; i++) {
                        var _contact = dataList[i];
                        renderContact(_contact);
                        userMap[_contact.id] = _contact;
                        if(_id == _contact.id){
                            _record = _contact;
                        }
                        me.contactList.push(_contact);
                    }
                    if(me.contactList.length > 0 && !me.currentContact) {
                        _record = me.contactList[0];
                    }
                    if(_record) {
                        me.changeChat(_record);
                    }
                    /*计算下一页*/
                    if(dataList.length < _pageLimit) {
                        me.contactHasMore = false;
                    } else {
                        me.contactPage += 1;
                    }
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        function renderContact(contact){
            contact._name = contact.nickname || contact.name || contact.phone;
            if(contact.lastMessage && contact.lastMessage.time) {
                contact.lastMessage._time = $filter('date')(contact.lastMessage.time, 'yy-MM-dd hh:mm');
            }
            contact._hasMore = true;
            contact._start = 0;
            if(!contact.photo) {
                contact.photo = me.defaultAvatar;
            }
        };
        function renderMessage(chat){
            if(chat.type == 1){
                chat.message = $.parseJSON(chat.message);
            }
        };
        /*切换聊天人*/
        me.changeChat = function(contact){
            if(me.currentContact && me.currentContact.id == contact.id) return;
            me.currentContact = contact;
            if(me.currentContact.chatLog){
                me.currentContact.unread = 0;
                $chatListScrollbar.mCustomScrollbar("scrollTo", "bottom", {scrollEasing : 'easeOut'});
                $('#${menuId}_replyContent').focus();
            } else {
                loadChartData();
            }
        };
        function loadChartData() {
            if(me.saving) return;
            me.saving = true;
            var _pageSize = 10;
            var searchData = {
                targetId: me.currentContact.id,
                start: me.currentContact._start,
                limit: _pageSize
            };
            websocketService.request('003004', searchData, function(command){
                if(command.success){
                    me.currentContact.unread = 0;
                    var dataList = command.data || [];
                    if(!me.currentContact.chatLog) me.currentContact.chatLog = [];
                    for(var i = 0; i < dataList.length; i++){
                        renderMessage(dataList[i]);
                        me.currentContact.chatLog.unshift(dataList[i]);
                    }
                    me.currentContact._start += dataList.length;
                    if(dataList.length < _pageSize) {
                        me.currentContact._hasMore = false;
                    }
                    $('#${menuId}_replyContent').val('').focus();
                } else {
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
                
            });
        };
        me.loadChatMore = function(){
            if(!me.currentContact._hasMore) return false;
            loadChartData();
        };
        me.chatFinished = function(){
            if($chatListScrollbar) {
                $chatListScrollbar.mCustomScrollbar("update");
            } else {
                $chatListScrollbar = $('#${menuId}_chatList');
                $chatListScrollbar.mCustomScrollbar({
                    theme:'dark-2',
                    setHeight: "500px",
                    axis:"y",
                    scrollInertia: 50,
                    alwaysShowScrollbar: 1
                });
            }
            $chatListScrollbar.mCustomScrollbar("scrollTo", "#${menuId}_chatBottom", {scrollEasing : 'easeOut'});
        };
        me.contactFinished = function(){
            if($contactListScrollbar) {
                $contactListScrollbar.mCustomScrollbar("update");
            } else {
                $contactListScrollbar = $('#${menuId}_contactList');
                $contactListScrollbar.mCustomScrollbar({
                    theme:'dark-2',
                    setHeight: "780px",
                    axis:"y",
                    scrollInertia: 50
                });
            }
        };
        function loadFile() {
            /*上传文件*/
            $('#${menuId}_imageFile').on('change', function(evt){
                var file = evt.target.files[0];
                if(file) {
                    imageUtil.getImg({
                        file: file,
                        callback: function(command){
                            if(command.success) {
                                sendMessage(1, command.data.content)
                            } else {
                                toastr.error(command.message || '提交异常', '温馨提示');
                            }
                            $('#${menuId}_imageFile').val('');
                        }
                    });
                }
            });
        };
        /*发送消息*/
        function sendMessage(type, content){
            if(me.saving) return;
            me.saving = true;
            var lastMessage = {
                time: new Date().getTime() + 100,
                type: type,
                message: type == 0 ? content : {real: content},
                sending: true
            };
            var message = {
                senderId: me.Admin.id,
                time: lastMessage.time,
                message: type == 0 ? content : {real: content},
                type: type,
                sending: true
            };
            if(!me.currentContact.chatLog) me.currentContact.chatLog = [];
            me.currentContact.chatLog.push(message);
            me.currentContact.lastMessage = lastMessage;
            var submintData = {
                senderId: '${User.id}',
                receiverId: me.currentContact.id,
                message: content
            };
            var methodCode = '003001';
            if(type == 1) {
                methodCode = '003006';
            }
            websocketService.request(methodCode, submintData, function(command){
                if(command.success){
                    var result = command.data || {};
                    message.time = result.sendTime;
                    message.id = result.id;
                    type == 1 && (message.message = result.message);
                    lastMessage.time = result.sendTime;
                    delete message.sending;
                    delete lastMessage.sending;
                    me.currentContact._replyContent = null;
                } else {
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        /*刷新*/
        me.loadContactData = function() {
            loadData();
        };
        /*客服消息*/
        $scope.$on('chat', function(e, command) {
            var result = command.data || {};
            var contact = userMap[result.senderId];
            var type = result.type;
            if(type == 1){
                result.message = $.parseJSON(result.message);
            }
            var lastMessage = {message: result.message, time: result.sendTime, type: type};
            if(contact){
                if(!contact.chatLog) contact.chatLog = [];
                contact.chatLog.push({
                    id: result.id,
                    senderId: result.senderId,
                    message: result.message,
                    time: result.sendTime,
                    type: type
                });
                contact.lastMessage = lastMessage;
                $scope.$apply();
            } else {
                contact = {
                    id: result.senderId,
                    status: 1,
                    name: 'Loading...',
                    lastMessage: lastMessage
                };
                userMap[result.senderId] = contact;
                me.contactList.unshift(contact);
                loadContactInfo(contact);
            }
        });
        /*加载客户信息*/
        function loadContactInfo(contact) {
            websocketService.request('003005', {userId: contact.id}, function(command){
                if(command.success){
                    var user = command.data || {};
                    contact.name = user.name;
                    contact.nickname = user.nickname;
                    contact.photo = user.photo;
                    contact.phone = user.phone;
                    contact.lastLoginIp = user.lastLoginIp;
                    renderContact(contact);
                    userMap[contact.id] = contact;
                }
                $scope.$apply();
            });
        }
        /*刷新聊天数据*/
        me.loadChatData = function(){
            
        };
        /*保存*/
        me.sendData = function() {
            var _content = me.currentContact._replyContent;
            if(_content) {
                sendMessage(0, _content);
            }
        };
        me.keydownReply = function($event){
            if($event.keyCode === 13 && !$event.shiftKey){
                $event.preventDefault();
                $event.stopPropagation();
                return false;
            }
        };
        me.keyupReply = function($event){
            if ($event && $event.keyCode === 13 && $event.shiftKey){
                return;
            }
            if ($event && $event.keyCode === 13 ){
                me.sendData();
            }
        };
        /*初始化*/
        function initPage() {
            loadData();
            loadFile();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>