
var app = angular.module('salamapp-peer', [
   'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute',
   'ui.bootstrap', 'dialogs.main', 'pascalprecht.translate', 'dialogs.default-translations'
]);

app.config(function ($routeProvider) {
    $routeProvider.when('/status/:collectiveId/:peerId/:action', {
        templateUrl: 'views/assignment-status.html',
        controller: 'AssignmentStatusCtrl'
    }).when('/status/:collectiveId/:peerId', {
        templateUrl: 'views/assignment-status.html',
        controller: 'AssignmentStatusCtrl'
    }).when('/landing/:status', {
        templateUrl: 'views/landing.html',
        controller: 'LandingCtrl'
    })
});
