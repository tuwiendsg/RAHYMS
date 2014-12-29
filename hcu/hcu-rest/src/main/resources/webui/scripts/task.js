
app.controller('TaskListCtrl', function ($rootScope, $scope, $http, $location, dialogs) {
    $scope.Util = Util 
    $scope.is_loading = true;
    $http.get('/rest/api/task?page=1').success(function (data) {
        $scope.tasks = data;
        $scope.$location = $location;
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading tasks', status, undefined));
        console.log('Error ' + data)
    })

    $scope.retrieveAssignments = function (task) {
        $http.get('/rest/api/collective/' +  task.collectiveId).success(function (data) {
            task.assignments = data.assignments;
        }).error(function (data, status) {
            dialogs.error(undefined, Util.error('Error loading task details', status, undefined));
            console.log('Error ' + data)
        })    	
    }

});

app.controller('TaskCreateCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    $scope.task = {};

    $scope.createTask = function () {
        console.log($scope.task);
        dialogs.wait(undefined, 'Creating task', 99);
        $http({
            method: 'POST',
            url: '/rest/api/task',
            data: $.param($scope.task),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $location.path('/task');
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error creating task', status, {404: 'Task generator rule not found', 503: 'Unable to assemble a collective to serve the task'}));
            console.log('Error ' + data)
        })
    }
});