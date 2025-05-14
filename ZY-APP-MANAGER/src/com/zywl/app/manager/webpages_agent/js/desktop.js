<@compress single_line=true>
var consoleLog = false;
if(window.localStorage && '1' == window.localStorage.getItem("consoleLog")) {
    consoleLog = true;
};
var controllerProvider = null;
var LIVEAPP = angular.module('LIVE-APP', [
    'ui.router',
    'oc.lazyLoad',
    'toastr'
    ], function($controllerProvider){
        controllerProvider = $controllerProvider;
    });
LIVEAPP.config(function(toastrConfig) {
    angular.extend(toastrConfig, {
        closeButton : false,
        debug : false,
        positionClass : 'toast-top-center',
        onclick : null,
        showDuration : 300,
        hideDuration : 1000,
        timeOut : 6000,
        extendedTimeOut : 1000,
        showEasing : 'swing',
        hideEasing : 'linear',
        showMethod : 'fadeIn',
        hideMethod : 'fadeOut',
        newestOnTop : false,
        closeButton : true
    });
});
angular.module('LIVE-APP')
    .run(['$templateCache', function($templateCache) {
        'use strict';
    }]);
var menuTree = [{id: 'Home', name: '代理数据', icon: 'ti-home'},
    {id: 'Business', name: '业务管理', icon: 'ti-view-grid', hasChild: true, children: [
        {id: 'SubAgentManager', name: '下级管理'}
    ]}
];
window._defualtMenuId = menuTree[0].id;
angular.module('LIVE-APP').constant('menuTree', menuTree);
</@compress>