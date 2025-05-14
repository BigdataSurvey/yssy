<@compress single_line=true>
if (!window.WebSocket) {
    alert('不受支持的浏览器！');
}
/** 模拟Map*/
eval(function(p,a,c,k,e,r){e=function(c){return c.toString(a)};if(!"".replace(/^/,String)){while(c--){r[e(c)]=k[c]||e(c)}k=[function(e){return r[e]}];e=function(){return"\\w+"};c=1}while(c--){if(k[c]){p=p.replace(new RegExp("\\b"+e(c)+"\\b","g"),k[c])}}return p}('4 5(){3.6={}}5.7.r=4(a,b){9{s!=a&&""!=a&&(3.6[a]=b)}8(c){2 c}};5.7.n=4(a){9{2 3.6[a]}8(b){2 b}};5.7.u=4(a){9{f(d b g 3.6)i(3.p==a)2!0;2!1}8(c){2 c}};5.7.m=4(a){9{f(d b g 3.6)i(3.6[b]===a)2!0;2!1}8(c){2 c}};5.7.l=4(a){9{j 3.6[a]}8(b){2 b}};5.7.o=4(){9{j 3.6,3.6={}}8(a){2 a}};5.7.q=4(){2 0==3.e().h?!0:!1};5.7.t=4(){2 3.e().h};5.7.e=4(){d a=[],b;f(b g 3.6)a.k(b);2 a};5.7.v=4(){f(d a=[],b=3.e(),c=0;c<b.h;c++)a.k(3.6[b[c]]);2 a};',32,32,"||return|this|function|Map|container|prototype|catch|try||||var|keyArray|for|in|length|if|delete|push|remove|containsValue|get|clear||isEmpty|put|null|size|containsKey|valueArray".split("|"),0,{}));
window._commandPool = new Map();

