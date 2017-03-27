var TaskTrend = React.createClass ({
    render: function() {
		return(<div className="quickstat-card">
			<div className="quickstat-card-header">{this.props.title}</div>
			<div className="quickstat-card-data text-center">{Math.round(100*this.props.number)/100}</div>
		</div>)
    }
});

export default TaskTrend