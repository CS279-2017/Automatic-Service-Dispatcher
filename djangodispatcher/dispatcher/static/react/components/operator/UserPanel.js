var UserPanel = React.createClass ({
    render: function() {
    	var task = [];
    	if(this.props.user.activeTask == null){
			task.push(<div key="1" className="card-task-text">Waiting for a Task</div>);
			task.push(<div key="2" className="card-task-location">No Location</div>);
			task.push(<div key="3" className="card-task-time"><span className="glyphicon glyphicon-time" aria-hidden="true"></span> Time</div>);
    	} else {
    		var date = new Date(this.props.user.activeTask.start_date);
    		var now = new Date();
    		//difference found in ms, so
    		var difference = (now-date);
    		var hours = Math.floor(difference/(1000*60*60));
    		var mins = Math.floor((difference%(1000*60*60)/(1000*60)));
    		task.push(<div key="1" className="card-task-text">{this.props.user.activeTask.name}</div>);
    		task.push(<div key="2" className="card-task-location">Pad {this.props.user.activeTask.sensor}</div>);
    		task.push(<div key="3" className="card-task-time"><span className="glyphicon glyphicon-time" aria-hidden="true"></span> 14 hr, 15 min</div>);
    	} //{hours+" hr, "+mins} min
    	//for(var ii=0;ii<this.props.user.activeTasks.length;ii++){
    	//	var myDate = new Date(this.props.user.activeTasks[ii].date);
    	//	tasks.push(<li className="list-group-item" key={ii}>{this.props.user.activeTasks[ii].name} at Sensor {this.props.user.activeTasks[ii].sensor}<br/>
    	//	Requested at: {myDate.toLocaleString()}</li>);
    	//}
		return(<div className="card">
				  <img src={"https://www.gravatar.com/avatar/"+this.props.user.emailHash+"?d=identicon&s=200"} alt="Avatar" style={{width:"100%"}}/>
				  <div className="card-container">
				  <p className="card-image-text">{this.props.user.firstName} {this.props.user.lastName}</p>
					{task}
				  </div>
				</div>)
    }
});

export default UserPanel
