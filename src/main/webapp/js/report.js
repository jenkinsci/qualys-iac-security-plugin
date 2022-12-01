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
function createColElement(colData){
    var rowElement = document.createElement("tr");
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
    tmp = '';

    jQuery.each(jsr.lstterraFormChecks, function (i, item) {
        var controlId = item.controlId;
        var controlName = item.controlName;
        var criticality = item.criticality;
        var filePath = item.filePath ? item.filePath : '';
        var resource = item.resource;
        var resultType = item.resultType === 'FAILED' ? '<span class="text-danger fw-bold">FAILED</span>' : '<span class="text-success fw-bold">PASSED</span>';
        jQuery('.iac-posture-rows').append(createColElement([controlId, controlName, criticality, resultType, filePath, resource]));
    });

    jQuery.each(jsr.lstremediation, function (i, item) {
        var controlId = item.controlId;
        var remediation = item.remediation;
        jQuery('.iac-remediation-rows').append(createColElement([controlId, remediation]));
    });

    if (!jsr.lstterraFormChecks || jsr.lstterraFormChecks.length === 0) {
        jQuery('.iacposture-tab').addClass('d-none');
        jQuery('.no-data-iacposture-tab').removeClass('d-none');
    }

    if (!jsr.lstremediation || jsr.lstremediation.length === 0) {
        jQuery('.remediation-tab').addClass('d-none');
        jQuery('.no-data-remediation-tab').removeClass('d-none');
    }

    tmp = '';
    jQuery.each(jsr.lstParsingErrors, function (i, item) {
        var checkType = item.checkType;
        var parsingErrorLocation = item.parsingErrorLocation;
        jQuery('.iac-remediation-rows').append(createColElement([checkType, parsingErrorLocation]));
    });
    if (tmp.trim().length > 0) {
        jQuery('.parsing-error-rows').append(tmp);
        jQuery('.parsing-error-tab').removeClass('d-none');
    }
    if (!jsr.lstParsingErrors || jsr.lstParsingErrors.length === 0) {
        jQuery('.parsing-error-tab').addClass('d-none');
    }
    if (jsr.summary) {
        jQuery('#scan-id').text(jsr.scanId);
        jQuery('#scan-name').text(jsr.scanName);
        jQuery('#scan-status').text(jsr.scanStatus);
        jQuery('.scan-info-row').removeClass('d-none');
        jQuery('#total-build-failure-control-count').text(jsr.summary.totalBuildFailureControlCount);
        jQuery('#out-of-build-failure-control-count').text(' of ' + jsr.summary.failed);

        if (jsr.appliedBuildSetting && jsr.lstterraFormChecks && jsr.lstterraFormChecks.length > 0) {
            var resURL = jQuery('#resURL').val();
            var correct_icon = '<img src="'+ resURL +'/checkmark-circle-outline" plugin-ionicons-api/>';
            var cancel_icon = '<img src="'+ resURL +'/close-circle-outline" plugin-ionicons-api/>';
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
    }
});