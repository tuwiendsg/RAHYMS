/**
 * Created by karaoglan on 08/04/16.
 */

app.controller('SimulationAnalyticsCtrl', function ($rootScope, $scope, $http, $location, dialogs) {


    const URL = '/rest/api/simulation/graph';
    const URL_SIMULATION = '/rest/api/simulation';
    const URL_TEMP = '/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-rest/src/main/resources/webui/temp/';

    $scope.graph = {};
    $scope.graph.xAxis = "clock";
    $scope.graph.yAxis = "algo_time";

    $scope.graphData = {};

    //todo brk sürekli override yapilmamasi lazim id lerle kontrol et ve web de date ile göster dropdown da
    //todo brk ayriyeten ayri bir button ile global svc dosyasini download imkani vermek lazim

    dialogs.wait(undefined, 'Getting simulations', 99);
    $http.get(URL_SIMULATION).success(function (data) {
        $rootScope.$broadcast('dialogs.wait.complete');
        $scope.simulations = data;
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading simulations', status, undefined));
        console.log('Error ' + data)
    });

    $scope.showGraph = function () {
        if(!$scope.graph.xAxis || !$scope.graph.yAxis) {
            dialogs.error('Please select values for x and y Axis');
            return;
        }

        $scope.graphData.xAxis = $scope.graph.xAxis;
        $scope.graphData.yAxis = $scope.graph.yAxis;
        $scope.graphData.simulationDate = $scope.simulationSelected.timeCreated;

        $http({
            method: 'POST',
            url: URL,
            data: $scope.graphData
        }).success(function (response) {
            console.log("response from graph is : " + response);
            $scope.graph.image = response.image;
            $scope.showingGraph = true;
        }).error(function (response, status) {
            dialogs.error(undefined, Util.error('Error starting graph',
                status, {404: 'graph not found', 503: 'graph Service unavailable'}));
        });

    };

    $scope.downloadFile = function(downloadPath) {

        //todo brk bu temp yerine direkt simulation traces dan dosya path in den indirilmesi lazim, su an path in basina localhost koydugu icin indirelemiyor
        //window.open('temp/' + $scope.simulationSelected.timeCreated +'.csv', '_blank', '');
        window.open('temp/file.csv', '_blank', '');
    };
        //todo brk su an temp deki dosyalari kopyaladiktan sonra güncellemiyor sistem incele
    $scope.copyFileToTemp = function () {
        var toSend = {
            'path': $scope.simulationSelected.filePath,
            'tempPath': URL_TEMP + 'file.csv'
        };

        $http({
            method: 'POST',
            url: URL + '/copy',
            data: $.param(toSend),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
        }).error(function (data, status) {
            dialogs.error(undefined, Util.error('Error sendind copy file path', status));
            console.log('Error ' + data)
        });
    };
});


