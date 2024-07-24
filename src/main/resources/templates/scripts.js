document.addEventListener('DOMContentLoaded', function () {
    Highcharts.chart('chart-container', {
        chart: {
            type: 'line'
        },
        title: {
            text: ''
        },
        xAxis: {
            categories: dealsDates
        },
        yAxis: {
            title: {
                text: 'Número de Tratos'
            }
        },
        plotOptions: {
            line: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: true
            },
        },
        series: [{
            name: 'Tratos Totales',
            data: dealsCounts
        },{
            name: 'Contactados',
            data: contactados
        },{
            name: 'Interesados',
            data: interesados
        },{
            name: 'Citas',
            data: citas
        },{
            name: 'Visitas',
            data: visitas
        },{
            name: 'Negociaciones',
            data: negociaciones
        },{
            name: 'Apartados',
            data: apartados
        }]
    });
});

document.addEventListener('DOMContentLoaded', function() {
    const table = document.querySelector('.source-table tbody');
    const rows = table.querySelectorAll('tr');

    // Definir los índices de las columnas para los datos
    const columns = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    const maxValues = {};

    // Encontrar los valores máximos para cada columna
    columns.forEach(colIndex => {
        maxValues[colIndex] = 0;
        rows.forEach(row => {
            const cell = row.cells[colIndex];
            if (cell) {
                const value = parseInt(cell.dataset.value) || 0;
                if (value > maxValues[colIndex]) {
                    maxValues[colIndex] = value;
                }
            }
        });
    });

    // Aplicar colores basados en la intensidad
    rows.forEach(row => {
        columns.forEach(colIndex => {
            const cell = row.cells[colIndex];
            if (cell) {
                const value = parseInt(cell.dataset.value) || 0;
                const maxValue = maxValues[colIndex];
                const intensity = maxValue ? value / maxValue : 0;
                const color = `rgba(0, 0, 255, ${intensity})`; // Color azul con opacidad variable
                cell.style.backgroundColor = color;
            }
        });
    });
});
