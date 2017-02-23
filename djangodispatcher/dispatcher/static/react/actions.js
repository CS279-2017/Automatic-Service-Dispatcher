export const GET_CURRENT_USER = 'GET_CURRENT_USER';
export const GET_OPERATOR_DATA = 'GET_OPERATOR_DATA';

export function fetchCurrentUser(){
    return function (dispatch){
        return $.get('/api/currentuser', function(result){
            dispatch(getDataUpdater(result))
        }.bind(this));
    }
}

export function getDataUpdater(data){
    return { type:GET_CURRENT_USER, data }
}

export function fetchCurrentUserTasks(){
    return function (dispatch){
        return $.get('/api/my_task', function(result){
            dispatch(getDataUpdater(result))
        }.bind(this));
    }
}

export function fetchCompletedUserTasks(){
    return function (dispatch){
        return $.get('/api/completedtasks', function(result){
            dispatch(getDataUpdater(result))
        }.bind(this));
    }
}

export function getOperatorUpdater(data){
    return { type:GET_OPERATOR_DATA, data }
}

export function fetchAllUsers(){
    return function (dispatch){
        return $.get('/api/allusers', function(result){
            dispatch(getOperatorUpdater(result))
        }.bind(this));
    }
}

export function fetchAllSensors(){
    return function (dispatch){
        return $.get('/api/sensors', function(result){
            dispatch(getOperatorUpdater(result))
        }.bind(this));
    }
}

export function fetchTotalData(){
    return function (dispatch){
        return $.get('/api/totaldata', function(result){
            dispatch(getOperatorUpdater(result))
        }.bind(this));
    }
}

export function completeTask(data){
    return function (dispatch){
        var url = '/api/finishtask/';
        $.ajaxSetup({
            beforeSend: function(xhr, settings) {
                function getCookie(name) {
                    var cookieValue = null;
                    if (document.cookie && document.cookie != '') {
                        var cookies = document.cookie.split(';');
                        for (var i = 0; i < cookies.length; i++) {
                            var cookie = jQuery.trim(cookies[i]);
                            // Does this cookie string begin with the name we want?
                            if (cookie.substring(0, name.length + 1) == (name + '=')) {
                                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                                break;
                            }
                        }
                    }
                    return cookieValue;
                }
                if (!(/^http:.*!/.test(settings.url) || /^https:.*!/.test(settings.url))) {
                    // Only send the token to relative URLs i.e. locally.
                    xhr.setRequestHeader("X-CSRFToken", getCookie('csrftoken'));
                }
            }
        });
        return $.ajax({
            url: url,
            dataType: 'json',
            type: 'POST',
            data: data,
            success: function(result) {
                if(result.error != undefined){
                    console.log("error");
                    dispatch(getDataUpdater(result));
                } else {
                    dispatch(getDataUpdater(result));
                }
            }.bind(this),
            error: function(xhr, status, err) {
                console.log(err);
            }.bind(this)});
    }
}
