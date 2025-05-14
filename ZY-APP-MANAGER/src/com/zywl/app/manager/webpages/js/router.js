<@compress single_line=true>
LIVEAPP.config(function($stateProvider, $urlRouterProvider, menuTree){
    $stateProvider.state ('LIVE-APP', {
        template:"<div class='container-content' ui-view></div>"
    });
    var menuList = menuTree || [];
    var stateMap = {};
    function loadMenu(menus) {
        if(menus && menus.length > 0) {
            for(var i = 0 ; i < menus.length; i++) {
                var menuItem = menus[i];
                if(menuItem.hasChild) {
                    loadMenu(menuItem.children);
                } else {
                    stateMap[menuItem.id] = getState(menuItem);
                }
            }
        }
    };
    function getState(menuItem) {
        return {
            params: {params:null},
            templateUrl: function($stateParams) {
                return './Modal?target=' + menuItem.id;
            },
            resolve: {
                des: function ($ocLazyLoad) {
                    var fileArray = [];
                    if (menuItem.cdnJs) {
                        fileArray = fileArray.concat(menuItem.cdnJs.split(';'));
                    }
                    if (menuItem.css) {
                        fileArray = fileArray.concat(menuItem.css.split(';'));
                    }
                    if(fileArray.length > 0) {
                        var ctrl = $ocLazyLoad.load({
                            files: fileArray
                        });
                        if (ctrl.$$state.status == 2)
                            return undefined;
                        return ctrl;
                    } else {
                        return null;
                    }
                }
            }
        };
    };
    loadMenu(menuList);
    renderState();
    function renderState() {
        angular.forEach(Object.keys(stateMap), function(key){
            $stateProvider.state('LIVE-APP.' + key, stateMap[key]);
        });
    };
});
</@compress>