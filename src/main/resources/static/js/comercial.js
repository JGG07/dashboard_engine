function toggleMenu() {
    const menu = document.getElementById('menu');
    const hamburger = document.querySelector('.hamburger');
    menu.style.display = menu.style.display === 'flex' ? 'none' : 'flex';
    hamburger.classList.toggle('active');
}

document.addEventListener('DOMContentLoaded', function () {
    Highcharts.chart('chart-container-comercial', {
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

function exportTableToCSV(filename) {
    const table = document.querySelector(filename);
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
document.getElementById('botonExportarTablaAsesores').addEventListener('click', () => exportTableToCSV('.leads-table'));
// Asocia el script a un botón de exportación
document.getElementById('botonExportarTablaAsesoresConversion').addEventListener('click', () => exportTableToCSV('.leads-campaign-table'));
// Asocia el script a un botón de exportación
document.getElementById('botonExportarTablaFuente').addEventListener('click', () => exportTableToCSV('.source-table-reason'));
// Asocia el script a un botón de exportación
document.getElementById('botonExportarTablaFuenteConversion').addEventListener('click', () => exportTableToCSV('.asesores-table-conversion'));

function filtrarPorAnio() {
    const anioSeleccionado = document.getElementById("anioSelector").value;

    // Filtrar filas de campañas
    const filas = document.querySelectorAll(".camping-wins tbody tr");
    filas.forEach(fila => {
        const anioFila = fila.getAttribute("data-anio");
        if (anioFila === anioSeleccionado) {
            fila.style.display = "";
        } else {
            fila.style.display = "none";
        }
    });

    // Filtrar filas de totales
    const totales = document.querySelectorAll(".camping-wins tfoot tr");
    totales.forEach(fila => {
        const anioFila = fila.getAttribute("data-anio");
        if (anioFila === anioSeleccionado) {
            fila.style.display = "";
        } else {
            fila.style.display = "none";
        }
    });
}


document.addEventListener("DOMContentLoaded", filtrarPorAnio);
//window.onload = filtrarPorAnio;
