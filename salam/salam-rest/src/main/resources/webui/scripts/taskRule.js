
app.controller('TaskRuleListCtrl', function ($rootScope, $scope, $http, $location, dialogs) {
    $scope.Util = Util 
    $scope.is_loading = true;
    $http.get('/rest/api/task_rule?page=1').success(function (data) {
        //$scope.rules = {};
        $scope.rules = data;
        for (key in data) {
            //$scope.rules[data[key].id] = data[key];
        }
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading task generator rules', status, undefined));
        console.log('Error ' + data)
    })

    $scope.deleteRule = function (id) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the rule #' +  id + '?');
        dlg.result.then(function(btn){
            dialogs.wait(undefined, 'Deleting task generator rules', 99);
            $http({
                method: 'DELETE',
                url: '/rest/api/task_rule/' + id,
            }).success(function (data) {
                $rootScope.$broadcast('dialogs.wait.complete');
                $location.path('/task-rule');
                delete $scope.rules[id];
            }).error(function (data, status) {
                $rootScope.$broadcast('dialogs.wait.complete');
                dialogs.error(undefined, Util.error('Error deleting task generator rules', status, undefined));
                console.log('Error ' + data)
            })
        },function(btn){
        });
    }

});

app.controller('TaskRuleCreateCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    $scope.rule = {};

    $scope.createRule = function () {
        console.log($scope.rule);
        dialogs.wait(undefined, 'Creating task generator rules', 99);
        $http({
            method: 'POST',
            url: '/rest/api/task_rule',
            data: $.param($scope.rule),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $location.path('/task-rule');
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error creating task generator rules', status, undefined));
            console.log('Error ' + data)
        })
    }

});