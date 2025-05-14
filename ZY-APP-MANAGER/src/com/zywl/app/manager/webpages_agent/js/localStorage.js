<@compress single_line=true>
/**
 * 本地存储服务
 */
LIVEAPP.service("storageService", function($window) {
    return {
        set : function(key, value, session) {
            if (session)
                $window.sessionStorage.setItem(key, value);
            else
                $window.localStorage.setItem(key, value);
        },
        get : function(key) {
            return $window.localStorage.getItem(key)
                    || $window.sessionStorage.getItem(key);
        },
        remove : function(key) {
            return $window.localStorage.removeItem(key)
                    || $window.sessionStorage.removeItem(key);
        },
        getBoolean : function(key) {
            var value = this.get(key);
            if (value) {
                if (value === 'true')
                    return true;
                else
                    return false;
            } else {
                return false;
            }
        },
        clear : function() {
            $window.localStorage.clear();
            $window.sessionStorage.clear();
        }
    }
});
</@compress>