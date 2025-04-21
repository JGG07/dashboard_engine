function toggleMenu() {
    const menu = document.getElementById('menu');
    const hamburger = document.querySelector('.hamburger');
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
    hamburger.classList.toggle('active');
}

const series = window.series || [];  // Si `series` no está definida, asigna un array vacío
const fechas = window.fechas || [];

const seriesCapitalized = series.map(serie => ({
    ...serie,
    name: capitalizeWords(serie.name)
}));

function capitalizeWords(str) {
    return str
        .toLowerCase()
        .split(' ')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
}

document.addEventListener('DOMContentLoaded', function () {

    // Inicializar la segunda gráfica
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
            categories: dealsDates,
            labels: {
                rotation: -90,
                style: {
                    color: '#FFFFFF'
                }
            }
        },
        yAxis: {
            title: {
                text: 'Número de Tratos',
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
        series: [{
            name: 'Tratos Totales',
            data: dealsCounts
        }, {
            name: 'Interesados',
            data: interesados
        }, {
            name: 'Contactados',
            data: contactados
        }, {
            name: 'Citas',
            data: citas
        }, {
            name: 'Visitas',
            data: visitas
        }, {
            name: 'Negociaciones',
            data: negociaciones
        }, {
            name: 'Apartados',
            data: apartados
        }]
    });

    // Inicializar la gráfica de pastel
    var pieData = Object.keys(sortedLostReasons).map(function(reason) {
        return {
            name: reason,
            y: sortedLostReasons[reason]
        };
    });

    Highcharts.chart('pie-container', {
        chart: {
            type: 'pie',
            backgroundColor: '#1F1D1C'
        },
        title: {
            text: '',
            style: {
                color: '#FFFFFF'
            }
        },
        tooltip: {
            pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>',
            style: {
                color: '#FFFFFF'
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: '#FFFFFF'
                    }
                }
            }
        },
        series: [{
            name: 'Razón de Pérdida',
            colorByPoint: true,
            data: pieData,
            colors: ['#FF6347', '#4682B4', '#32CD32', '#FFD700', '#FF69B4', '#8A2BE2', '#5F9EDC', '#D2691E', '#FF4500', '#6A5ACD']
        }]
    });

    // Inicializar la gráfica de barras
    Highcharts.chart('container', {
        chart: {
            type: 'bar',
            backgroundColor: '#1F1D1C'
        },
        title: {
            text: '',
            style: {
                color: '#FFFFFF'
            }
        },
        xAxis: {
            categories: ['Interesado', 'Contactado', 'Cita', 'Visita', 'Negociación', 'Apartado'],
            title: {
                text: null
            },
            labels: {
                style: {
                    color: '#FFFFFF'
                }
            }
        },
        yAxis: {
            min: 0,
            title: {
                text: '',
                align: 'high'
            },
            labels: {
                style: {
                    color: '#FFFFFF'
                }
            }
        },
        tooltip: {
            valueSuffix: '',
            backgroundColor: '#1F1D1C',
            borderColor: '#FFFFFF',
            style: {
                color: '#FFFFFF'
            }
        },
        plotOptions: {
            bar: {
                borderRadius: '50%',
                dataLabels: {
                    enabled: true,
                    color: '#FFFFFF',
                    style: {
                        fontFamily: '"Arial", monospace'
                    }
                },
                groupPadding: 0.1
            }
        },
        series: [{
            name: 'Abiertos: ' + totalOpenDeals,
            data: openDeals,
            color: '#7cb5ec'
        }, {
            name: 'Perdidos: ' + totalLostDeals,
            data: lostDeals,
            color: '#ff4040'
        }, {
            name: 'Ganados: ' + totalWonDeals,
            data: wonDeals,
            color: '#90ed7d'
        }],
        exporting: {
            enabled: true,
            buttons: {
                contextButton: {
                    align: 'right',
                    verticalAlign: 'top',
                    x: 0,
                    y: 352
                }
            }
        }
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
        defaultDate: [
            /* Manejar nulos adecuadamente */
            /* Si las variables son nulas, simplemente no pasamos valor al campo */
            "${startDate != null ? startDate : ''}",
            "${endDate != null ? endDate : ''}"
        ],
        position: "below",
    });
});


function exportTableToCSV(filename = 'tabla-leads.csv') {
    const table = document.querySelector('.source-table');
    const rows = Array.from(table.querySelectorAll('tr'));
    const csvRows = [];

    // Recorre cada fila de la tabla
    rows.forEach(row => {
        const cells = Array.from(row.querySelectorAll('th, td'));
        const rowValues = cells.map(cell => `"${cell.innerText.replace(/"/g, '""')}"`); // Escapa comillas dobles
        csvRows.push(rowValues.join(',')); // Unir celdas de la fila
    });

    // Crear el archivo Blob con los datos CSV
    const csvString = csvRows.join('\n');
    const blob = new Blob([csvString], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);

    // Crear un enlace temporal para descargar el archivo
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();

    // Limpiar
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Asocia el script a un botón de exportación
document.getElementById('botonExportarCSV').addEventListener('click', () => exportTableToCSV());



function exportTableToCSV(filename = 'tabla-razon-perdida.csv') {
    const table = document.querySelector('.source-table-reason');
    const rows = Array.from(table.querySelectorAll('tr'));
    const csvRows = [];

    // Recorre cada fila de la tabla
    rows.forEach(row => {
        const cells = Array.from(row.querySelectorAll('th, td'));
        const rowValues = cells.map(cell => `"${cell.innerText.replace(/"/g, '""')}"`); // Escapa comillas dobles
        csvRows.push(rowValues.join(',')); // Unir celdas de la fila
    });

    // Crear el archivo Blob con los datos CSV
    const csvString = csvRows.join('\n');
    const blob = new Blob([csvString], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);

    // Crear un enlace temporal para descargar el archivo
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();

    // Limpiar
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Asocia el script a un botón de exportación
document.getElementById('botonExportarTablaReason').addEventListener('click', () => exportTableToCSV());

function exportTableToCSV(filename = 'campaign-table.csv') {
    const table = document.querySelector('.campaign-table');
    const rows = Array.from(table.querySelectorAll('tr'));
    const csvRows = [];

    // Recorre cada fila de la tabla
    rows.forEach(row => {
        const cells = Array.from(row.querySelectorAll('th, td'));
        const rowValues = cells.map(cell => `"${cell.innerText.replace(/"/g, '""')}"`); // Escapa comillas dobles
        csvRows.push(rowValues.join(',')); // Unir celdas de la fila
    });

    // Crear el archivo Blob con los datos CSV
    const csvString = csvRows.join('\n');
    const blob = new Blob([csvString], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);

    // Crear un enlace temporal para descargar el archivo
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();

    // Limpiar
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Asocia el script a un botón de exportación
document.getElementById('customButtonCampaign').addEventListener('click', () => exportTableToCSV());