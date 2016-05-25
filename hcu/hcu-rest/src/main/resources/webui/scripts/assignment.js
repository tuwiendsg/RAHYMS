
app.controller('AssignmentStatusCtrl', function ($rootScope, $scope, $http, $location, $routeParams, dialogs) {

    // get collective data
    $scope.Util = Util 
    $scope.is_loading = true;
    $http.get('/rest/api/collective/' + $routeParams.collectiveId).success(function (data) {
        $scope.collective = data;
        console.log(data);
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading assignment data', status, undefined));
        console.log('Error ' + data)
    });

    // get assignment data
    $http.get('/rest/api/collective/' + $routeParams.collectiveId + '/assignment/' + $routeParams.peerId).success(function (data) {
        $scope.is_loading = false;
        $scope.assignment = data;
        console.log(data);
        $scope.actionButtons = Util.getAssignmentActionButtons($scope.assignment.status, $routeParams.action);
    }).error(function (data, status) {
        $rootScope.$broadcast('dialogs.wait.complete');
        dialogs.error(undefined, Util.error('Error loading assignment data', status, undefined));
        console.log('Error ' + data)
    });

    $scope.updateStatus = function (collectiveId, peerId, status, action) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure you want to <b>' + action.toUpperCase() + '</b> this assignment?');
        dlg.result.then(function(btn){
            dialogs.wait(undefined, 'Updating assignment status', 99);
            $http({
                method: 'PUT',
                url: '/rest/api/collective/' + collectiveId + '/status/' + peerId,
                data: $.param({'status': status}),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function (data) {
                $rootScope.$broadcast('dialogs.wait.complete');
                $location.path('/landing/ok');
            }).error(function (data, status) {
                $rootScope.$broadcast('dialogs.wait.complete');
                dialogs.error(undefined, Util.error('Error updating assignment status', status, undefined));
                console.log('Error ' + data)
            })
        },function(btn){
        });
    }

    $scope.cancel = function () {
        $location.path('/landing/cancel');
    }
    
});

