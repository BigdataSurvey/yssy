<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, imageUtil, toastr){
        var me = this;
        var dataList = [];
        var baseTable = null;
        var $formImg = $('#${menuId}_form_img');
        var cropperImg = false;
        var cropperOption = {
            aspectRatio: 3 / 1,
            minContainerWidth: 760,
            minContainerHeight: 360,
            autoCropArea: 1,
            cropmove: function(event) {
                cropperImg = true;
            }
        };
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "title", title: '标题', render: datatableUtil.renderNormal},
                    {data: "url", title: '链接', render: function(data, type, row, setting){
                        var _html = null;
                        if(data) {
                            _html = '<a class="text-primary" target="_blank" href="' + data + '">' + data + '</a>';
                        }
                        return _html || '-';
                    }},
                    {data: "img3_1", title: '图片', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="轮播图" data-lightbox="scale"><img src="' + data + '" class="td-img"></a>';
                    }},
                    {data: "id", title: '操作', width: '220px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-info btn-sm mr-2" ng-click="${menuId}.upRow(' + setting.row + ')"><i class="ti-arrow-up"></i>上移</button>'
                            + '<button class="btn waves-effect waves-light btn-info btn-sm mr-2" ng-click="${menuId}.downRow(' + setting.row + ')"><i class="ti-arrow-down"></i>下移</button>'
                            + '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(' + setting.row + ')"><i class="ti-slice"></i>修改</button>'
                            + '<button class="btn waves-effect waves-light btn-danger btn-sm mr-2" ng-click="${menuId}.deleteRow(' + setting.row + ')"><i class="ti-close"></i>删除</button>';
                        return _opearHtml;
                    }},
                    ],
                    drawCallback: function(){
                        loadOpearte();
                    },
                    ajax: function (data, callback, settings) {
                        loadData(data, callback, settings);
                    }
            });
        };
        /*请求数据*/
        function loadData(data, callback, settings){
            if(me.loading) return;
            me.loading = true;
            websocketService.request('033001', null, function(command){
                if(command.success){
                    dataList = command.data || [];
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: dataList.length,
                        recordsFiltered: dataList.length,
                        data: dataList
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
            });
        };
        /*加载操作*/
        function loadOpearte() {
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        me.createRow = function(){
            me.currentTpl = {_title: '新增轮播图', img3_1: null};
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
            $('#${menuId}_form_photo').val('').trigger('change');
            $formImg.removeAttr('src').trigger('change');
            var _cropper = $formImg.data('cropper');
            if(_cropper) {
                $formImg.cropper('destroy');
            }
            $formImg.cropper(cropperOption);
            $('#${menuId}_rowModal').modal('show');
        };
        me.updateRow = function(index){
            var record = dataList[index];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = '编辑轮播图';
                me.currentTpl._errorMsg = null;
                me.currentTpl._index = index;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                for(var key in record) {
                    var _value = record[key];
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(_value).trigger('change');
                }
                $('#${menuId}_form_photo').val('').trigger('change');
                $formImg.attr('src', record.img3_1).trigger('change');
                var _cropper = $formImg.data('cropper');
                if(_cropper) {
                    $formImg.cropper('destroy');
                }
                $formImg.cropper(cropperOption);
                $('#${menuId}_rowModal').modal('show');
            }
        };
        /*上移*/
        me.upRow = function(index) {
            if(index > 0) {
                dataList.splice(index - 1, 1, ...dataList.splice(index, 1, dataList[index - 1]));
                if(me.saving) return;
                me.saving = true;
                websocketService.request('033002', dataList, function(command){
                    if(command.success){
                        toastr.success('上移成功', '系统提示');
                        baseTable.ajax.reload();
                    } else {
                        toastr.error(command.message || '提交异常', '系统提示');
                    }
                    me.saving = false;
                });
            } else {
                toastr.warning('已经是第一条了', '温馨提示');
            }
        };
        /*下移*/
        me.downRow = function(index) {
            if(index < (dataList.length - 1)) {
                dataList.splice(index + 1, 1, ...dataList.splice(index, 1, dataList[index + 1]));
                if(me.saving) return;
                me.saving = true;
                websocketService.request('033002', dataList, function(command){
                    if(command.success){
                        toastr.success('下移成功', '系统提示');
                        baseTable.ajax.reload();
                    } else {
                        toastr.error(command.message || '提交异常', '系统提示');
                    }
                    me.saving = false;
                });
            } else {
                toastr.warning('已经是最后一条了', '温馨提示');
            }
        };
        /*删除*/
        me.deleteRow = function(index) {
            var record = dataList[index];
            if(record) {
                alertUtil.alert({
                    html: '即将删除 <span class="ml-1 text-danger">' + record.title + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {index: index};
                        websocketService.request('033003', submitData, function(command){
                            if(command.success){
                                baseTable.ajax.reload();
                                Swal.close();
                            } else {
                                toastr.error(command.message || '提交异常', '系统提示');
                                Swal.hideLoading();
                            }
                            me.saving = false;
                            $scope.$apply();
                        });
                    }
                });
            }
        };
        /*保存*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            var _location = $('#${menuId}_formEdit').find('input[name=location]:checked').attr('data-value');
            if(cropperImg) {
                submitData.img3_1 = $formImg.cropper('getCroppedCanvas').toDataURL();
            } else {
                submitData.img3_1 = me.currentTpl.img3_1;
            }
            if(!submitData.title || !submitData.img3_1) {
                toastr.warning('请输入完成信息', '温馨提示');
                return false;
            }
            if(me.currentTpl.$type == 'edit') {
                dataList[me.currentTpl._index] = submitData;
            } else {
                if(_location == 1) {
                    dataList.unshift(submitData);
                } else {
                    dataList.push(submitData);
                }
            }
            me.saving = true;
            websocketService.request('033002', dataList, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                    $('#${menuId}_rowModal').modal('hide');
                    cropperImg = false;
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        function initPage() {
            initTable();
            $('#${menuId}_form_photo').on('change', function(evt){
                var $this = $(this);
                var file = evt.target.files[0];
                if(file) {
                    me.saving = true;
                    imageUtil.getImg({
                        original: true,
                        file: file,
                        callback: function(command){
                            if(command.success) {
                                $formImg.attr('src', command.data.content).trigger('change');
                                $formImg.cropper('destroy').cropper(cropperOption);
                                cropperImg = true;
                            } else {
                                toastr.error(command.message, '系统提示');
                                $this.val('').trigger('change');
                            }
                            me.saving = false;
                            $scope.$apply();
                        }
                    });
                }
            });
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>