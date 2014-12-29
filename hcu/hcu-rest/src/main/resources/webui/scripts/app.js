
var app = angular.module('hcuapp', [
    'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute',
    'ui.bootstrap', 'dialogs.main', 'pascalprecht.translate', 'dialogs.default-translations'
]);

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
    }).otherwise({
        redirectTo: '/peer'
    })
});

function HeaderController($scope, $location) 
{ 
    $scope.isActive = function (viewLocation) { 
        return viewLocation === $location.path();
    };
}