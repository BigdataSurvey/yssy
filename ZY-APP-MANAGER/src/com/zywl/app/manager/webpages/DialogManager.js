<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        /*刷新*/
        me.loadData = function() {
            if(me.loading) return;
            me.loading = true;
            websocketService.request('015003', {key: 'APP_HOME_NOTICE'}, function(command){
                if(command.success){
                    $('#${menuId}_context').froalaEditor('html.set', command.data || '');
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
            });
        };
        /*修改*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {key: 'APP_HOME_NOTICE',value: $('#${menuId}_context').froalaEditor('html.get')};
            var text = htmlText(submitData.value);
            if(text.length > 1000) {
                toastr.warning('内容必须在1000字以内', '温馨提示');
                return false;
            };
            me.saving = true;
            websocketService.request('015002', submitData, function(command){
                if(command.success){
                    toastr.success("修改成功", '系统提示');
                } else {
                    me._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        function htmlText(html){
            var span = document.createElement("span");
            span.innerHTML = html;
            return span.innerText;
        };
        function initPage() {
            $('#${menuId}_context').froalaEditor({
                language: 'zh_cn',
                height: 400,
                placeholderText: '请输入内容...',
                toolbarButtons: ['insertLink']
            });
            me.loadData();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>