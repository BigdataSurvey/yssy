<nav class="navbar header-navbar pcoded-header">
    <div class="navbar-wrapper">
        <div class="navbar-logo">
            <a class="mobile-menu waves-effect waves-light" id="mobile-collapse" href="javascript:;"> <i class="ti-menu"></i></a>
            <div class="mobile-search waves-effect waves-light">
                <div class="header-search">
                    <div class="main-search morphsearch-search">
                        <div class="input-group">
                            <span class="input-group-prepend search-close"><i
                                    class="ti-close input-group-text"></i></span>
                            <input type="text" class="form-control" placeholder="Enter Keyword">
                            <span class="input-group-append search-btn"><i
                                    class="ti-search input-group-text"></i></span>
                        </div>
                    </div>
                </div>
            </div>
            <!--<a href="/"><img class="img-fluid logo-img" src="${staticfile}/files/assets/images/logo.png" alt="Logo"></a>-->
            <a class="mobile-options waves-effect waves-light"> <i class="ti-more"></i> </a>
        </div>
        <div class="navbar-container container-fluid">
            <ul class="nav-left">
                <li>
                    <div class="sidebar_toggle">
                        <a href="javascript:void(0)"><i class="ti-menu"></i></a>
                    </div>
                </li>
                <li> <a href="javascript:;" onclick="javascript:toggleFullScreen()" class="waves-effect waves-light">
                        <i class="ti-fullscreen"></i> </a> </li>
            </ul>
            <ul class="nav-right">
                <li class=""> <a href="javascript:;" class="waves-effect waves-light" ng-click="dctrl.loginout();"> 
                    <i class="fa fa-sign-out"></i> 登出 </a>
                </li>
            </ul>
        </div>
    </div>
</nav>