<script>
    <#include "js/inner-page.js">
    <#include "test.js">
</script>
<div class="pcoded-content" id="${menuId}_content" data-ng-controller="Controller-${menuId} as ${menuId}">
    <div>
        <donut-chart config="donutConfig" data="dataList.incomeData">
        </donut-chart>
    </div>
    <!--柱状图-->
    <div id="id0001" >
    </div>
    
</div>

