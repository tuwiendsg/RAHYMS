app.controller('SimulationAnalyticsCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    const URL = '/rest/api/simulation/graph';
    const URL_SIMULATION = '/rest/api/simulation';
    const URL_FILE_GET = '/rest/api/simulation/file';

    $scope.graph = {};
    $scope.graph.xAxis = "clock";
    $scope.graph.yAxis = "algo_time";

    $scope.graphData = {};
    $scope.simulationSelected = undefined;

    dialogs.wait(undefined, 'Getting simulations', 99);

    $scope.getSimulations = function () {
        $http.get(URL_SIMULATION).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $scope.simulations = data;
            $scope.is_loading = false;
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error loading simulations', status, undefined));
            console.log('Error ' + data)
        });
    };

    $scope.getSimulations();

    $scope.showGraph = function () {
        if (!$scope.graph.xAxis || !$scope.graph.yAxis) {
            dialogs.error('Please select values for x and y Axis');
            return;
        }

        $scope.graphData.xAxis = $scope.graph.xAxis;
        $scope.graphData.yAxis = $scope.graph.yAxis;
        $scope.graphData.simulationId = $scope.simulationSelected.id;

        $('html, body').animate({
            scrollTop: $("#graph-div")[0].scrollHeight
        }, "slow");

        $http({
            method: 'POST',
            url: URL,
            data: $scope.graphData
        }).success(function (response) {
            $scope.graph.image = response.image;
            $scope.showingGraph = true;
        }).error(function (response, status) {
            dialogs.error(undefined, Util.error('Error starting graph',
                status, {404: 'graph not found', 503: 'graph Service unavailable'}));
        });

    };

    $scope.downloadFile = function () {

        $http({
            method: 'GET',
            url: URL_FILE_GET + '?filePath=' + $scope.simulationSelected.filePath
        }).success(function (response) {

            var a = window.document.createElement('a');
            a.href = window.URL.createObjectURL(new Blob([response], {type: 'text/csv'}));
            a.download = $scope.simulationSelected.simulationName + '-' + $scope.simulationSelected.timeCreated + '.csv';

            // Append anchor to body.
            document.body.appendChild(a);
            a.click();

            // Remove anchor from body
            document.body.removeChild(a);

        }).error(function (response, status) {
            dialogs.error(undefined, Util.error('Error getting file',
                status, {404: 'file not found', 503: 'file Service unavailable'}));
        });

    };

    $scope.refresh = function () {

        dialogs.wait(undefined, 'Refreshing simulation', 99);

        $scope.getSimulations();

        for (var i = 0; i < $scope.simulations.length; i++) {
            if ($scope.simulations[i].id === $scope.simulationSelected.id) {
                $scope.simulationSelected.timeFinished = $scope.simulations[i].timeFinished
            }
        }

    };

});


