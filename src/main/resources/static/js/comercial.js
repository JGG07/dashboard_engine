function toggleMenu() {
    const menu = document.getElementById('menu');
    const hamburger = document.querySelector('.hamburger');
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
    hamburger.classList.toggle('active');
}

document.addEventListener('DOMContentLoaded', function () {
    Highcharts.chart('chart-container', {
        chart: {
            backgroundColor: '#1F1D1C',
            type: 'line'
        },
        title: {
            text: '',
            style: {
                color: '#FFFFFF'
            }
        },
        xAxis: {
            categories: fechas, // Array de fechas
            labels: {
                rotation: -90,
                style: {
                    color: '#FFFFFF'
                }
            }
        },
        yAxis: {
            title: {
                text: 'Actividades',
                style: {
                    color: '#FFFFFF'
                }
            },
            labels: {
                style: {
                    color: '#FFFFFF'
                }
            }
        },
        legend: {
            itemStyle: {
                color: '#FFFFFF'
            }
        },
        plotOptions: {
            line: {
                dataLabels: {
                    enabled: true,
                    style: {
                        color: '#FFFFFF'
                    }
                },
                enableMouseTracking: true
            }
        },
        series: series // Array de objetos, cada uno representando a un asesor
    });
});





// Toggle para el dropdown de Fuente
function toggleSourceDropdown() {
    var dropdown = document.getElementById("source-dropdown");
    dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
}

document.addEventListener("click", function(event) {
    var dropdown = document.getElementById("source-dropdown");
    if (!event.target.closest(".custom-select-source")) {
        dropdown.style.display = "none";
    }
});

// Toggle para el dropdown de Asesor
function toggleAsesorDropdown() {
    var dropdown = document.getElementById("asesor-dropdown");
    dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
}

document.addEventListener("click", function(event) {
    var dropdown = document.getElementById("asesor-dropdown");
    if (!event.target.closest(".custom-select-asesor")) {
        dropdown.style.display = "none";
    }
});




document.addEventListener('DOMContentLoaded', function() {
    flatpickr("#dateRange", {
        mode: "range",
        dateFormat: "Y-m-d",
        defaultDate: ["${startDate}", "${endDate}"],
        position: "below", /* Asegura que el calendario se posicione debajo del campo de entrada */
    });
});


