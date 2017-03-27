import ChartJSChart from './ChartJSChart'

var SiteTable = React.createClass ({

    render: function() {
		var header = [];
		var site_list = [];
		if(this.props.timeChart == undefined){
			return(<div>Nothing</div>)
		}
		console.log(this.props.manuallyScheduled)
		return (<div>
		<ChartJSChart chartId="timeChart" labels={this.props.timeChart.months}
			 chartType={'bar'} datasetLabel={["Task Time"]}
			 data={[this.props.timeChart.times]} width="6"
			 chartTitle={'Task Time per Month'} xAxisLabel={'Month'}
			 monthChart={false} yAxisLabel={'Time (Hours)'} stacked={false}/>
		<ChartJSChart chartId="volumeChart" labels={this.props.waterHauled.months}
			 chartType={'bar'} datasetLabel={["Water Hauled"]}
			 data={[this.props.waterHauled.volume]} width="6"
			 chartTitle={'Water Hauled Per Month'} xAxisLabel={'Month'}
			 monthChart={false} yAxisLabel={'Volume Hauled'} stacked={false}/>
		<ChartJSChart chartId="manualChart" labels={this.props.manuallyScheduled.months}
			 chartType={'bar'} datasetLabel={["Not Manual", "Manual"]}
			 data={[this.props.manuallyScheduled.notManual, this.props.manuallyScheduled.manual]} width="6"
			 chartTitle={'Manual vs Automatic Scheduling'} xAxisLabel={'Month'}
			 monthChart={false} yAxisLabel={'Number Scheduled'} stacked={true}/>
		</div>);
	    }
});

export default SiteTable

/*


<ChartJSChart chartId="leaseChart" labels={this.props.timeChart.months}
                                   chartType={'line'} datasetLabel={["Task Time"]}
                                   data={[this.props.timeChart.times]}
                                   chartTitle={'Task Time per Month'}
                                   monthChart={true} yAxisLabel={'Time'}/>



<div className="panel panel-default">
			  <div className="panel-body">
				<h3 className="panel-title text-center">Sensor Job Data</h3>
			  </div>
			  <table className="table table-bordered">
			  <thead>
			  <tr>{header}</tr>
			  </thead>
			  		<tbody>{site_list}</tbody>
				</table>
			</div>
*/