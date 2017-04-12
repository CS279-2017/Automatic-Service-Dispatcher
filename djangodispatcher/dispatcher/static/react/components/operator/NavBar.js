var NavBar = React.createClass ({
    showModal: function(){
      $('#manualTaskModal').modal('show');
    },
    showPasscodeModal: function(){
      $('#passcodeModal').modal('show');
    },
    render: function() {
		return(<nav className="navbar navbar-inverse navbar-fixed-top navbar-grey">
  <div className="container-fluid">
    <div className="navbar-header">
      <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
        <span className="sr-only">Toggle navigation</span>
      </button>
      <a className="navbar-brand" href="#">
        <img alt="Glow" height="27px" src="/static/react/OilAndGas-PossibleLogov3.png"/>
      </a>
    </div>
      <ul className="nav navbar-nav navbar-right">
        <li><a onClick={this.showModal}>Schedule Task</a></li>
         <li><a onClick={this.showPasscodeModal}>Update Passcode</a></li>
        <li><a href="#">{this.props.user.firstName+" "+this.props.user.lastName}</a></li>
        <li><a href="/accounts/logout/">Logout</a></li>
      </ul>
    </div>
</nav>)
    }
});

export default NavBar