
app.controller('SimulationTaskListCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    const URL = '/rest/api/simulation-task';
    $scope.Util = Util;
    $scope.is_loading = true;

    $scope.getAllTasks = function () {
        $http({
            method: 'GET',
            url: URL
        }).success(function (data) {
            $rootScope.tasks = {};
            for (var i = 0; i < data.length; i++) {
                $rootScope.tasks[i] = data[i];
            }
            $scope.is_loading = false;
        }).error(function (data, status) {
            $scope.is_loading = false;
            dialogs.error(undefined, Util.error('Error loading tasks', status, undefined));
            console.log('Error ' + data)
        });
    };

    $scope.getAllTasks();

    $scope.deleteTaskClicked = false;

    $http.get(URL + "/default").success(function (data) {
        $scope.addTask = data;
        $scope.addTask.task = angular.copy(angular.fromJson(data.task));
        $scope.addTask.id = undefined;
    }).error(function (data, status) {
        dialogs.error(undefined, Util.error('Error loading default task', status, undefined));
        console.log('Error ' + data)
    });

    $scope.taskDetail = function (objectId) {
        if (!$scope.deleteTaskClicked) {
            $location.path('/simulation-task-detail/' + objectId);
        }
        $scope.deleteTaskClicked = false;
    };

    $scope.deleteTask = function (index, objectId) {
        $scope.deleteTaskClicked = true;
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the task "'+ $scope.tasks[index].name +'" at index #' + index + '?');
        dlg.result.then(function (btn) {
            dialogs.wait(undefined, 'Deleting task', 99);
            $http({
                method: 'DELETE',
                url: URL + '/' + objectId
            }).success(function (data) {
                $rootScope.$broadcast('dialogs.wait.complete');
                delete $scope.tasks[index];
            }).error(function (data, status) {
                $rootScope.$broadcast('dialogs.wait.complete');
                dialogs.error(undefined, Util.error('Error deleting task', status, undefined));
                console.log('Error ' + data)
            })
        }, function (btn) {
        });

    };

    $scope.checkTaskForUnique = function (task) {
        for (var first in $scope.tasks) {

            if ($scope.tasks[first].name.toLowerCase() === task.name.toLowerCase()) {
                return false;
            }
        }
        return true;
    };

    $scope.createTask = function () {

        if(!$scope.checkTaskForUnique($scope.addTask) ) {
            dialogs.notify(undefined, "Please enter unique name!");
            return;
        }

        dialogs.wait(undefined, 'Creating task', 99);

        var taskToUpdate = {
            'name': $scope.addTask.name,
            'task': angular.toJson($scope.addTask.task, true)
        };

        $http({
            method: 'POST',
            url: URL,
            data: $.param(taskToUpdate),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $scope.getAllTasks();
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error creating task', status, {409: 'Task with the same objectId exists'}));
            console.log('Error ' + data)
        });
    }

});
