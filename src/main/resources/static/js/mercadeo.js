function toggleMenu() {
    const menu = document.getElementById('menu');
    const hamburger = document.querySelector('.hamburger');
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
    hamburger.classList.toggle('active');
}

document.addEventListener('DOMContentLoaded', function() {
    chart = Highcharts.chart('chart-container', {
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
                rotation: -90, // Opcional para evitar superposición
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
});

document.addEventListener('DOMContentLoaded', function() {
    // Variables Thymeleaf convertidas en formato JavaScript


    // Preparar datos para la gráfica de pastel
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
                color: '#FFFFFF' // Color del título
            }
        },
        tooltip: {
            pointFormat: '{point.name}: <b>{point.percentage:.1f}%</b>',
            style: {
                color: '#FFFFFF' // Color del texto del tooltip
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true, // Habilita las etiquetas de datos
                    format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                    style: {
                        color: '#FFFFFF' // Color del texto de los data labels
                    }
                }
            }
        },
        series: [{
            name: 'Razón de Pérdida',
            colorByPoint: true,
            data: pieData,
            colors: ['#FF6347', '#4682B4', '#32CD32', '#FFD700', '#FF69B4', '#8A2BE2', '#5F9EDC', '#D2691E', '#FF4500', '#6A5ACD'] // Colores personalizados para cada segmento
        }]
    });
});

document.addEventListener('DOMContentLoaded', function() {

    Highcharts.chart('container', {
        chart: {
            type: 'bar',
            backgroundColor: '#1F1D1C' // Color de fondo del gráfico
        },
        title: {
            text: '',
            align: 'left',
            style: {
                color: '#FFFFFF', // Color del texto del título
                fontFamily: '"Arial", monospace' // Aplica la fuente
            }
        },
        subtitle: {
            text: '',
            align: 'left',
            style: {
                color: '#FFFFFF', // Color del texto del subtítulo
                fontFamily: '"Arial", monospace' // Aplica la fuente
            }
        },
        xAxis: {
            categories: ['Interesado', 'Contactado', 'Cita', 'Visita', 'Negociación', 'Apartado'],
            title: {
                text: null
            },
            labels: {
                style: {
                    color: '#FFFFFF', // Color del texto de las etiquetas del eje X
                    fontFamily: '"Arial", monospace' // Aplica la fuente
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
                    color: '#FFFFFF', // Color del texto de las etiquetas del eje Y
                    fontFamily: '"Arial", monospace' // Aplica la fuente
                }
            }
        },
        tooltip: {
            valueSuffix: '',
            backgroundColor: '#1F1D1C', // Color de fondo del tooltip
            borderColor: '#FFFFFF', // Color del borde del tooltip
            style: {
                color: '#FFFFFF', // Color del texto del tooltip
                fontFamily: '"Arial", monospace' // Aplica la fuente
            }
        },
        plotOptions: {
            bar: {
                borderRadius: '50%',
                dataLabels: {
                    enabled: true,
                    color: '#FFFFFF', // Color del texto de las etiquetas de datos
                    style: {
                        fontFamily: '"Arial", monospace' // Aplica la fuente
                    }
                },
                groupPadding: 0.1
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'top',
            x: -5,
            y: 5,
            floating: false,
            borderWidth: 1,
            backgroundColor: '#1F1D1C',
            itemStyle: {
                color: '#FFFFFF', // Color del texto de la leyenda
                fontFamily: '"Arial", monospace' // Aplica la fuente
            }
        },
        credits: {
            enabled: false
        },
        series: [{
            name: 'Abiertos: ' + totalOpenDeals,
            data: openDeals,
            color: '#7cb5ec' // Color para la serie 'Abiertos'
        }, {
            name: 'Perdidos: ' + totalLostDeals,
            data: lostDeals,
            color: '#ff4040' // Color para la serie 'Perdidos'
        }, {
            name: 'Ganados: ' + totalWonDeals,
            data: wonDeals,
            color: '#90ed7d' // Color para la serie 'Ganados'
        }],
        exporting: {
            enabled: true,
            buttons: {
                contextButton: {
                    align: 'right',      // Alineación horizontal (left, center, right)
                    verticalAlign: 'top',  // Alineación vertical (top, middle, bottom)
                    x: 0,               // Ajuste en píxeles desde el borde especificado (horizontal)
                    y: 352                // Ajuste en píxeles desde el borde especificado (vertical)
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
