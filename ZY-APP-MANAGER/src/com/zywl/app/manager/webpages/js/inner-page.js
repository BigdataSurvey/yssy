<@compress single_line=true>
$(document).ready(function() {
    "use strict";
    /*wave effect js*/
    Waves.init();
    Waves.attach('#${menuId}_content .flat-buttons', ['waves-button']);
    Waves.attach('#${menuId}_content .float-buttons', ['waves-button', 'waves-float']);
    Waves.attach('#${menuId}_content .float-button-light', ['waves-button', 'waves-float', 'waves-light']);
    Waves.attach('#${menuId}_content .flat-buttons', ['waves-button', 'waves-float', 'waves-light', 'flat-buttons']);
    $('#${menuId}_content .form-control').on('blur', function() {
        if ($(this).val().length > 0) {
            $(this).addClass("fill");
        } else {
            $(this).removeClass("fill");
        }
    });
    $('#${menuId}_content .form-control').on('focus', function() {
        $(this).addClass("fill");
    });
    if($('#${menuId}_content .select2')[0]) {
        /*禁用空选择*/
        $('#${menuId}_content .select2').select2({
            theme: "classic",
            width: '100%',
            placeholder: '-- 请选择 --',
            allowClear: true
        });
    }
    if($('#${menuId}_content .select3')[0]) {
        /*禁用搜索框*/
        $('#${menuId}_content .select3').select2({
            theme: "classic",
            width: '100%',
            minimumResultsForSearch: -1,
            placeholder: '-- 请选择 --'
        });
    }
    if($('#${menuId}_content .select4')[0]) {
        /*显示空选择*/
        $('#${menuId}_content .select4').select2({
            theme: "classic",
            width: '100%',
            minimumResultsForSearch: -1
        });
    };
    if($('#${menuId}_content .select5')[0]) {
        /*显示空选择*/
        $('#${menuId}_content .select5').select2({
            theme: "classic",
            width: '100%',
            allowClear: true
        });
    };
});
</@compress>