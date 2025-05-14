<nav class="pcoded-navbar" navbar-theme="themelight1" active-item-style="style0">
    <div class="sidebar_toggle">
        <a href="#"><i class="icon-close icons"></i></a>
    </div>
    <div class="pcoded-inner-navbar main-menu">
        <div class="">
            <div class="main-menu-header">
                <img class="img-80 img-radius" src="${staticfile}/files/assets/images/avatar-4.jpg"
                    alt="User-Profile-Image">
                <div class="user-details">
                    <span>{{dctrl.Admin.username}}</span>
                </div>
            </div>
        </div>
        <div class="pcoded-navigation-label">
            Navigation
        </div>
        <ul class="pcoded-item pcoded-left-item">
            <li class="" ng-class="{'pcoded-hasmenu': menu.hasChild}" ng-repeat="menu in dctrl.menuList" ng-init="$last && dctrl.menuFinished()"
                data-id="{{menu.id}}" dropdown-icon="style1" subitem-icon="style4">
                <a href="javascript:;" class="waves-effect waves-dark" ng-click="dctrl.gotoMenu(menu, $event)"> 
                    <span class="pcoded-micon"><i class="{{menu.icon}}"></i><b></b></span>
                    <span class="pcoded-mtext">{{menu.name}}</span> <span class="pcoded-mcaret"></span>
                </a>
                <ul class="pcoded-submenu" ng-if="menu.hasChild">
                    <li class="" ng-repeat="subMenu in menu.children" data-id="{{subMenu.id}}" > 
                        <a href="javascript:;" class="waves-effect waves-dark" ng-click="dctrl.gotoMenu(subMenu, $event)">
                            <span class="pcoded-micon"><i class="ti-angle-right"></i></span>
                            <span class="pcoded-mtext">{{subMenu.name}}</span> 
                            <span class="pcoded-mcaret"></span> 
                        </a>
                    </li> 
                </ul>
            </li>
        </ul>
    </div>
</nav>