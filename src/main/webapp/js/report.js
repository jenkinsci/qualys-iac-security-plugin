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
$(document).ready(function () {
    if (jsr && jsr.summary.failedStats && !(jsr.summary.failedStats.high === 0 && jsr.summary.failedStats.medium === 0 && jsr.summary.failedStats.low === 0)) {
        google.charts.load('current', {
            'packages': ['corechart']
        });
        google.charts.setOnLoadCallback(drawChart);
        $('#qualysIaCScanPieChart').removeClass('d-none');
        $('.pie-chart-col').removeClass('d-none');
    } else {
        $('.no-data-qualys-iac-pie-chart').removeClass('d-none');
    }

    tmp = '';
    $.each(jsr.lstterraFormChecks, function (i, item) {
        var controlId = item.controlId;
        var controlName = item.controlName;
        var criticality = item.criticality;
        var filePath = item.filePath ? item.filePath : '';
        var resource = item.resource;
        var resultType = item.resultType === 'FAILED' ? '<span class="text-danger fw-bold">FAILED</span>' : '<span class="text-success fw-bold">PASSED</span>';
        tmp = tmp + "<tr><td class='col-1'>" + controlId + "</td><td>" + controlName + "</td><td>" + criticality + "</td><td>" + resultType + "</td><td>" + filePath + "</td><td> " + resource + " </td></tr>";
    });
    if (tmp.trim().length > 0) {
        $('.iac-posture-rows').append(tmp);
    }
    tmp = '';
    $.each(jsr.lstremediation, function (i, item) {
        var controlId = item.controlId;
        var remediation = item.remediation;
        tmp = tmp + "<tr><td class='col-1'>" + controlId + "</td><td>" + remediation + "</td></tr>";
    });
    if (tmp.trim().length > 0) {
        $('.iac-remediation-rows').append(tmp);
    }
    if (!jsr.lstterraFormChecks || jsr.lstterraFormChecks.length === 0) {
        $('.iacposture-tab').addClass('d-none');
        $('.no-data-iacposture-tab').removeClass('d-none');
    }

    if (!jsr.lstremediation || jsr.lstremediation.length === 0) {
        $('.remediation-tab').addClass('d-none');
        $('.no-data-remediation-tab').removeClass('d-none');
    }

    tmp = '';
    $.each(jsr.lstParsingErrors, function (i, item) {
        var checkType = item.checkType;
        var parsingErrorLocation = item.parsingErrorLocation;
        tmp = tmp + "<tr><td class='col-1'>" + checkType + "</td><td>" + parsingErrorLocation + "</td></tr>";
    });
    if (tmp.trim().length > 0) {
        $('.parsing-error-rows').append(tmp);
        $('.parsing-error-tab').removeClass('d-none');
    }
    if (!jsr.lstParsingErrors || jsr.lstParsingErrors.length === 0) {
        $('.parsing-error-tab').addClass('d-none');
    }
    if (jsr.summary) {
        $('#scan-id').text(jsr.scanId);
        $('#scan-name').text(jsr.scanName);
        $('#scan-status').text(jsr.scanStatus);
        $('.scan-info-row').removeClass('d-none');
        $('#total-build-failure-control-count').text(jsr.summary.totalBuildFailureControlCount);
        $('#out-of-build-failure-control-count').text(' of ' + jsr.summary.failed);

        if (jsr.appliedBuildSetting && jsr.lstterraFormChecks && jsr.lstterraFormChecks.length > 0) {
            var resURL = $('#resURL').val();
            var cancel_icon = '<img height="24" width="24" src="' + resURL + '/plugin/qualys-iac-security/icons/cancel-icon.png"/>';
            var correct_icon = '<img height="24" width="24" src="' + resURL + '/plugin/qualys-iac-security/icons/correct.png"/>';
            if (jsr.summary.highViolatesCriteria) {
                $('.high-criteria-icon').html(cancel_icon);
            } else {
                $('.high-criteria-icon').html(correct_icon);
            }
            if (jsr.summary.mediumViolatesCriteria) {
                $('.medium-criteria-icon').html(cancel_icon);
            } else {
                $('.medium-criteria-icon').html(correct_icon);
            }
            if (jsr.summary.lowViolatesCriteria) {
                $('.low-criteria-icon').html(cancel_icon);
            } else {
                $('.low-criteria-icon').html(correct_icon);
            }
        }
    }
});