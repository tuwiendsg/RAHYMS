/**
 * Created by karaoglan on 17/04/16.
 */

app.controller('SimulationUnitDetailCtrl', function ($rootScope, $routeParams, $scope, $http, $location, dialogs) {

    const URL = '/rest/api/simulation-unit';

    var objectId = $routeParams.objectId;

    dialogs.wait(undefined, 'Getting unit', 99);
    $http.get(URL + "/" + objectId).success(function (data) {
        $rootScope.$broadcast('dialogs.wait.complete');
        $scope.unitGenerator = data;
        $scope.unitGeneratorName = angular.copy($scope.unitGenerator.name);
        $scope.temporaryUnit = angular.copy(angular.fromJson($scope.unitGenerator.unit));
        $scope.valueConnectedness = angular.copy($scope.generateValueFromRandomNumberForRepresentation($scope.temporaryUnit.connection.weight, null));
        $scope.is_loading = false;
        $scope.defaultParams();
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading unit detail', status, undefined));
        console.log('Error ' + data)
    });

    //todo brk functional properties deki kisimda sadece normal dist mi olacak?

    $scope.defaultParams = function () {

        $scope.providedService = {};

        $scope.editServiceClicked = false;
        $scope.editServiceIndex = undefined;

        $scope.editCommonPropertyClicked = false;
        $scope.editCommonPropertyIndex = undefined;

        $scope.valueToAdd = {};
        $scope.valueToAdd.params = {};
        $scope.valueToAdd.params.third = 1.0E-9;
        $scope.valueToAdd.class = 'NormalDistribution';

        $scope.privateProperty = {};

        $scope.privateProperty.type = "static";

        $scope.commonProperty = {};
        $scope.commonProperty.type = angular.copy($scope.privateProperty.type);
        $scope.commonProperty.interfaceClass = angular.copy($scope.privateProperty.interfaceClass);

        $scope.editFunctionalPropertyClicked = false;
        $scope.editFunctionalPropertyIndex = undefined;
        $scope.servicesIndexForEditFunctionalProperty = undefined;

        $scope.mappingValues = {};
        $rootScope.mappingValueArray = [];
    };

    $scope.saveService = function () {
        if (!$scope.editServiceClicked) {
            $scope.temporaryUnit.services.push(angular.copy($scope.providedService));
        }
        else {
            $scope.temporaryUnit.services[$scope.editServiceIndex].functionality = angular.copy($scope.providedService.functionality);
            $scope.temporaryUnit.services[$scope.editServiceIndex].probabilityToHave = angular.copy($scope.providedService.probabilityToHave);
        }

        $scope.defaultParams();
    };

    $scope.editServiceSetParams = function (index) {
        $scope.editServiceClicked = true;
        $scope.editServiceIndex = index;
        $scope.providedService.functionality = $scope.temporaryUnit.services[index].functionality;
        $scope.providedService.probabilityToHave = $scope.temporaryUnit.services[index].probabilityToHave;
    };

    $scope.deleteService = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the service "'+ $scope.temporaryUnit.services[index].functionality +'" at index #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryUnit.services.splice(index, 1);
        }, function (btn) {
        });
    };

    $scope.openAddPropertyModalForIndex = function (index) {
        $scope.servicesIndexForEditFunctionalProperty = index;
    };

    //todo brk genel olarak value veyahut weight ile ilgili kesin heryerde lazim mi notnull?

    $scope.savePrivateProperty = function () {
        if (!$scope.temporaryUnit.services[$scope.servicesIndexForEditFunctionalProperty].properties) {
            $scope.temporaryUnit.services[$scope.servicesIndexForEditFunctionalProperty].properties = [];
        }

        if (!$scope.privateProperty.interfaceClass) {
            delete $scope.privateProperty.interfaceClass;
        }

        //todo brk validate prob values every.
        $scope.privateProperty.value = angular.copy($scope.randomNumberGenerate(null, $scope.valueToAdd, $scope.mappingValues));

        if ($scope.editFunctionalPropertyClicked) {
            $scope.temporaryUnit.services[$scope.servicesIndexForEditFunctionalProperty].properties[$scope.editFunctionalPropertyIndex] = $scope.privateProperty;
        } else {
            $scope.temporaryUnit.services[$scope.servicesIndexForEditFunctionalProperty].properties.push($scope.privateProperty);
        }

        $scope.defaultParams();
    };

    $scope.openEditFunctionalProperty = function (serviceIndex, index) {
        $scope.editFunctionalPropertyClicked = true;
        $scope.servicesIndexForEditFunctionalProperty = serviceIndex;

        $scope.editFunctionalPropertyIndex = index;
        $scope.privateProperty = $scope.temporaryUnit.services[serviceIndex].properties[index];

        if ($scope.privateProperty.value) {
            $scope.valueToAdd = $scope.generateValueFromRandomNumberForRepresentation($scope.privateProperty.value, $scope.valueToAdd);
            $scope.valueToAdd.check = true;
        }

        if ($scope.privateProperty.value && $scope.privateProperty.value.mapping) {
            $scope.mappingValues = $scope.privateProperty.value.mapping;
            $scope.calculateNumberOfMappingValues($scope.valueToAdd);
        }
    };

    $scope.deleteFunctionalProperty = function () {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the functional property  "'+
            $scope.temporaryUnit.services[$scope.servicesIndexForEditFunctionalProperty].properties[$scope.editFunctionalPropertyIndex].name +'" at index #' + $scope.editFunctionalPropertyIndex + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryUnit.services[$scope.servicesIndexForEditFunctionalProperty].properties.splice($scope.editFunctionalPropertyIndex, 1);
        }, function (btn) {
        });

    };

    //todo brk functional property mecburi mi eklenmek zorunda mi?

    $scope.saveProperty = function () {

        if (!$scope.commonProperty.interfaceClass) {
            delete $scope.commonProperty.interfaceClass;
        }

        if ($scope.valueToAdd.check) {
            $scope.commonProperty.value = angular.copy($scope.randomNumberGenerate(null, $scope.valueToAdd, $scope.mappingValues));
        }

        if (!$scope.editCommonPropertyClicked) {
            $scope.temporaryUnit.commonProperties.push(angular.copy($scope.commonProperty));
        }
        else {
            $scope.temporaryUnit.commonProperties[$scope.editCommonPropertyIndex] = angular.copy($scope.commonProperty);
        }

        $scope.defaultParams();

    };

    $scope.editCommonProperty = function (index) {
        $scope.editCommonPropertyClicked = true;
        $scope.editCommonPropertyIndex = index;
        $scope.commonProperty = $scope.temporaryUnit.commonProperties[index];
        if ($scope.commonProperty.value) {
            $scope.valueToAdd = $scope.generateValueFromRandomNumberForRepresentation($scope.commonProperty.value, $scope.valueToAdd);
            $scope.valueToAdd.check = true;
        }
        if ($scope.commonProperty.value && $scope.commonProperty.value.mapping) {
            $scope.mappingValues = $scope.commonProperty.value.mapping;
            $scope.calculateNumberOfMappingValues($scope.valueToAdd);
        }

    };

    $scope.deleteProperty = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the property "'+ $scope.temporaryUnit.commonProperties[index].name +'" at index #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryUnit.commonProperties.splice(index, 1);
        }, function (btn) {
        });
    };

    //todo brk optional olabilen degerleri kontrol et.

    /* COMMON PROPERTY FINISH */

    //todo brk service.properties de interface class lazim mi?

    $scope.updateUnit = function () {

        $scope.valueConnectedness.class = 'NormalDistribution';
        $scope.valueConnectedness.check = true;
        $scope.valueConnectedness.params.third = 1.0E-9;
        $scope.temporaryUnit.connection.weight = angular.copy(
            $scope.randomNumberGenerate($scope.valueConnectedness, $scope.valueToAdd, $scope.mappingValues));

        dialogs.wait(undefined, 'saving unit', 99);
        var unitToSend = {
            'name': $scope.unitGeneratorName,
            'objectId': objectId,
            'unit': angular.toJson($scope.temporaryUnit, true)
        };

        //todo brk ask veyhut incele su hata olaylari 409 felan?

        $http({
            method: 'PUT',
            url: URL,
            data: $.param(unitToSend),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $location.path('/simulation-unit');
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error updating unit', status, {409: 'Unit with the same objectId exists'}));
            console.log('Error ' + data)
        });

    };

    //todo brk json icinde value olarak gösterilen random value mecburi degil sanirsam sor.^mecbur ise validerung koy.
    //todo brk onu checkbox ile istendiginde aktiv edecegim bunu sor mecburi olup olmadigini?

    $scope.backToHome = function () {
        $location.path('/simulation-unit');
    };

});