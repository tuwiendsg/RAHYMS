Util = {};

Util.getStatusClass = function(status) {
    switch (status) {
    case "ASSIGNED":
    case "DELEGATED":
        clazz = "label-warning";
        break;
    case "RUNNING":
        clazz = "label-success";
        break;
    case "TERMINATED":
        clazz = "label-danger";
        break;
    case "FINISHED":
        clazz = "label-primary";
        break;
    default:
        clazz = "label-default";
    }
    return clazz;
}

Util.getAssignmentActionButtons = function(status, route_action) {
    actionButtons = [];
    switch (route_action) {
    case "accept":
        actionButtons.push({"action": "RUNNING", "title": "Accept", "clazz": "btn-default"});
        break;
    case "terminate":
        actionButtons.push({"action": "TERMINATED", "title": "Terminate", "clazz": "btn-danger"});
        break;
    case "delegate":
        actionButtons.push({"action": "DELEGATED", "title": "Delegate", "clazz": "btn-warning"});
        break;
    default:
        // create buttons based on the current state
        switch (status) {
        case "ASSIGNED":
            actionButtons.push({"action": "RUNNING", "title": "Accept", "clazz": "btn-default"});
            actionButtons.push({"action": "DELEGATED", "title": "Delegate", "clazz": "btn-warning"});
            //actionButtons.push({"action": "TERMINATED", "title": "Terminate", "clazz": "btn-danger"});
            break;
        case "DELEGATED":
            break;
        case "RUNNING":
            actionButtons.push({"action": "PAUSED", "title": "Pause", "clazz": "btn-default"});
            actionButtons.push({"action": "FINISHED", "title": "Finish", "clazz": "btn-default"});
            actionButtons.push({"action": "DELEGATED", "title": "Delegate", "clazz": "btn-warning"});
            break;
        case "PAUSED":
            actionButtons.push({"action": "RUNNING", "title": "Resume", "clazz": "btn-default"});
            actionButtons.push({"action": "FINISHED", "title": "Finish", "clazz": "btn-default"});
            actionButtons.push({"action": "DELEGATED", "title": "Delegate", "clazz": "btn-warning"});
            break;
        case "TERMINATED":
            break;
        case "FINISHED":
            break;
        }                        
    };

    return actionButtons;
}

Util.error = function(message, status, status_mapping) {
    var status_message = '';
    if (status==0) status_message = 'Connection Error';
    if (status_mapping && status_mapping[status]) {
        status_message = ' (' + status_mapping[status] + ')';
    }
    return message + '<br>Status = ' + status + status_message;
}

Util.spinner = function() {
    return '<h4><i class="fa fa-lg fa-spinner fa-spin"></i> Loading...</h4>';
}
