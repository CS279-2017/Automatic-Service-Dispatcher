var PasscodeModal = React.createClass({
    getInitialState: function() {
        return {
            sensors: [],
            currentPasscode: "0"
        }
    },
    componentWillReceiveProps: function(newProps){
        this.setState({sensors: newProps.sensors, currentPasscode: newProps.sensors[0].passcode});
    },
    updatePasscode: function(evt){
        evt.preventDefault();
        var date = new Date();
        date = date.toISOString();
        var data = {sensorID: this.refs["sensor"].value,
                    passcode: this.refs["passcode"].value}

        this.props.sendData(data);
    },
    resetCurrent: function(evt){
        evt.preventDefault();
        var passcode = "";
        for(var i=0;i<this.state.sensors.length; i++){
            if(this.state.sensors[i].sensor == this.refs["sensor"].value)
                passcode = this.state.sensors[i].passcode;
        }

        this.setState({currentPasscode: passcode})
    },
    render: function() {
        var sensor_list = [];
        if(this.state.sensors != undefined){
            for(var ii=0; ii<this.state.sensors.length;ii++){
              sensor_list.push(<option value={this.state.sensors[ii].sensor} key={ii}>{"Pad"+" "+this.state.sensors[ii].sensor}</option>)
            }
        }
        return(<div className="modal fade" id="passcodeModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
            <div className="modal-dialog form-modal" role="document">
                <div className="modal-content">
                    <div className="modal-header">
                        <button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 className="modal-title" id="myModalLabel">Update a Passcode</h4>
                    </div>
                    <div className="modal-body">
                        <form method="post">
                            <div class="form-group">
                            <label class="col-sm-2 control-label">Old Passcode</label>
                            <div class="col-sm-10">
                              <p class="form-control-static">{this.state.currentPasscode}</p>
                            </div>
                          </div>
                            <div className="form-group">
                                <label htmlFor="passcode">New Passcode</label>
                                <input type="text" id="passcode" className="form-control" ref='passcode' placeholder="New Passcode" required/>
                            </div>
                            <div className="form-group">
                                <label htmlFor="sensor">Choose Sensor</label>
                                <select id="sensor" ref="sensor" className="form-control btn btn-default" onChange={this.resetCurrent} required>
                                    {sensor_list}
                                </select>
                            </div>
                            <div className="form-group text-right">
                                <input id="user_type_form_button" className="btn btn-primary btn-margin-top"
                                       type="button" onClick={this.updatePasscode} value="Update"/>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>);
    }
});

export default PasscodeModal