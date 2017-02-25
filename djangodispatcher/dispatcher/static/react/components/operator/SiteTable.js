import ChartJSChart from './ChartJSChart'

var SiteTable = React.createClass ({

    render: function() {
		var header = [];
		var site_list = [];
		if(this.props.eventsPerPad == undefined){
			return(<div>Nothing</div>)
		}
		// events per day for past 30 days
		// event times
		return (<div>
		<ChartJSChart chartId="eventsPerTypeChart" labels={this.props.eventsPerPad.labels}
			 chartType={'bar'} datasetLabel={this.props.eventsPerPad.series}
			 data={this.props.eventsPerPad.data} width="6"
			 chartTitle={'Events per Type at Locations'} xAxisLabel={'Location'}
			 monthChart={false} yAxisLabel={'Number of Events'}/>
		<ChartJSChart chartId="timeChart" labels={this.props.timeChart.labels}
			 chartType={'bar'} datasetLabel={this.props.timeChart.series}
			 data={[this.props.timeChart.data]} width="6"
			 chartTitle={'Average Time Based on Task'} xAxisLabel={'Type'}
			 monthChart={false} yAxisLabel={'Time (Hours)'}/>
		</div>);
	    }
});

export default SiteTable

/*
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