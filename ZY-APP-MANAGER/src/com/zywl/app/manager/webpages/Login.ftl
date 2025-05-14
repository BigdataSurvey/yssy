<#compress>
<!doctype html>
<html lang="en" data-ng-app="LIVE-APP">
<head>
    <title>Login - ${company}</title>
    <!--[if lt IE 10]>
    <script src="https://cdn.bootcdn.net/ajax/libs/html5shiv/3.7.0/html5shiv.min.js"></script>
    <script src="https://cdn.bootcdn.net/ajax/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <!-- Meta -->
    <meta charset="utf-8">
    <meta http-equiv="refresh" content="21600" />
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="keywords" content="${company}">
    <meta name="author" content="${company}">
    <!-- Favicon icon -->
    <link rel="icon" href="${staticfile}/files/assets/images/favicon.ico" type="image/x-icon">
    <!-- Required Fremwork -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/bower_components/bootstrap/css/bootstrap.min.css">
    <!-- waves.css -->
    <link rel="stylesheet" href="${staticfile}/files/assets/pages/waves/css/waves.min.css" type="text/css" media="all">
    <!-- themify-icons line icon -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/icon/themify-icons/themify-icons.css">
    <!-- ico font -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/icon/icofont/css/icofont.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/icon/font-awesome/css/font-awesome.min.css">
    <!-- Style.css -->
    <link rel="stylesheet" type="text/css" href="${staticfile}/files/assets/css/style.css">
    <link href="https://cdn.bootcdn.net/ajax/libs/angular-toastr/2.1.1/angular-toastr.min.css" rel="stylesheet">
    <style>
        <#include "css/login.css">
    </style>
    <script>
        if(window != top) top.location.reload();
        /*业务判断*/
        if(window._hasLogin) top.location.reload();
    </script>
</head>

<body themebg-pattern="theme4" data-ng-controller="loginCtrl as lctrl">
    <#include "common/PreLoader.ftl">
    <section class="login-block">
        <!-- Container-fluid starts -->
        <div class="container">
            <div class="row">
                <div class="col-sm-12">
                    <!-- Authentication card start -->
                    <form class="md-float-material form-material">
                        <div class="auth-box card">
                            <div class="card-block">
                                <div class="row m-b-20">
                                    <div class="col-md-12">
                                        <h3 class="text-center">${company}</h3>
                                    </div>
                                </div>
                                <div class="form-group form-primary">
                                    <input type="text" name="email" class="form-control" autocomplete="off" ng-class="{'fill': lctrl.hasAccount}" ng-model="lctrl.account" ng-keyup="lctrl.keyupLogin($event)">
                                    <span class="form-bar"></span>
                                    <label class="float-label">账号：</label>
                                </div>
                                <div class="form-group form-primary">
                                    <input type="password" name="password" class="form-control" ng-model="lctrl.password" ng-keyup="lctrl.keyupLogin($event)" autocomplete="off">
                                    <span class="form-bar"></span>
                                    <label class="float-label">密码：</label>
                                </div>
                                <div class="row m-t-25 text-left">
                                    <div class="col-12">
                                        <div class="checkbox-fade fade-in-primary d-">
                                            <label>
                                                <input type="checkbox" ng-model="lctrl.rememberMe">
                                                <span class="cr"><i class="cr-icon icofont icofont-ui-check txt-primary"></i></span>
                                                <span class="text-inverse">记住我（公共场所请谨慎使用）</span>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                                <div class="row m-t-30">
                                    <div class="col-md-12">
                                        <button type="button" class="btn btn-danger btn-md btn-block waves-effect waves-light text-center m-b-20" ng-disabled="lctrl.loading" ng-click="lctrl.doLogin()">登 录</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                    <!-- end of form -->
                </div>
                <!-- end of col-sm-12 -->
            </div>
            <!-- end of row -->
        </div>
        <!-- end of container-fluid -->
    </section>
    <!-- Warning Section Starts -->
    <#include "common/IEWarning.ftl">
    <!-- Warning Section Ends -->
    <!-- Required Jquery -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery/js/jquery.min.js"></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery-ui/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/popper.js/js/popper.min.js"></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/bootstrap/js/bootstrap.min.js"></script>
    <!-- waves js -->
    <script src="${staticfile}/files/assets/pages/waves/js/waves.min.js"></script>
    <!-- jquery slimscroll js -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery-slimscroll/js/jquery.slimscroll.js"></script>
    <!-- modernizr js -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/modernizr/js/modernizr.js"></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/modernizr/js/css-scrollbars.js"></script>
    <!-- i18next.min.js -->
    <script type="text/javascript" src="${staticfile}/files/bower_components/i18next/js/i18next.min.js"></script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/i18next-xhr-backend/js/i18nextXHRBackend.min.js">
    </script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/i18next-browser-languagedetector/js/i18nextBrowserLanguageDetector.min.js">
    </script>
    <script type="text/javascript" src="${staticfile}/files/bower_components/jquery-i18next/js/jquery-i18next.min.js"></script>
    <script type="text/javascript" src="${staticfile}/files/assets/js/common-pages.js"></script>
    <!--angularjs-->
    <script src="${staticfile}/files/assets/js/angular.min.js"></script>
    <script src="${staticfile}/files/assets/js/angular-toastr.min.js"></script>
    <script src="${staticfile}/files/assets/js/angular-toastr.tpls.min.js"></script>
    <#--<script src="https://cdn.bootcdn.net/ajax/libs/libs/angular.js/1.5.8/i18n/angular-locale_zh.js"></script>-->
    <script>
        <#include "js/login.js">
        <#include "js/websocket.js">
        <#include "js/localStorage.js">
        <#include "Login.js">
    </script>
</body>
</html>
</#compress>