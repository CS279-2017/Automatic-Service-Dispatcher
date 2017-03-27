var ManualTaskModal = React.createClass({
    getInitialState: function() {
        return {
            sensors: [],
        }
    },
    componentWillReceiveProps: function(newProps){
        this.setState({sensors: newProps.sensors});
    },
    createTask: function(evt){
        evt.preventDefault();
        var date = new Date();
        date = date.toISOString();
        var data = {tag: {metric: 'WATER'},
                    data: {sensorID: this.refs["sensor"].value,
                           date: date,
                           waterLevel: this.refs["water_level"].value,
                           manuallyScheduled: true}}
        this.props.sendData(data);
    },
    render: function() {
        var sensor_list = [];
        if(this.state.sensors != undefined){
            for(var ii=0; ii<this.state.sensors.length;ii++){
              sensor_list.push(<option value={this.state.sensors[ii].sensor} key={ii}>{"Pad"+" "+this.state.sensors[ii].sensor}</option>)
            }
        }
        return(<div className="modal fade" id="manualTaskModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
            <div className="modal-dialog form-modal" role="document">
                <div className="modal-content">
                    <div className="modal-header">
                        <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 className="modal-title" id="myModalLabel">Manually Schedule Water Haul</h4>
                    </div>
                    <div className="modal-body">
                        <form method="post">
                            <div className="form-group">
                                <label htmlFor="water_level">Water Level</label>
                                <input type="text" id="water_level" className="form-control" ref='water_level' placeholder="Water Level" required/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="sensor">Choose Sensor</label>
                                <select id="sensor" ref="sensor" className="form-control btn btn-default" required>
                                    {sensor_list}
                                </select>
                            </div>
                            <div className="form-group text-right">
                                <input id="user_type_form_button" className="btn btn-primary btn-margin-top"
                                       type="button" onClick={this.createTask} value="Create Task"/>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>);
    }
});

export default ManualTaskModal