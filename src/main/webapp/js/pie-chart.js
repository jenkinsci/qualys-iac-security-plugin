function drawIacPieChart(containerId, title, data, options) {
    var container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = '';

    var cs = window.getComputedStyle(container);
    var padL = parseFloat(cs.paddingLeft) || 0;
    var padR = parseFloat(cs.paddingRight) || 0;
    var padT = parseFloat(cs.paddingTop) || 0;
    var padB = parseFloat(cs.paddingBottom) || 0;
    var w = container.offsetWidth - padL - padR;
    var h = container.offsetHeight - padT - padB;
    if (w < 100) w = 420;
    if (h < 100) h = 250;

    var canvas = document.createElement('canvas');
    var dpr = window.devicePixelRatio || 1;
    canvas.width = w * dpr;
    canvas.height = h * dpr;
    canvas.style.width = w + 'px';
    canvas.style.height = h + 'px';
    canvas.style.display = 'block';
    canvas.style.margin = '0 auto';
    var ctx = canvas.getContext('2d');
    ctx.scale(dpr, dpr);
    container.appendChild(canvas);

    var titleH = 24;
    var legendH = 28;
    var chartH = h - titleH - legendH;
    var cx = w / 2;
    var cy = titleH + chartH / 2;
    var radius = Math.min(w / 2 - 20, chartH / 2 - 10);
    var innerRadius = radius * (options.pieHole || 0.4);

    var total = 0;
    for (var i = 0; i < data.length; i++) {
        total += data[i].value;
    }

    if (total === 0) {
        ctx.font = '14px Arial';
        ctx.fillStyle = '#666';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('No failed checks', cx, cy);
        return;
    }

    ctx.font = 'bold 14px Arial';
    ctx.fillStyle = '#333';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ctx.fillText(title, cx, 2);

    var startAngle = -Math.PI / 2;
    for (var i = 0; i < data.length; i++) {
        var sliceAngle = (data[i].value / total) * 2 * Math.PI;
        var endAngle = startAngle + sliceAngle;

        ctx.beginPath();
        ctx.arc(cx, cy, radius, startAngle, endAngle);
        ctx.arc(cx, cy, innerRadius, endAngle, startAngle, true);
        ctx.closePath();
        ctx.fillStyle = data[i].color;
        ctx.fill();

        if (data[i].value > 0) {
            var midAngle = startAngle + sliceAngle / 2;
            var labelRadius = (radius + innerRadius) / 2;
            var lx = cx + Math.cos(midAngle) * labelRadius;
            var ly = cy + Math.sin(midAngle) * labelRadius;
            ctx.font = 'bold 13px Arial';
            ctx.fillStyle = '#fff';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(data[i].value, lx, ly);
        }

        startAngle = endAngle;
    }

    var legendY = h - legendH / 2;
    var legendSpacing = 16;
    ctx.font = '12px Arial';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'middle';
    var totalLegendW = 0;
    for (var i = 0; i < data.length; i++) {
        totalLegendW += 14 + ctx.measureText(data[i].label).width + legendSpacing;
    }
    var legendX = (w - totalLegendW + legendSpacing) / 2;
    for (var i = 0; i < data.length; i++) {
        ctx.fillStyle = data[i].color;
        ctx.fillRect(legendX, legendY - 6, 12, 12);
        ctx.fillStyle = '#333';
        ctx.fillText(data[i].label, legendX + 16, legendY);
        legendX += 14 + ctx.measureText(data[i].label).width + legendSpacing;
    }
}
