<#compress>
<!doctype html>
<html lang="en" data-ng-app="LIVE-APP">
<head>
    <title>Desktop - ${company}</title>
    <!--[if lt IE 10]>
    <script src="https://cdn.bootcss.com/html5shiv/3.7.0/html5shiv.min.js"></script>
    <script src="https://cdn.bootcss.com/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <!-- Meta -->
    <meta charset="utf-8">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="keywords" content="${company}">
    <meta name="author" content="${company}">
    <!-- Favicon icon -->
    <link rel="icon" href="${staticfile}/files/assets/images/favicon.ico" type="image/x-icon">
    <!-- waves.css -->
    <link rel="stylesheet" href="${staticfile}/files/assets/pages/waves/css/waves.min.css" type="text/css" media="all">
    <!-- Required Fremwork -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/bootstrap/css/bootstrap.min.css">
    <!-- waves.css -->
    <link rel="stylesheet" href="${staticfile}/files/assets/pages/waves/css/waves.min.css" type="text/css" media="all">
    <!-- themify icon -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/icon/themify-icons/themify-icons.css">
    <!-- ico font --> 
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/icon/icofont/css/icofont.css">
    <!-- font-awesome-n -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/css/font-awesome-n.min.css">
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/css/font-awesome.min.css">
    <!-- Data Table Css -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/datatables.net-bs4/css/dataTables.bootstrap4.min.css">
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/pages/data-table/css/buttons.dataTables.min.css">
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/datatables.net-responsive-bs4/css/responsive.bootstrap4.min.css">
    <!-- Switch component css -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/switchery/css/switchery.min.css">
    <!-- light-box css --> 
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/ekko-lightbox/css/ekko-lightbox.css"> 
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/lightbox2/css/lightbox.css"> 
    <!-- scrollbar.css -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/css/jquery.mCustomScrollbar.css">
    <!-- toastr -->
    <link href="https://cdn.bootcss.com/angular-toastr/2.1.1/angular-toastr.min.css" rel="stylesheet">
    <!--animate-->
    <link href="https://cdn.bootcss.com/animate.css/3.7.2/animate.min.css" rel="stylesheet">
    <!-- sweetalert2 -->
    <link href="https://cdn.bootcss.com/limonte-sweetalert2/8.11.8/sweetalert2.min.css" rel="stylesheet">
    <!-- datepicker -->
    <link href="https://cdn.bootcss.com/bootstrap-datepicker/1.6.4/css/bootstrap-datepicker.standalone.min.css" rel="stylesheet">
    <!-- select2 -->
    <link href="https://cdn.bootcss.com/select2/4.0.8/css/select2.min.css" rel="stylesheet">
    <!-- cropper -->
    <link href="https://cdn.bootcss.com/cropper/4.0.0/cropper.min.css" rel="stylesheet">
    <!-- Style.css -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/css/style.css">
    <style>
        <#include "css/fix.css">
    </style>
</head>

