<@compress single_line=true>
var consoleLog = false;
if(window.localStorage && '1' == window.localStorage.getItem("consoleLog")) {
    consoleLog = true;
};
var LIVEAPP = angular.module('LIVE-APP', ['toastr']);
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
</@compress>