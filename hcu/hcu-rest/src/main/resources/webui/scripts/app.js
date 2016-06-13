
var app = angular.module('hcuapp', [
    'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute', 'ngDragDrop',
    'ui.bootstrap', 'dialogs.main', 'pascalprecht.translate', 'dialogs.default-translations'
]);

app.filter('takeLastWordAfterDot', function () {
    // pass in `item` which is the single Object we'll manipulate
    return function (item) {

        if(item === undefined) {
            return "";
        }

        return item.split(".").pop();
    };
});

app.directive('onlyDigits', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function (scope, element, attr, ctrl) {
            function inputValue(val) {

                console.log("inOnlyDigits");

                if (val) {
                    var digits = val.replace(/[^0-9]/g, '');

                    if (digits !== val) {
                        ctrl.$setViewValue(digits);
                        ctrl.$render();
                    }
                    return parseInt(digits,10);
                }
                return undefined;
            }
            ctrl.$parsers.push(inputValue);
        }
    };
});

app.config(function ($routeProvider) {
    $routeProvider.when('/peer', {
        templateUrl: 'views/list-peer.html',
        controller: 'PeerListCtrl'
    }).when('/peer/create', {
        templateUrl: 'views/create-peer.html',
        controller: 'PeerCreateCtrl'
    }).when('/task', {
        templateUrl: 'views/list-task.html',
        controller: 'TaskListCtrl'
    }).when('/task/create', {
        templateUrl: 'views/create-task.html',
        controller: 'TaskCreateCtrl'
    }).when('/task-rule', {
        templateUrl: 'views/list-task-rule.html',
        controller: 'TaskRuleListCtrl'
    }).when('/task-rule/create', {
        templateUrl: 'views/create-task-rule.html',
        controller: 'TaskRuleCreateCtrl'
    }).when('/collective', {
        templateUrl: 'views/list-collective.html',
        controller: 'CollectiveListCtrl'
    }).when('/collective/:collectiveId', {
        templateUrl: 'views/detail-collective.html',
        controller: 'CollectiveDetailCtrl'
    }).when('/simulation-task', {
        templateUrl: 'views/simulation/simulation-task-list.html',
        controller: 'SimulationTaskListCtrl'
    }).when('/simulation-task-detail/:objectId', {
        templateUrl: 'views/simulation/simulation-task-detail.html',
        controller: 'SimulationTaskDetailCtrl'
    }).when('/simulation-unit', {
        templateUrl: 'views/simulation/simulation-unit-list.html',
        controller: 'SimulationUnitListCtrl'
    }).when('/simulation-unit-detail/:objectId', {
        templateUrl: 'views/simulation/simulation-unit-detail.html',
        controller: 'SimulationUnitDetailCtrl'
    }).when('/simulation', {
        templateUrl: 'views/simulation/simulation.html',
        controller: 'SimulationCtrl'
    }).when('/simulation-analytics', {
        templateUrl: 'views/simulation/simulation-analytics.html',
        controller: 'SimulationAnalyticsCtrl'
    }).otherwise({
        redirectTo: '/peer'
    })
});

function HeaderController($rootScope, $scope, $location)
{

    const MAX_OF_MAPPING_VALUES = 9;

    $scope.isActive = function (viewLocation) {
        return viewLocation === $location.path();
    };

    $scope.interactiveMode = true;
    $scope.simulationMode  = false;

    $scope.interactive = function () {
        $scope.simulationMode = false;
        $scope.interactiveMode = true;
    };

    $scope.simulation = function () {
        $scope.interactiveMode = false;
        $scope.simulationMode = true;
    };

    $rootScope.mappingValueArray = [];

    $rootScope.generateValueFromRandomNumberForRepresentation = function (value, valueToAdd) {
        var valueValidated = {};
        valueValidated.params = {};

        //for static
        //todo brk task da static value var mi?
        if(angular.isNumber(value)) {
            valueValidated.class = 'Static';
            valueValidated.params.first = value;
            return valueValidated;
        }

        if (!value || !value.class) {
            valueToAdd = {};
            return valueToAdd.valueToAdd;
        }
        valueValidated.class = value.class;
        valueValidated.params.first = value.params[0];


        if (value.params.length > 1)
            valueValidated.params.second = value.params[1];

        if (value.params.length > 2)
            valueValidated.params.third = value.params[2];

        if(value.mapping)
            valueValidated.mapping = value.mapping;

        return valueValidated;
    };

    //todo brk bu value daki static secenegi bütün value olan yerlere gelecek mi? non functional da var , functional a da gelecek mi?

    $rootScope.randomNumberGenerate = function (valueConnect, valueToAdd, mappingValues) {

        if (valueConnect) {
            valueToAdd = angular.copy(valueConnect);

        }

        var valueValidated = {};

        valueValidated.class = valueToAdd.class;
        valueValidated.params = [];


        if(valueToAdd.class === 'Static') {
            valueValidated = valueToAdd.params.first;
            return valueValidated;
        }

        valueValidated.params.push(valueToAdd.params.first);

        if (valueToAdd.class !== 'Static') {
            valueValidated.params.push(valueToAdd.params.second);
        }
        //todo brk value mecburi diyor ama non functional da girilmemis valuelar var onlar ne olacak? metric leri silersek göstermez isek sorun kalkiyor.

        if (valueToAdd.class === 'NormalDistribution') {
            valueValidated.params.push(valueToAdd.params.third);
        } else if (valueToAdd.class === 'UniformIntegerDistribution'){
            mappingValues = $rootScope.filterMappingValues(mappingValues, valueToAdd);
            if (mappingValues) {
                valueValidated.mapping = {};
                valueValidated.mapping = mappingValues;
            }
        }

        return valueValidated;
    };

    $rootScope.filterMappingValues = function (mappingValues, valueToAdd) {

        var filtered = {};

        angular.forEach(mappingValues, function(value, key) {
            if(key >= valueToAdd.params.first && key <= valueToAdd.params.second)
                filtered[key] = value;
        });

        return filtered;
    };

    $rootScope.calculateNumberOfMappingValues = function (valueToAdd) {
        console.info("in calculate mapping");

        $rootScope.mappingValueArray = [];

        if (valueToAdd.params.second < valueToAdd.params.first ||
            (valueToAdd.params.second - valueToAdd.params.first) > MAX_OF_MAPPING_VALUES) {

            $rootScope.mappingValueArray = undefined;
            return;
        }

        for (var i = valueToAdd.params.first; i <= valueToAdd.params.second; i++) {
            $rootScope.mappingValueArray.push(i);
        }

        console.info("number of mapping values and array : " + $rootScope.mappingValueArray.length + '\n' +
            $rootScope.mappingValueArray);
    };
}