<body data-ng-controller="desktopCtrl as dctrl">
    <#include "common/PreLoader.ftl">
    <div id="pcoded" class="pcoded">
        <div class="pcoded-overlay-box"></div>
        <div class="pcoded-container navbar-wrapper">
            <#include "common/Nav.ftl">
            <#include "common/SidebarChat.ftl">
            <div class="pcoded-main-container">
                <div class="pcoded-wrapper">
                    <#include "common/LeftNav.ftl">
                    <div ui-view id="proded-content"></div>
                </div>
            </div>
        </div>
    </div>
    <!-- Warning Section Starts -->
    <#include "common/IEWarning.ftl">
    <!-- Warning Section Ends -->
    <!-- gaode map -->
    <script type="text/javascript" src="https://webapi.amap.com/maps?v=1.4.15&key=f2e2a36eed4965ce312ca84cdeeb7d74"></script>
    <!-- Required Jquery -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery/js/jquery.min.js "></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery-ui/js/jquery-ui.min.js "></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/popper.js/js/popper.min.js"></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/bootstrap/js/bootstrap.min.js "></script>
    <!-- waves js -->
    <script src="${staticfile}/files/assets/pages/waves/js/waves.min.js"></script>
    <!-- jquery slimscroll js -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery-slimscroll/js/jquery.slimscroll.js "></script>
    <!-- modernizr js -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/modernizr/js/modernizr.js "></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/modernizr/js/css-scrollbars.js"></script>
    <!-- slimscroll js -->
    <script src="${staticfile}/files/assets/js/jquery.mCustomScrollbar.concat.min.js "></script>
    <!-- menu js -->
    <script src="${staticfile}/files/assets/js/pcoded.min.js"></script>
    <!-- data-table js -->
    <script src="${staticfile}/files/bower_components/datatables.net/js/jquery.dataTables.min.js"></script>
    <script src="${staticfile}/files/bower_components/datatables.net-buttons/js/dataTables.buttons.min.js"></script>
    <script src="${staticfile}/files/assets/pages/data-table/js/jszip.min.js"></script>
    <script src="${staticfile}/files/assets/pages/data-table/js/pdfmake.min.js"></script>
    <script src="${staticfile}/files/assets/pages/data-table/js/vfs_fonts.js"></script>
    <script src="${staticfile}/files/bower_components/datatables.net-buttons/js/buttons.print.min.js"></script>
    <script src="${staticfile}/files/bower_components/datatables.net-buttons/js/buttons.html5.min.js"></script>
    <script src="${staticfile}/files/bower_components/datatables.net-bs4/js/dataTables.bootstrap4.min.js"></script>
    <script src="${staticfile}/files/bower_components/datatables.net-responsive/js/dataTables.responsive.min.js"></script>
    <script src="${staticfile}/files/bower_components/datatables.net-responsive-bs4/js/responsive.bootstrap4.min.js"></script>
    <!-- Switch component js -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/switchery/js/switchery.min.js"></script>
    <!-- Sweet Alert -->
    <script src="${staticfile}/files/assets/js/sweetalert2.min.js"></script>
    <script src="${staticfile}/files/npm/promise-polyfill@7.1.0/dist/promise.min.js"></script>
    <!-- light-box js --> 
    <script type="text/javascript" src="${staticfile}/files/bower_components/ekko-lightbox/js/ekko-lightbox.js"></script> 
    <script type="text/javascript" src="${staticfile}/files/bower_components/lightbox2/js/lightbox.js"></script>
    <!-- DateTimePicker -->
    <script src="${staticfile}/files/assets/js/bootstrap-datepicker.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap-datepicker/1.6.4/locales/bootstrap-datepicker.zh-CN.min.js"></script>
    <!-- select2 -->
    <script src="${staticfile}/files/assets/js/select2.min.js"></script>
    <!-- cropper -->
    <script src="${staticfile}/files/assets/js/cropper.min.js"></script>
    <!-- Max-length js --> 
    <script type="text/javascript" src="${staticfile}/files/bower_components/bootstrap-maxlength/js/bootstrap-maxlength.js"></script>
    <!-- ajaxform -->
    <script src="${staticfile}/files/assets/js/jquery.form.min.js"></script>
    <!-- qrcode -->
    <script src="${staticfile}/files/assets/js/jquery.qrcode.min.js"></script>
    <!-- moment -->
    <script src="${staticfile}/files/assets/js/moment.min.js"></script>
    <script src="https://cdn.bootcss.com/moment.js/2.24.0/locale/zh-cn.js"></script>
    <script src="${staticfile}/files/cyberplayer-3.4.1/cyberplayer.js"></script>
    <!--svga-->
    <script src="${staticfile}/files/SVGAPlayer-Web/svga.min.js"></script>
    <script src="https://cdn.bootcss.com/howler/2.1.2/howler.core.min.js"></script>
    <!-- clipboard -->
    <script src="https://cdn.bootcss.com/clipboard.js/2.0.4/clipboard.min.js"></script>
    <!-- Custom js -->
    <script><#include "Desktop.jquery.js"></script>
    <!--angularjs-->
    <script src="${staticfile}/files/assets/js/angular.js"></script>
    <script src="${staticfile}/files/assets/js/angular-ui-router.min.js"></script>
    <script src="${staticfile}/files/assets/js/ocLazyLoad.min.js"></script>
    <script src="${staticfile}/files/assets/js/angular-toastr.min.js"></script>
    <script src="https://cdn.bootcss.com/angular-toastr/2.1.1/angular-toastr.tpls.min.js"></script>
   <#-- <script src="https://cdn.bootcss.com/angular.js/1.5.8/i18n/angular-locale_zh.js"></script>-->
    <script>
        <#include "js/desktop.js">
        <#include "js/localStorage.js">
        <#include "js/websocket.js">
        <#include "js/socketEvent.js">
        <#include "js/audioService.js">
        <#include "js/noticeService.js">
        <#include "js/amountUtil.js">
        <#include "js/datatableUtil.js">
        <#include "js/imageUtil.js">
        <#include "js/alertUtil.js">
        <#include "js/altDate.js">
        <#include "js/router.js">
        <#include "Desktop.js">
    </script>
</body>
</html>
</#compress>