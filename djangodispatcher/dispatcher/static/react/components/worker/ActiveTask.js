var ActiveTask = React.createClass ({
	completeTask: function(evt) {
            var data = {
                taskId: this.props.taskId,
            };
            this.props.completeTask(data);
    },
    render: function() {
		var myDate = new Date(this.props.date);
		return (
			<div className="panel panel-default">
			  <div className="panel-body">
				<h3 className="panel-title text-center">ActiveTask</h3>
			  </div>
			  <table className="table table-bordered">
			  <thead>
			  <tr>
					<th>Task</th>
					<th>Sensor</th>
					<th>Date</th>
					<th>Complete</th>
				</tr>
			  </thead>
			  		<tbody>
					   <tr className="text-center">
						<td>{this.props.name}</td>
						<td>{this.props.sensor}</td>
						<td>{myDate.toLocaleString()}</td>
						<td><button className="btn btn-success btn-xs" onClick={this.completeTask}>Complete</button></td>
						</tr>
					   </tbody>
				</table>
			</div>
		);
	    }
});

export default ActiveTask