/**websocket通信*/
LIVEAPP.service("websocketService", function($rootScope, $filter, toastr) {
    var WS = {
        connected : false,
        _taskPool : new Object(),
        _heartInterval : 5000,
        _reconnectInterval : 1,
        _retryCount : 0,
        _maxRetryCount : 5,/*失败重试次数*/
        _retryDelay : 5000,/*重试N次后在指定的秒数后重新尝试*/
        _showConnectTip : false,
        consoleLog : consoleLog || false,
        protocol : 'https:' == document.location.protocol ? 'wss://' : 'ws://',
        debug : function(e) {
            this.consoleLog && window.console && window.console.log("[ws " + new Date() + "]" + " | " + e);
        },
        warn : function(e) {
            this.consoleLog && window.console && window.console.warn("[ws " + new Date() + "]" + " | " + e);
        },
        _CHARS : '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split(''),
        _startHeartFunction : function() {
            this.debug('start heart data listener...');
            this._heartIntervalFn = setInterval(function() {
                WS.request("0", null, function() {});
            }, this._heartInterval);
        },
        _UUID : function(len, radix) {
            len = len || 32;
            var chars = this._CHARS, uuid = [], i;
            radix = radix || chars.length;
            for (i = 0; i < len; i++)
                uuid[i] = chars[0 | Math.random() * radix];
            return uuid.join('');
        },
        _Utf8Array2Str : function(array) {
            var out, i, len, c;
            var char2, char3;
            out = "";
            len = array.length;
            i = 0;
            while (i < len) {
                c = array[i++];
                switch (c >> 4) {
                    case 0 :
                    case 1 :
                    case 2 :
                    case 3 :
                    case 4 :
                    case 5 :
                    case 6 :
                    case 7 :
                        out += String.fromCharCode(c);
                        break;
                    case 12 :
                    case 13 :
                        char2 = array[i++];
                        out += String.fromCharCode(((c & 0x1F) << 6)
                                | (char2 & 0x3F));
                        break;
                    case 14 :
                        char2 = array[i++];
                        char3 = array[i++];
                        out += String.fromCharCode(((c & 0x0F) << 12)
                                | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
                        break;
                }
            }
            return out;
        },
        request : function(code, data, callback, unrecovery) {
            if (code) {
                var command = new Object;
                command.code = code;
                command.data = data;
                command.id = WS._UUID();
                if (code != '0' && !unrecovery) {
                    window._commandPool.put(command.id, command);
                }
                if (callback) {
                    var task = new Object;
                    task.id = command.id;
                    task.callback = callback;
                    task.create = new Date();
                    this._taskPool[task.id] = task;
                }
                if (this.connected) {
                    this._request(command);
                } else {
                    this.debug('通信失败，Socket未连接');
                    WS.debug("服务器未连接", "通信失败");
                    command.success = false;
                    command.message = '服务器未连接';
                    this._response(command);
                }
            } else {
                WS.warn('命令码为空');
            }
        },
        connect : function(url, handler) {
            if (!this.handler)
                this.handler = handler;
            if (!this.url) {
                this.url = this.protocol + (url || (window.location.host + "/agent"));
            }
            this.debug('尝试连接到 ' + this.url);
            try{
                this.client = new WebSocket(this.url);
                this.client.onerror = this._connectError;
                this.client.onopen = this._connectSuccess;
                this.client.onmessage = this._receivedMessage;
                this.client.onclose = this._connectClose;
            } catch(e) {this.debug(e);}
        },
        _request : function(command) {
            var commandStr = JSON.stringify(command);
            if (command.code != '0'){
                this.debug("request : " + commandStr);
                commandStr = new Blob([commandStr],{type:"text/plain"});
            }else{
                commandStr = new Blob(["0"],{type:"text/plain"});
            }
            this.client.send(commandStr);
        },
        _connectSuccess : function(data) {

        },
        _response : function(command) {
            window._commandPool.remove(command.id);
            if (command.push) {
                this.debug('server push : ' + JSON.stringify(command));
                if (command.code === '0000') {
                    if(window._hasLogin && !command.data.login) {
                        toastr.error('登录超时', '系统提示');
                        setTimeout(function(){
                            window.location.reload();
                        }, 3000);
                    } else {
                        WS.connected = true;
                        WS.debug("connection established");
                        WS._startHeartFunction();
                        if (!WS.hasConnectSuccess)
                            this.handler(command);
                        WS.hasConnectSuccess = true;
                    }
                } else if(command.code == '0004') {
                    toastr.error('登录超时', '系统提示');
                    setTimeout(function(){
                        window.location.reload();
                    }, 3000);
                } else {
                    if (WS.hasConnectSuccess)
                        this.handler(command);
                }
            } else {
                var task = this._taskPool[command.id];
                if (task) {
                    this.debug('response : ' + JSON.stringify(command));
                    delete this._taskPool[command.id];
                    this._refreshDelay(task);
                    task.callback(command);
                }
            }
        },
        _refreshDelay : function(task) {
            this._delay = new Date().getTime() - task.create.getTime();
            this.debug(task.id + " 请求耗时 " + this._delay + " 毫秒");
        },
        _receivedMessage : function(e) {
        	/*var reader = new FileReader();
            reader.onload = function(evt) {
                if (evt.target.readyState == FileReader.DONE) {
                    var command = JSON.parse(WS._Utf8Array2Str(new Uint8Array(evt.target.result)));
                    WS._response(command);
                }
            };
            reader.readAsArrayBuffer(e.data);*/

        	var b = new Blob([e.data]);
        	var r = new FileReader();
    	    r.readAsText(b, 'utf-8');
    	    r.onload = function (){
				if(r.readyState == FileReader.DONE){
					var command = JSON.parse(r.result);
                    WS._response(command);
				}
    	    }
        },
        _connectClose : function() {
            console.log(1)
            WS.debug('connection closed...');
            WS._disconnect();
        },
        _stopConnect : function() {
            console.log(2)
            WS.debug('call disconnect...');
            WS.connected = false;
            WS.autoClose = true;
            WS.socketClient.close();
        },
        _disconnect : function() {
            WS._heartIntervalFn && clearInterval(WS._heartIntervalFn);
            WS.hasConnectSuccess = false;
            if (!WS.connected && !WS.initialed) {
                WS.initialed = true;
                WS.debug('服务器连接失败');
            }
            if (WS.connected) {
                WS.debug('服务器连接中断，重新连接...');
                WS.connected = false;
            }
            if (WS.autoClose)
                return;
            if (WS._retryCount >= WS._maxRetryCount) {
                WS._retryCount = 0;
                setTimeout(function() { WS.connect();}, this._retryDelay);
            } else {
                setTimeout(function() {
                            WS._retryCount += 1;
                            WS.connect();
                        }, this._reconnectInterval);
            }
        }
    };
    return {
        connectServer : function(url, handler) {
            WS.connect(url, handler);
        },
        getNetDelay : function() {
            return WS.connected ? (WS._delay || -1) : -1;
        },
        request : function(code, data, callback, unrecovery) {
            if(!data || !data.withoutMask){
                /*
                window._loadMask && window._loadMask.show();
                window.showMask && cfpLoadingBar.start();
                */
            }
            WS.request(code, data, callback, unrecovery);
        },
        close : function(leave) {
            WS.leave = leave;
            WS._stopConnect();
        },
        serverConnected : function() {
            return WS.connected;
        }
    }
});
</@compress>
