var TaskTrend = React.createClass ({
    render: function() {
		return(<div className="quickstat-card">
			<div className="quickstat-card-header">{this.props.title}</div>
			<div className="quickstat-card-data text-center">{this.props.number}</div>
		</div>)
    }
});

export default TaskTrend