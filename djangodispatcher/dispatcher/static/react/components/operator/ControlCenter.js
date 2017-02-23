import { connect } from 'react-redux'
import { fetchCurrentUser, fetchAllUsers, fetchAllSensors, fetchTotalData} from '../../actions'
import UserPanel from './UserPanel'
import Map from './Map'
import TaskTrend from './TaskTrend'
import SiteTable from './SiteTable'
import NavBar from './NavBar'
import SideBar2 from './SideBar2'

var ControlCenter = React.createClass({
    componentDidMount: function(){
        this.props.fetchCurrentUser();
        this.props.fetchAllUsers();
        this.props.fetchAllSensors();
        this.props.fetchTotalData();
    },
    render: function() {
        var userlist = [];
        var trends = [];
        if(this.props.allUsers!=undefined){
            for(var ii=0;ii<this.props.allUsers.length;ii++){
                userlist.push(<div className="col-md-4 col-sm-6" key={ii}><UserPanel user = {this.props.allUsers[ii]}/></div>);
            }
        }
        trends.push(<div className="col-md-4" key={1}><TaskTrend title="Total Users" number={this.props.numUsers}/></div>);
        trends.push(<div className="col-md-4" key={2}><TaskTrend title="Total Tasks Done" number={this.props.numDone}/></div>);
        trends.push(<div className="col-md-4" key={3}><TaskTrend title="Total Tasks Pending" number={this.props.numActive}/></div>);
        return(<div className="fluid-body col-md-12">
                <NavBar user={this.props.user}/>
                <SideBar2/>
                  <div className="tab-content">
                    <div role="tabpanel" className="tab-pane active text-center" id="map">
                    <Map sensors={this.props.sensors} users={this.props.allUsers}/>
                    </div>
                    <div role="tabpanel" className="tab-pane" id="users"><br/><br/>{userlist}</div>
                    <div role="tabpanel" className="tab-pane" id="trends"><br/><br/>
                        <div className="col-md-12">
                        {trends}
                        </div>
                        <div className="col-md-12">
                            <SiteTable sites={this.props.sites}/>
                        </div>
                    </div>
                  </div>

            </div>);
    }
});//                    <Map sensors={this.props.sensors} users={this.props.allUsers}/>


const mapStateToProps = (state) => {
    console.log(state);
    return {
        user: state.getCurrentUser,
        allUsers: state.operatorData.users,

        sensors: state.operatorData.sensors,
        numActive:   state.operatorData.numActive,
        numDone:    state.operatorData.numDone,
        numUsers:   state.operatorData.numUsers,
        sites:      state.operatorData.sites
    };
};

//in this method, call the action method
const mapDispatchToProps = (dispatch) => {
    return {
        fetchCurrentUser:() => {
            dispatch(fetchCurrentUser());
        },
        fetchAllUsers:() => {
            dispatch(fetchAllUsers());
        },
        fetchAllSensors:() => {
            dispatch(fetchAllSensors());
        },
        fetchTotalData:() => {
            dispatch(fetchTotalData());
        }
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(ControlCenter)