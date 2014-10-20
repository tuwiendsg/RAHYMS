
app.controller('CollectiveListCtrl', function ($rootScope, $scope, $http, $location, dialogs) {
    $scope.Util = Util 
    $scope.is_loading = true;
    $http.get('/rest/api/collective?page=1').success(function (data) {
        $scope.collectives = data;
        $scope.$location = $location;
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading collectives', status, undefined));
        console.log('Error ' + data)
    })

});

app.controller('CollectiveDetailCtrl', function ($rootScope, $scope, $http, $location, $routeParams, dialogs) {
    $scope.Util = Util 
    $scope.is_loading = true;
    $http.get('/rest/api/collective/' + $routeParams.collectiveId).success(function (data) {
        $scope.collective = data;
        // calculate size
        var size = 0;
        for (var i=0; i<data.assignments.length; i++) {
            if (data.assignments[i].status!="DELEGATED") {
                size = size + 1;
            }
        }
        $scope.collective.size = size;
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading collective details', status, undefined));
        console.log('Error ' + data)
    })

});
