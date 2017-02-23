var SideBar2 = React.createClass({
    render: function() {
        return(<nav className="navbar navbar-default sidebar" role="navigation">
    <div className="container-fluid">
    <div className="navbar-header">
      <button type="button" className="navbar-toggle" data-toggle="collapse" data-target="#bs-sidebar-navbar-collapse-1">
        <span className="sr-only">Toggle navigation</span>
        <span className="icon-bar"></span>
        <span className="icon-bar"></span>
        <span className="icon-bar"></span>
      </button>      
    </div>
    <div className="collapse navbar-collapse" id="bs-sidebar-navbar-collapse-1">
      <ul className="nav navbar-nav">
        <li className="active"><a href="#map" role="tab" data-toggle="tab">Map<span className="pull-right hidden-xs showopacity glyphicon glyphicon-map-marker"></span></a></li>
        <li><a href="#users" role="tab" data-toggle="tab">Users<span className="pull-right hidden-xs showopacity glyphicon glyphicon-user"></span></a></li>
        <li><a href="#trends" role="tab" data-toggle="tab">Trends<span className="pull-right hidden-xs showopacity glyphicon glyphicon-signal"></span></a></li>
      </ul>
    </div>
  </div>
</nav>);
    }
});

export default SideBar2

/*
 <li className="dropdown">
          <a href="#" className="dropdown-toggle" data-toggle="dropdown">Usuarios <span className="caret"></span><span className="pull-right hidden-xs showopacity glyphicon glyphicon-user"></span></a>
          <ul className="dropdown-menu forAnimate" role="menu">
            <li><a href="{{URL::to('createusuario')}}">Crear</a></li>
            <li><a href="#">Modificar</a></li>
            <li><a href="#">Reportar</a></li>
            <li className="divider"></li>
            <li><a href="#">Separated link</a></li>
            <li className="divider"></li>
            <li><a href="#">Informes</a></li>
          </ul>
        </li>
*/