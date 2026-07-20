document.addEventListener('DOMContentLoaded', function() {
    const dailyChartEl = document.getElementById('dailySalesChart');
    const categoryChartEl = document.getElementById('expenseCategoryChart');
    if (!dailyChartEl && !categoryChartEl) return;

    // Retrieve active branch filter from hidden input
    const branchId = document.getElementById('selected-branch-id')?.value || 0;
    
    // Fetch data from reports API
    fetch(`/api/v1/reports/dashboard-charts?branch_id=${branchId}`)
        .then(res => res.json())
        .then(data => {
            if (dailyChartEl && data.daily_sales) {
                renderDailySalesChart(data.daily_sales);
            }
            if (categoryChartEl && data.expense_categories) {
                renderExpenseCategoryChart(data.expense_categories);
            }
        })
        .catch(err => console.error('Error loading chart data:', err));

    function renderDailySalesChart(salesData) {
        const labels = salesData.map(item => item.date);
        const values = salesData.map(item => item.amount);

        const ctx = dailyChartEl.getContext('2d');
        
        // Gradient fill for line chart
        const gradient = ctx.createLinearGradient(0, 0, 0, 400);
        gradient.addColorStop(0, 'rgba(142, 78, 20, 0.3)');
        gradient.addColorStop(1, 'rgba(142, 78, 20, 0.01)');

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Daily Sales (₹)',
                    data: values,
                    borderColor: '#8e4e14', /* Saffron Gold */
                    backgroundColor: gradient,
                    fill: true,
                    tension: 0.3,
                    borderWidth: 2,
                    pointBackgroundColor: '#8e4e14',
                    pointRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            color: '#7f8c8d'
                        }
                    },
                    y: {
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            color: '#7f8c8d'
                        }
                    }
                }
            }
        });
    }

    function renderExpenseCategoryChart(categoryData) {
        const labels = categoryData.map(item => item.category);
        const values = categoryData.map(item => item.amount);

        // Stitch palette colors
        const chartColors = [
            '#012d1d', '#8e4e14', '#383c40', '#1b4332', 
            '#ffab69', '#ba1a1a', '#a5d0b9', '#ffdcc4',
            '#22262a', '#717973', '#eeeeeb', '#dadad7'
        ];

        const ctx = categoryChartEl.getContext('2d');
        
        // Compute percentages for legend
        const total = values.reduce((sum, val) => sum + parseFloat(val), 0);
        const richLabels = labels.map((label, index) => {
            const val = parseFloat(values[index]);
            const pct = total > 0 ? ((val / total) * 100).toFixed(1) : 0;
            return `${label} - ₹${val.toLocaleString('en-IN')} (${pct}%)`;
        });

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: richLabels,
                datasets: [{
                    data: values,
                    backgroundColor: chartColors.slice(0, labels.length),
                    borderWidth: 1,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            color: '#7f8c8d',
                            boxWidth: 12,
                            font: {
                                size: 11
                            }
                        }
                    }
                },
                cutout: '65%'
            }
        });
    }
});
