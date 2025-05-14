<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, $stateParams, datatableUtil, alertUtil, imageUtil, storageService, websocketService, toastr){

        //柱状图数据
        function initConfigChart() {
            var parkaccountChart = echarts.init(document.getElementById('id0001'));//div 标签id
            var seriesLabel = {
                normal: {
                    show: true,
                    textBorderColor: '#333',
                    textBorderWidth: 2
                }
            };
            var option = {
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                legend: {
                    data: ['总数1', '总数2', '总数3'],
                    bottom:true
                },
                grid: {
                    left: '1%',
                    right: '4%',
                    bottom: '8%',
                    top:'5%',
                    containLabel: true
                },
                xAxis: {
                    type: 'value',
                    name: '',
                    axisLabel: {
                        formatter: '{value}'
                    }
                },
                yAxis: {
                    type: 'category',
                    inverse: true,
                    data: ['y1', 'y2', 'y3']
                },
                series: [
                    {
                        name: '总数1',
                        type: 'bar',
                        label: seriesLabel,
                        data: [165, 170, 330]
                    },
                    {
                        name: '总数2',
                        type: 'bar',
                        label: seriesLabel,
                        data: [150, 105, 110]
                    },
                    {
                        name: '总数3',
                        type: 'bar',
                        label: seriesLabel,
                        data: [220, 82, 63]
                    }
                ]
            };
            parkaccountChart.setOption(option);
        }
        function init() {
            initConfigChart();
        }
        init();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>