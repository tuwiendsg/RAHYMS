/**
 * Created by karaoglan on 18/04/16.
 */


app.controller('SimulationCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    const URL = '/rest/api/simulation';
    const TASK_URL = '/rest/api/simulation-task';
    const UNIT_URL = '/rest/api/simulation-unit';

    $scope.unitListDrag = [];
    $scope.unitListDrop = [];

    $scope.taskListDrag = [];
    $scope.taskListDrop = [];

    $http({
        method: 'GET',
        url: TASK_URL
    }).success(function (data) {
        for (var i = 0; i < data.length; i++) {
            data[i].isUnit = false;
            data[i].task = angular.fromJson(data[i].task);
            $scope.taskListDrag.push(data[i]);
        }
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading tasks', status, undefined));
        console.log('Error ' + data);
    });

    $http({
        method: 'GET',
        url: UNIT_URL
    }).success(function (data) {
        for (var i = 0; i < data.length; i++) {
            data[i].isUnit = true;
            data[i].unit = angular.fromJson(data[i].unit);
            $scope.unitListDrag.push(data[i]);
        }
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading units', status, undefined));
        console.log('Error ' + data);
    });

    //todo brk start dedikten sonra beklerken baska yere tiklanabiliyor.

    $scope.initializeDefaultValues = function () {

        $scope.consumerProperties = {};
        $scope.composerProperties = {};
        $scope.simulationProperties = {};

        //todo brk base directory ile alakali
        //todo brk bu prefixler bu sekidle eklenmemesi lazim web de dynamic olmasi lazim sor. su anlik gerek yokmus.
        $scope.composerProperties.reliability_trace_file_prefix = '/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-simulation/traces/reliability/';
        $scope.composerProperties.trace_file_prefix = '/Users/karaoglan/IdeaProjects/RAHYMS/hcu/hcu-simulation/traces/composer/composer-';

        $scope.consumerProperties.numberOfCycles = 5;
        $scope.consumerProperties.waitBetweenCycles = 1;

        $scope.composerProperties.algorithm = "PriorityDistribution";
        $scope.composerProperties.aco_variant = "AntSystemAlgorithm";
        $scope.composerProperties.epsilon_pheromone = 0.1;
        $scope.composerProperties.number_of_ants = 8;
        $scope.composerProperties.maximum_number_of_cycles = 2000;
        $scope.composerProperties.stagnant_limit = 10000;
        $scope.composerProperties.initial_pheromone = 0.001;
        $scope.composerProperties.pheromone_weight = 0.2;
        $scope.composerProperties.heuristic_weight = 1;
        $scope.composerProperties.pheromone_evaporation = 0.01;
        $scope.composerProperties.connectedness_weight = 1.0;
        $scope.composerProperties.cost_weight = 1.0;
        $scope.composerProperties.response_time_weight = 1.0;
        $scope.composerProperties.competency_weight = 1.0;
        $scope.composerProperties.objective_multiplier = 100;
        $scope.composerProperties.min_pheromone = 0.0001;
        $scope.composerProperties.max_pheromone = 1;
        $scope.composerProperties.q_0 = 0.5;
        $scope.composerProperties.pheromone_decay = 0.01;
    };

    $scope.initializeDefaultValues();

    $scope.dragAll = function () {
        angular.forEach($scope.unitListDrag, function (value) {
            $scope.unitListDrop.push(value);
        });

        angular.forEach($scope.taskListDrag, function (value) {
            $scope.taskListDrop.push(value);
        });

        $scope.unitListDrag = [];
        $scope.taskListDrag = [];
    };

    $scope.startSimulation = function () {

        $scope.simulationProperties.timeCreated = new Date();

        //todo brk simulationProperties modeline gerek kalmadi su an assagida depend. gerekiyor cünkü string olarak composer in icinde yollayacagim
        //todo brk bu composerdan nasil alinacak simulation name felan incele.
        for (var key in $scope.simulationProperties) {
            $scope.composerProperties[key] = $scope.simulationProperties[key];
        }

        $scope.tasks = [];
        $scope.units = [];
        dialogs.wait(undefined, 'Starting simulation', 99);

        angular.forEach($scope.unitListDrop, function (value) {
            $scope.units.push(angular.toJson(value.unit, true));
        });

        angular.forEach($scope.taskListDrop, function (value) {
            $scope.tasks.push(angular.toJson(value.task, true));
        });

        var jsonData = {
            'units': $scope.units,
            'tasks': $scope.tasks,
            'composerProperties': angular.toJson($scope.composerProperties, true),
            'consumerProperties': $scope.consumerProperties,
            'simulation': $scope.simulationProperties
        };

        $http({
            method: 'POST',
            url: URL,
            data: jsonData
        }).success(function (response) {
            console.log("response from simulation start is : " + response);
            $rootScope.$broadcast('dialogs.wait.complete');
            $location.path('/simulation-analytics');
        }).error(function (response, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error starting simulation',
                status, {404: 'Simulation not found', 503: 'Simulation Service unavailable'}));
        });
        $scope.is_loading = false;
    };

    $("#unitDrag").droppable({
        accept: function(d) {
            if(d.hasClass("units")){
                return true;
            }
        }
    });

    $("#unitDrop").droppable({
        accept: function(d) {
            if(d.hasClass("units")){
                return true;
            }
        }
    });

    $("#taskDrag").droppable({
        accept: function(d) {
            if(d.hasClass("tasks")){
                return true;
            }
        }
    });

    $("#taskDrop").droppable({
        accept: function(d) {
            if(d.hasClass("tasks")){
                return true;
            }
        }
    });

});