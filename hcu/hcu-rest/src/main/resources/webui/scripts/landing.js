
app.controller('LandingCtrl', function ($scope, $http, $location, $routeParams) {
    switch ($routeParams.status) {
    case "ok":
        $scope.message = "Your request is being processed.";
        break;
    case "cancel":
        $scope.message = "Request canceled.";
        break;
    }
});

