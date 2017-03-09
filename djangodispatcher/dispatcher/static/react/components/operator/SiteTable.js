import ChartJSChart from './ChartJSChart'

var SiteTable = React.createClass ({

    render: function() {
		var header = [];
		var site_list = [];
		if(this.props.timeChart == undefined){
			return(<div>Nothing</div>)
		}
		// events per day for past 30 days
		// event times
		return (<div>
		<ChartJSChart chartId="timeChart" labels={this.props.timeChart.months}
			 chartType={'bar'} datasetLabel={["Task Time"]}
			 data={[this.props.timeChart.times]} width="6"
			 chartTitle={'Task Time per Month'} xAxisLabel={'Month'}
			 monthChart={false} yAxisLabel={'Time (Hours)'}/>
		<ChartJSChart chartId="volumeChart" labels={this.props.waterHauled.months}
			 chartType={'bar'} datasetLabel={["Water Hauled"]}
			 data={[this.props.waterHauled.volume]} width="6"
			 chartTitle={'Water Hauled Per Month'} xAxisLabel={'Month'}
			 monthChart={false} yAxisLabel={'Volume Hauled'}/>
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