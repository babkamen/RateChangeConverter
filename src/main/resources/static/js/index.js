const EXCEL_URL = "excel";
const MAX_NUMBER_OF_DOCUMENTS = 100000000;
const FIND_ALL = "rates?sort=date,desc&size=" + MAX_NUMBER_OF_DOCUMENTS;
const FIND_BETWEEN_DATES = "rates/{0}/{1}?sort=date,desc&size=" + MAX_NUMBER_OF_DOCUMENTS;
const DOWNLOAD_EXCHANGE_RATES_URL = "rates/download/{0}/{1}";
const DATE_RANGE_PICKER_SELECTOR = 'input[name="daterange"]';
const DATE_FORMAT = 'YYYY-MM-DD';

var chart;

$(document).ajaxStart(function () {
    Pace.stop();
    Pace.bar.render();
    Pace.go()
});

$(document).ajaxStop(function () {
    Pace.restart();
});


const chartConfig = {
    type: 'line',
    data: {
        datasets: [{
            data: null,
            borderColor: "#3e95cd",
            fill: false
        }
        ]
    },
    options: {
        responsive: true,
        legend: {
            display: false
        },
        title: {
            display: true,
            text: 'USD exchange rate over time',
            fontSize: 15

        },
        tooltips: {
            mode: 'index',
            intersect: false,
        },
        hover: {
            mode: 'nearest',
            intersect: true
        },
        scales: {
            xAxes: [{
                type: 'time',
                position: 'bottom',
                time: {
                    unit: "day",
                    displayFormats: {
                        day: "l",
                    },
                },
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Date'
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Rate'
                }
            }]
        }
    }
};


$(document).ready(function () {
    $(DATE_RANGE_PICKER_SELECTOR).daterangepicker({
        locale: {
            format: DATE_FORMAT
        },
        maxDate: new Date(),
        autoUpdateInput: false,
    });

    $(DATE_RANGE_PICKER_SELECTOR).on('apply.daterangepicker', onDatePickerPick);
    $("#download_exchange").click(onDownloadExchangeClick);

    makeApiCallAndUpdateData();
});


function makeApiCallAndUpdateData(startDate, endDate) {
    var url = FIND_ALL;
    if (startDate && endDate) {
        url = FIND_BETWEEN_DATES.format(startDate, endDate);
    }

    $.get(url).done(function (data) {
        console.log("data", data);
        const content = data.content;
        updateTableAndChart(content);
    }).fail(function (jqXHR, textStatus, errorThrown) {
        alert("Something went wrong");
        console.log(jqXHR, textStatus, errorThrown)
    });
}

function onDatePickerPick(ev, picker) {
    console.log(picker);
    var startDate = picker.startDate.format(DATE_FORMAT);
    var endDate = picker.endDate.format(DATE_FORMAT);
    $(this).val(startDate + ' - ' + endDate);
    console.log("Will update data. Dates=", startDate.toString(), endDate.toString());
    makeApiCallAndUpdateData(startDate, endDate);
}

/**
 * Returns number of occurences of rate property after i
 */
function countSameValues(data, i) {
    console.log(typeof data[0].rate);
    var count = 0;
    for (var r = i; r + 1 < data.length && data[r].rate === data[r + 1].rate; r++, count++) {
        console.log("R,rate1,rate2=", r, data[r].rate, data[r + 1].rate);
    }
    return count;
}

function diffDays(date, endDate) {
    var a = moment(date);
    var b = moment(endDate);
    return a.diff(b, 'days');
}
function handleUnknownValues(content) {
    var data = JSON.parse(JSON.stringify(content));
    for (var i = 0; i < data.length - 1; i++) {
        var o = data[i];
        var date = o.date;
        var rate = o.rate;
        var j = parseInt(i) + 1;
        var endDate = data[j].date;
        var diff = diffDays(date, endDate);
        console.log("Date1=", date, "NextDate=", endDate, "diff=", diff);
        console.log("I,Elem,NextElem=",i, data[i].rate, data[j].rate);
        if (diff > 1) {
            // o.rowspan = diff;
            var dates = enumerateDaysBetweenDatesInDescOrder(date, endDate);
            console.log("dates=", date, endDate, dates);
            var c=i+1;
            for (var t of dates) {
                data.splice(c++, 0, {date: t, rate: rate});
            }
            i += dates.length+1;
        }

    }
    return data;
}

