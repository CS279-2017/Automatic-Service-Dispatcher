var ChartJSChart = React.createClass({
    componentWillReceiveProps: function(newProps){
        if(newProps.labels != undefined && newProps.data != undefined){
            var ctxMyChart = document.getElementById(newProps.chartId);
            var light_colors = ["rgba(73, 246, 4,0.31)", "rgba(255, 141,12,0.31)", "rgba(18, 65, 201,0.31)", "rgba(163, 0, 0,0.31)", "rgba(101, 222, 241,0.31)"];
            var med_colors = ["rgba(73, 246, 4,0.7)", "rgba(255, 141,12,0.7)", "rgba(18, 65, 201,0.7)", "rgba(163, 0, 0,0.7)", "rgba(101, 222, 241,0.7)"];
            var dark_colors = ["rgba(73, 246, 4,1)", "rgba(255, 141,12, 1)", "rgba(18, 65, 201, 1)", "rgba(163, 0, 0, 1)", "rgba(101, 222, 241, 1)"];
            var chartObj = {
                type: newProps.chartType,
                data: {labels: newProps.labels,
                    datasets:[]},
                options: {
                    title: {
                        display: true,
                        text: newProps.chartTitle,
                    },
                    legend: {
                        display: true,
                        labels: {
                            fontColor: 'rgb(0,0,0)'
                        },
                        position: 'bottom'
                    },
                    scales: {
                        yAxes: [{
                            scaleLabel: {
                                display: true,
                                labelString: newProps.yAxisLabel
                            }
                        }],
                        xAxis : []
                    }
                }
            };
            for(var ii = 0; ii<newProps.data.length; ii++){
                chartObj.data.datasets.push({
                    label: newProps.datasetLabel[ii],
                    fill: true,
                    lineTension: .01,
                    backgroundColor: light_colors[ii],
                    borderColor: med_colors[ii],
                    borderWidth: 1,
                    pointBorderColor: med_colors[ii],
                    pointBackgroundColor: med_colors[ii],
                    pointHoverBackgroundColor: dark_colors[ii],
                    pointHoverBorderColor: dark_colors[ii],
                    pointBorderWidth: 1,
                    pointHoverRadius: 3,
                    pointRadius: 1,
                    pointHitRadius: 1,
                    data: newProps.data[ii],
                });
            }
            if(newProps.monthChart){
                chartObj.options.scales.xAxes = [{
                    type: 'time',
                    time: {
                        unit: 'month'
                    }
                }]
            } else {
                chartObj.options.scales.xAxes = [{
                    scaleLabel: {
                        display: true,
                        labelString: newProps.xAxisLabel
                    }
                }];
                chartObj.options.scales.yAxes[0].ticks={
                    suggestedMin: 0,    // minimum will be 0, unless there is a lower value.
                    // OR //
                    beginAtZero: true   // minimum value will be 0.
                };
            }
            var myProblemWorkordersChart = new Chart(ctxMyChart, chartObj);
        }
    },
    componentDidMount : function(){
        if(this.props.labels != undefined && this.props.data != undefined){
            var ctxMyChart = document.getElementById(this.props.chartId);
            var light_colors = ["rgba(73, 246, 4,0.31)", "rgba(255, 141,12,0.31)", "rgba(18, 65, 201,0.31)", "rgba(163, 0, 0,0.31)", "rgba(101, 222, 241,0.31)"];
            var med_colors = ["rgba(73, 246, 4,0.7)", "rgba(255, 141,12,0.7)", "rgba(18, 65, 201,0.7)", "rgba(163, 0, 0,0.7)", "rgba(101, 222, 241,0.7)"];
            var dark_colors = ["rgba(73, 246, 4,1)", "rgba(255, 141,12, 1)", "rgba(18, 65, 201, 1)", "rgba(163, 0, 0, 1)", "rgba(101, 222, 241, 1)"];
            var chartObj = {
                type: this.props.chartType,
                data: {labels: this.props.labels,
                    datasets:[]},
                options: {
                    title: {
                        display: true,
                        text: this.props.chartTitle,
                    },
                    legend: {
                        display: true,
                        labels: {
                            fontColor: 'rgb(0,0,0)'
                        },
                        position: 'bottom'
                    },
                    scales: {
                        yAxes: [{
                            scaleLabel: {
                                display: true,
                                labelString: this.props.yAxisLabel
                            }
                        }],
                        xAxis : []
                    }
                }
            };
            for(var ii = 0; ii<this.props.data.length; ii++){
                chartObj.data.datasets.push({
                    label: this.props.datasetLabel[ii],
                    fill: true,
                    lineTension: .01,
                    backgroundColor: light_colors[ii],
                    borderColor: med_colors[ii],
                    borderWidth: 1,
                    pointBorderColor: med_colors[ii],
                    pointBackgroundColor: med_colors[ii],
                    pointHoverBackgroundColor: dark_colors[ii],
                    pointHoverBorderColor: dark_colors[ii],
                    pointBorderWidth: 1,
                    pointHoverRadius: 3,
                    pointRadius: 1,
                    pointHitRadius: 1,
                    data: this.props.data[ii],
                });
            }
            if(this.props.monthChart){
                chartObj.options.scales.xAxes = [{
                    type: 'time',
                    time: {
                        unit: 'month'
                    }
                }]
            } else {
                chartObj.options.scales.xAxes = [{
                    scaleLabel: {
                        display: true,
                        labelString: this.props.xAxisLabel
                    }
                }];
                chartObj.options.scales.yAxes[0].ticks={
                    suggestedMin: 0,    // minimum will be 0, unless there is a lower value.
                    // OR //
                    beginAtZero: true   // minimum value will be 0.
                };
            }
            var myProblemWorkordersChart = new Chart(ctxMyChart, chartObj);
        }
    },
    render: function() {
        return(<div className={"col-md-"+this.props.width}>
                    <canvas id={this.props.chartId}/>
                </div>);
    }
});

export default ChartJSChart