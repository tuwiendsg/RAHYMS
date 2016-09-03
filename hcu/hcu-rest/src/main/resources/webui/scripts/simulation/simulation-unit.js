

app.controller('SimulationUnitListCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    const URL = '/rest/api/simulation-unit';
    $scope.Util = Util;
    $scope.is_loading = true;

    $scope.getAllUnits = function () {
        $http({
            method: 'GET',
            url: URL
        }).success(function (data) {
            $rootScope.units = {};
            for (var i = 0; i < data.length; i++) {
                $rootScope.units[i] = data[i];
            }
            $scope.is_loading = false;
        }).error(function (data, status) {
            $scope.is_loading = false;
            dialogs.error(undefined, Util.error('Error loading units', status, undefined));
            console.log('Error ' + data)
        });
    };

    $scope.getAllUnits();

    $scope.deleteUnitClicked = false;

    $http.get(URL + "/default").success(function (data) {
        $scope.addUnit = data;
        $scope.addUnit.unit = angular.copy(angular.fromJson(data.unit));
        $scope.addUnit.id = undefined;
    }).error(function (data, status) {
        dialogs.error(undefined, Util.error('Error loading default unit', status, undefined));
        console.log('Error ' + data)
    });

    $scope.unitDetail = function (objectId) {
        if(!$scope.deleteUnitClicked) {
            $location.path('/simulation-unit-detail/' + objectId);
        }

        $scope.deleteUnitClicked = false;

    };

    $scope.deleteUnit = function (index, objectId) {
        $scope.deleteUnitClicked = true;
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the unit "'+ $scope.units[index].name +'" at index #' + index + '?');
        dlg.result.then(function (btn) {
            dialogs.wait(undefined, 'Deleting unit', 99);
            $http({
                method: 'DELETE',
                url: URL + '/' + objectId,
            }).success(function (data) {
                $rootScope.$broadcast('dialogs.wait.complete');
                delete $scope.units[index];
            }).error(function (data, status) {
                $rootScope.$broadcast('dialogs.wait.complete');
                dialogs.error(undefined, Util.error('Error deleting unit', status, undefined));
                console.log('Error ' + data)
            })
        }, function (btn) {
        });

    };

    $scope.checkUnitForUnique = function (unit) {
        for (var first in $scope.units) {

            if ($scope.units[first].name.toLowerCase() === unit.name.toLowerCase()) {
                return false;
            }
        }
        return true;
    };

    $scope.createUnit = function () {
        console.log($scope.addUnit.unit);

        if(!$scope.checkUnitForUnique($scope.addUnit) ) {
            dialogs.notify(undefined, "Please enter unique name!");
            return;
        }

        dialogs.wait(undefined, 'Creating unit', 99);
        var unitToSend = {
            'name': $scope.addUnit.name,
            'unit': angular.toJson($scope.addUnit.unit, true),
            'id': $scope.addUnit.id
        };

        $http({
            method: 'POST',
            url: URL,
            data: $.param(unitToSend),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $scope.getAllUnits();
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error creating unit', status, {409: 'unit with the same objectId exists'}));
            console.log('Error ' + data);
        });
    }
});
