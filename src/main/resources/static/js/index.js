const EXCEL_URL = "excel";
const MAX_NUMBER_OF_DOCUMENTS=100000000;
const FIND_ALL = "rates?sort=date,desc&size="+MAX_NUMBER_OF_DOCUMENTS;
const FIND_BETWEEN_DATES = "rates/{0}/{1}?sort=date,desc&size="+MAX_NUMBER_OF_DOCUMENTS;
const DOWNLOAD_EXCHANGE_RATES_URL = "rates/download/{0}/{1}";
const DATE_RANGE_PICKER_SELECTOR = 'input[name="daterange"]';
const DATE_FORMAT = 'YYYY-MM-DD';

var chart;

$(document).ajaxStart(function() { Pace.stop();Pace.bar.render();Pace.go() });
$(document).ajaxStop(function() { Pace.restart(); });


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
    }).fail(function () {
        alert("Something went wrong");
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


function updateTableAndChart(content) {
    updateTable(content);
    updateChart(content);
}

function updateTable(data) {
    console.log("data when creating table=", data);
    var html = "";
    if (data.length > 0) {
        for (var i in data) {
            var o = data[i];
            var date = o.date;
            var j = parseInt(i) + 1;
            var rowspan = "";
            var diff;
            if (i < data.length - 1) {
                var endDate = data[j].date;
                var a = moment(date);
                var b = moment(endDate);
                diff = a.diff(b, 'days');
                console.log("Date1=", date, "NextDate=", endDate, "diff=", diff);
                if (diff > 1) {
                    rowspan = "rowspan=" + diff + " class='vertical-center'";
                }
            }
            var tableRow = `<tr>
      <td>` + date + `</td><td ` + rowspan + `>` + o.rate + `</td>
      </tr> `;
            html += tableRow;
            if (rowspan.length > 0) {
                //descending order
                var dates = enumerateDaysBetweenDatesInDescOrder(date, endDate);
                console.log("dates=", date, endDate, dates);
                for (var t of dates) {
                    console.log("Enumdate=", t);
                    html += "<tr><td>" + t + "</td></tr>"
                }
            }
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