function handleDuplicates(content) {
    var data = JSON.parse(JSON.stringify(content));
    for (var i = 0; i < data.length - 1; i++) {
        var o = data[i];
        var date = o.date;
        var j = parseInt(i) + 1;
        if (data[j].rate === data[i].rate) {
            var count = countSameValues(data, j);
            console.log("Same values=", count);
            if (o.rowspan) {
                o.rowspan = o.rowspan + count + 2;
            } else {
                o.rowspan = count + 2;
            }
            console.log("Rowspan=", o.rowspan);
            for (var r = j; r < j + count + 1; r++) {
                data[r].rate = null;
            }
            console.log("After cleaning=", JSON.stringify(data));

        }


    }
    return data;
}

function updateTableAndChart(content) {
    var data = content;
    if (content.length > 0) {
        data = handleUnknownValues(data);
    }
    updateChart(data);

    if(content.length>0){
        data = handleDuplicates(data);
    }
    updateTable(data);

    console.log("Content=", content);
}

function updateTable(data) {
    console.log("data when creating table=", data);
    var html = "";
    if (data.length > 0) {
        for (var i in data) {
            var o = data[i];
            var date = o.date;
            var rate = o.rate;
            var j = parseInt(i) + 1;
            var rowspan = o.rowspan ? "rowspan=" + o.rowspan + " class='vertical-center'" : "";
            var diff;
            if (i < data.length - 1 && rate) {
                var endDate = data[j].date;
                var a = moment(date);
                var b = moment(endDate);
                diff = a.diff(b, 'days');
                console.log("Date1=", date, "NextDate=", endDate, "diff=", diff);
                if (diff > 1) {
                }
            }
            html += `<tr><td>${date}</td>`;
            if (rate) {
                html += `<td ${rowspan}>${rate}</td>`;
            }
            html += `</tr>`;

        }
    }
    console.log("Table=", html);
    $(".table-body").html(html);
}


function updateChart(data) {
    if (chart) {
        chart.destroy();
    }
    if (data.length == 0)return;
    var chartData = [];
    for (var o of data) {
        var date = o.date;
        var rate = o.rate;
        chartData.push({x: date, y: rate});
    }
    console.log("Chart data=", chartData);
    var ctx = document.getElementById("myChart").getContext('2d');
    ctx.height = 500;
    chartConfig.data.datasets[0].data = chartData;
    chart = new Chart(ctx, chartConfig);
}

function onDownloadExchangeClick() {
    var daterange = $('#daterange');
    if (daterange.val() === "") {
        alert("Please select dates to download data");
        return;
    }

    var data = daterange.data('daterangepicker');
    var startDate = data.startDate.format(DATE_FORMAT);
    var endDate = data.endDate.format(DATE_FORMAT);

    console.log("Picker startDate=", startDate, "endDate=", endDate);
    $.post(DOWNLOAD_EXCHANGE_RATES_URL.format(startDate, endDate)).done(function (data) {
        console.log("Loaded data");
        makeApiCallAndUpdateData(startDate, endDate);
    }).fail(function (response) {
        console.log("response=", response);
        if (response.status = 422 && response.responseJSON.code === 1) {
            alert(response.responseJSON.message);
        } else {
            alert("Something went wrong");
        }
    });
}

$("#download_excel").click(function () {
    console.log("Download excel");
    //convert table data to json
    var data = $("table").tableToJSON();
    console.log("table data", data);
    // Use XMLHttpRequest instead of Jquery $ajax
    xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        var a;
        if (xhttp.readyState === 4 && xhttp.status === 200) {
            //create download link and click
            a = document.createElement('a');
            a.href = window.URL.createObjectURL(xhttp.response);
            a.download = "Rates.xls";
            a.style.display = 'none';
            document.body.appendChild(a);
            a.click();
        }
    };


    xhttp.open("POST", EXCEL_URL);
    xhttp.setRequestHeader("Content-Type", "application/json");
    xhttp.responseType = 'blob';
    xhttp.send(JSON.stringify(data));

});


// First, checks if it isn't implemented yet.
if (!String.prototype.format) {
    String.prototype.format = function () {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function (match, number) {
            return typeof args[number] != 'undefined' ?
                args[number] : match;
        });
    };
}

var enumerateDaysBetweenDatesInDescOrder = function (startDate, endDate) {
    var dates = [];

    var currDate = moment(startDate).startOf('day');
    var lastDate = moment(endDate).startOf('day');

    while (currDate.subtract(1, 'days').diff(lastDate) > 0) {
        console.log(currDate.format(DATE_FORMAT));
        dates.push(currDate.clone().format(DATE_FORMAT));
    }
    return dates;
};
