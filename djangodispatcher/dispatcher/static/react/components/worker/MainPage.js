import { connect } from 'react-redux'
import { fetchCurrentUser, fetchCurrentUserTasks, fetchCompletedUserTasks,
 completeTask} from '../../actions'
import TaskPanel from './TaskPanel'
import ActiveTask from './ActiveTask'

var MainPage = React.createClass({
    componentDidMount: function(){
        this.props.fetchCurrentUser();
        this.props.fetchCurrentUserTasks();
        this.props.fetchCompletedUserTasks();
    },
    render: function() {
        return(<div>

                <h1 className="text-center">{this.props.user.firstName+" "+this.props.user.lastName}
                <span className="logout-btn"><a className="btn btn-warning" href="/accounts/logout/">Logout</a></span></h1>
                <div className="col-md-6">
                    <ActiveTask name={this.props.name} date={this.props.date} sensor={this.props.sensor}
                    completeTask={this.props.completeTask} taskId={this.props.taskId}/>
                </div>
                <div className="col-md-6">
                    <TaskPanel tasks={this.props.user.completed_tasks} type="Completed"/>
                </div>
            </div>);
    }
});

const mapStateToProps = (state) => {
    //updateCompany
    //console.log(state.getCurrentUser)
    return {
        user: state.getCurrentUser,
        completed_tasks: state.getCurrentUser.completedTasks,

        taskId: state.getCurrentUser.taskId,
        name: state.getCurrentUser.name,
        date: state.getCurrentUser.date,
        workerId: state.getCurrentUser.workerId,
        sensor: state.getCurrentUser.sensor,
        dateCompleted: state.getCurrentUser.dateCompleted,
        hourOpen: state.getCurrentUser.hoursOpen
    };
};

//in this method, call the action method
const mapDispatchToProps = (dispatch) => {
    return {
        fetchCurrentUser:() => {
            dispatch(fetchCurrentUser());
        },
        fetchCurrentUserTasks:() => {
            dispatch(fetchCurrentUserTasks());
        },
        fetchCompletedUserTasks:() => {
            dispatch(fetchCompletedUserTasks());
        },
        completeTask:(data)=>{
            dispatch(completeTask(data));
        }
    }
};

export default connect(mapStateToProps, mapDispatchToProps)(MainPage)