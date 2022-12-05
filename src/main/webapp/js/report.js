var jsr = JSON.parse(document.getElementById('jsonScanResult').value);
function drawChart() {
    var data = google.visualization.arrayToDataTable([
        ['Severity', 'Count'],
        ['High (' + jsr.summary.failedStats.high + ')', jsr.summary.failedStats.high],
        ['Medium (' + jsr.summary.failedStats.medium + ')', jsr.summary.failedStats.medium],
        ['Low (' + jsr.summary.failedStats.low + ')', jsr.summary.failedStats.low]
    ]);

    var options = {
        title: 'Qualys IaC Scan Failed Report',
        pieHole: 0.4,
        pieSliceText: 'value',
        slices: {
            0: {
                color: '#dc3912'
            },
            1: {
                color: '#ff9900'
            },
            2: {
                color: '#366eea'
            }
        },
        'chartArea': {'width': '100%', 'height': '80%'},
    };

    var chart = new google.visualization.PieChart(document.getElementById('qualysIaCScanPieChart'));

    chart.draw(data, options);
}
function CreateSpanElement(cssClass, text){
    var spanElement = document.createElement("span");
    spanElement.setAttribute('class', cssClass);
    spanElement.innerText = text;
    return spanElement.outerHTML;
}
function createColElement(colData, resultType, isFailedResultsOnly, applyColor){
    var rowElement = document.createElement("tr");
    if(applyColor){
        var cssClass = (resultType === 'FAILED' && !isFailedResultsOnly ? 'bg-danger-custom' : 'bg-normal')
        rowElement.setAttribute('class', cssClass);
    }
    for(var i = 0; i < colData.length;i++){
        var colElement = document.createElement("td");
        colElement.innerHTML=colData[i];
        rowElement.appendChild(colElement);
    }
    return rowElement;
}
jQuery(document).ready(function () {
    if (jsr && jsr.summary.failedStats && !(jsr.summary.failedStats.high === 0 && jsr.summary.failedStats.medium === 0 && jsr.summary.failedStats.low === 0)) {
        google.charts.load('current', {
            'packages': ['corechart']
        });
        google.charts.setOnLoadCallback(drawChart);
        jQuery('#qualysIaCScanPieChart').removeClass('d-none');
        jQuery('.pie-chart-col').removeClass('d-none');
    } else {
        jQuery('.no-data-qualys-iac-pie-chart').removeClass('d-none');
    }
    jQuery('.icon-success path').attr('fill','#10dc60')
    jQuery('.icon-danger path').attr('fill','#ff8aac')
    var isFailedResultsOnly = (jsr.isFailedResultsOnly == undefined || jsr.isFailedResultsOnly == null ? false : jsr.isFailedResultsOnly);
    jQuery.each(jsr.lstterraFormChecks, function (i, item) {
        var controlId = item.controlId;
        var controlName = item.controlName;
        var criticality = item.criticality;
        var filePath = item.filePath ? item.filePath : '';
        var resource = item.resource;
        var resultType = item.resultType === 'FAILED' ? CreateSpanElement('text-danger fw-bold', 'FAILED') : CreateSpanElement('text-success fw-bold', 'PASSED');
        var rowElement = createColElement([controlId, controlName, criticality, resultType, filePath, resource], item.resultType, isFailedResultsOnly, true);
        jQuery('.iac-posture-rows').append(rowElement);
    });

    jQuery.each(jsr.lstremediation, function (i, item) {
        var controlId = item.controlId;
        var remediation = item.remediation;
        jQuery('.iac-remediation-rows').append(createColElement([controlId, remediation], undefined, undefined, false));
    });

    if (!jsr.lstterraFormChecks || jsr.lstterraFormChecks.length === 0) {
        jQuery('.iacposture-tab').addClass('d-none');
        jQuery('.no-data-iacposture-tab').removeClass('d-none');
    }

    if (!jsr.lstremediation || jsr.lstremediation.length === 0) {
        jQuery('.remediation-tab').addClass('d-none');
        jQuery('.no-data-remediation-tab').removeClass('d-none');
    }

    jQuery.each(jsr.lstParsingErrors, function (i, item) {
        var checkType = item.checkType;
        var parsingErrorLocation = item.parsingErrorLocation;
        jQuery('.parsing-error-rows').append(createColElement([checkType, parsingErrorLocation], undefined, undefined, false));
    });
    if (jQuery('.parsing-error-rows').length > 0) {
        jQuery('.parsing-error-tab').removeClass('d-none');
    }
    if (!jsr.lstParsingErrors || jsr.lstParsingErrors.length === 0) {
        jQuery('.parsing-error-tab').addClass('d-none');
    }
    else {
        jQuery('.parsing-error-tab').removeClass('d-none');
    }
    //Apply css classes to first and second column
    jQuery('#iacposture tbody td:nth-child(1)').attr('class','col-2');
    jQuery('#iacposture tbody td:nth-child(2)').attr('class','col-3');
    jQuery('#remediation tbody td:nth-child(1)').attr('class','col-1');
    jQuery('#remediation tbody td:nth-child(2)').attr('class','col-3');
    jQuery('#iacposture,#remediation,#parsing-error').DataTable();
    if (jsr.summary) {
        jQuery('#scan-id').text(jsr.scanId);
        jQuery('#scan-name').text(jsr.scanName);
        jQuery('#scan-status').text(jsr.scanStatus);
        jQuery('.scan-info-row').removeClass('d-none');
        jQuery('#total-build-failure-control-count').text(jsr.summary.totalBuildFailureControlCount);
        jQuery('#out-of-build-failure-control-count').text(' of ' + jsr.summary.failed);

        if (jsr.appliedBuildSetting && jsr.lstterraFormChecks && jsr.lstterraFormChecks.length > 0) {
            var resURL = jQuery('#resURL').val();
            var cancel_icon = jQuery('.icon-danger').prop("outerHTML");
            var correct_icon = jQuery('.icon-success').prop("outerHTML");
            if (jsr.summary.highViolatesCriteria) {
                jQuery('.high-criteria-icon').html(cancel_icon);
            } else {
                jQuery('.high-criteria-icon').html(correct_icon);
            }
            if (jsr.summary.mediumViolatesCriteria) {
                jQuery('.medium-criteria-icon').html(cancel_icon);
            } else {
                jQuery('.medium-criteria-icon').html(correct_icon);
            }
            if (jsr.summary.lowViolatesCriteria) {
                jQuery('.low-criteria-icon').html(cancel_icon);
            } else {
                jQuery('.low-criteria-icon').html(correct_icon);
            }
        }
        if (jsr.appliedBuildSetting == false) {
          jQuery('#buildFailureCount').addClass('d-none');
        }
    }
});