app.controller('SimulationTaskDetailCtrl', function ($rootScope, $routeParams, $scope, $http, $location, dialogs) {

    const URL = '/rest/api/simulation-task';

    var objectId = $routeParams.objectId;

    dialogs.wait(undefined, 'Getting task', 99);
    $http.get(URL + "/" + objectId).success(function (data) {
        $rootScope.$broadcast('dialogs.wait.complete');
        $scope.taskGenerator = data;

        $scope.taskGeneratorName = angular.copy($scope.taskGenerator.name);
        $scope.temporaryTask = angular.copy(angular.fromJson($scope.taskGenerator.task));

        $scope.defaultTasksOccurance = $scope.temporaryTask.taskTypes[0].tasksOccurance;

        $scope.loadValue = $scope.generateValueFromRandomNumberForRepresentation($scope.temporaryTask.taskTypes[0].load);

        $scope.defaultParams();

    }).error(function (data, status) {
        $rootScope.$broadcast('dialogs.wait.complete');
        dialogs.error(undefined, Util.error('Error loading task detail', status, undefined));
        console.log('Error ' + data)
    });


    //todo brk non functional string compar degistirmiyor edit kismin iincele yeni eklerken de eklemiyor
    $scope.defaultParams = function () {

        $scope.role = {};
        $scope.valueToAdd = {};
        $scope.valueToAdd.params = {};
        $scope.valueToAdd.params.third = 1.0E-9;
        $scope.valueToAdd.class = 'NormalDistribution';

        $scope.privateSpecification = {};
        $scope.privateSpecification.type = "static";

        $scope.commonProperty = {};
        $scope.commonProperty.type = angular.copy($scope.privateSpecification.type);
        $scope.commonProperty.comparator = angular.copy($scope.privateSpecification.comparator);

        $scope.editCommonPropertyClicked = false;
        $scope.editCommonPropertyIndex = undefined;

        $scope.editRoleClicked = false;
        $scope.editRoleIndex = undefined;

        $scope.editFunctionalPropertyClicked = false;
        $scope.roleIndexForEditFunctionalProperty = undefined;
        $scope.editFunctionalPropertyIndex = undefined;

        $scope.dependsOn = [];
        $scope.dependsOnStrongValues = {};
        $scope.dependsOnValues = {};
        $scope.mappingValues = {};
        $rootScope.mappingValueArray = [];
    };

    $scope.editRoleSetParams = function (index) {
        $scope.editRoleClicked = true;
        $scope.editRoleIndex = index;
        $scope.role.functionality = $scope.temporaryTask.taskTypes[0].roles[index].functionality;
        $scope.role.probabilityToHave = $scope.temporaryTask.taskTypes[0].roles[index].probabilityToHave;
        $scope.role.relativeLoadRatio = $scope.temporaryTask.taskTypes[0].roles[index].relativeLoadRatio;
        $scope.calculateDependencyForEditRole($scope.temporaryTask.taskTypes[0].roles[index].dependsOn);
    };

    $scope.calculateDependencyForEditRole = function (depends) {
        angular.forEach(depends, function (value, key) {
            if (value.startsWith("*")) {
                var functionalityValue = value.substring(1);
                $scope.dependsOnValues[functionalityValue] = true;
                $scope.dependsOnStrongValues[functionalityValue] = true;
            } else if (value) {
                $scope.dependsOnValues[value] = true;
            }
        });
    };

    $scope.saveRole = function () {

        $scope.calculateDependency();
        $scope.role.dependsOn = $scope.dependsOn;

        if (!$scope.editRoleClicked) {
            if(!$scope.checkRoleFunctionalityForUnique($scope.role.functionality) ) {
                dialogs.notify(undefined, "Please enter unique functionality name!");
                return;
            }

            $scope.temporaryTask.taskTypes[0].roles.push(angular.copy($scope.role));
        } else {
            if(!$scope.checkRoleFunctionalityForUnique($scope.role.functionality, $scope.editRoleIndex) ) {
                dialogs.notify(undefined, "Please enter unique functionality name!");
                return;
            }

            $scope.temporaryTask.taskTypes[0].roles[$scope.editRoleIndex].functionality = angular.copy($scope.role.functionality);
            $scope.temporaryTask.taskTypes[0].roles[$scope.editRoleIndex].dependsOn = angular.copy($scope.role.dependsOn);
            $scope.temporaryTask.taskTypes[0].roles[$scope.editRoleIndex].probabilityToHave = angular.copy($scope.role.probabilityToHave);
            $scope.temporaryTask.taskTypes[0].roles[$scope.editRoleIndex].relativeLoadRatio = angular.copy($scope.role.relativeLoadRatio);
        }

        //todo brk bak su an bos array atiyor sorun var mi dependson da algorithmada

        $scope.defaultParams();
    };

    $scope.checkRoleFunctionalityForUnique = function (func, index) {
        for (var i = 0; i < $scope.temporaryTask.taskTypes[0].roles.length; i++) {

            if (index !== undefined && i === index) continue;

            if ($scope.temporaryTask.taskTypes[0].roles[i].functionality === func) {
                return false;
            }
        }
        return true;


    };

    $scope.calculateDependency = function () {
        angular.forEach($scope.dependsOnValues, function (value, key) {
            if (value) {
                if ($scope.dependsOnStrongValues[key]) {
                    $scope.dependsOn.push("*" + key);
                } else {
                    $scope.dependsOn.push(key);
                }
            }
        });
    };

    $scope.deleteRole = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the role "' + $scope.temporaryTask.taskTypes[0].roles[index].functionality + '" at index #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryTask.taskTypes[0].roles.splice(index, 1);
        }, function (btn) {
        });
    };

    $scope.openEditFunctionalProperty = function (roleIndex, index) {
        $scope.editFunctionalPropertyClicked = true;
        $scope.roleIndexForEditFunctionalProperty = roleIndex;

        $scope.editFunctionalPropertyIndex = index;
        $scope.privateSpecification = $scope.temporaryTask.taskTypes[0].roles[roleIndex].specification[index];

        if ($scope.privateSpecification.value) {
            $scope.valueToAdd = $scope.generateValueFromRandomNumberForRepresentation($scope.privateSpecification.value, $scope.valueToAdd);
            $scope.valueToAdd.check = true;
        }

        if ($scope.privateSpecification.value.params && $scope.privateSpecification.value.mapping) {
            $scope.mappingValues = $scope.privateSpecification.value.mapping;
            $scope.calculateNumberOfMappingValues($scope.valueToAdd);
        }
    };

    $scope.openAddPropertyModalForIndex = function (index) {
        $scope.roleIndexForEditFunctionalProperty = index;
    };

    //functional
    $scope.savePrivateSpecification = function () {
        if (!$scope.temporaryTask.taskTypes[0].roles[$scope.roleIndexForEditFunctionalProperty].specification) {
            $scope.temporaryTask.taskTypes[0].roles[$scope.roleIndexForEditFunctionalProperty].specification = [];
        }

        if (!$scope.privateSpecification.comparator) {
            delete $scope.privateSpecification.comparator;
        }

        //todo brk validate prob values every.
        $scope.privateSpecification.value = angular.copy($scope.randomNumberGenerate(null, $scope.valueToAdd, $scope.mappingValues));

        if ($scope.editFunctionalPropertyClicked) {
            $scope.temporaryTask.taskTypes[0].roles[$scope.roleIndexForEditFunctionalProperty].specification[$scope.editFunctionalPropertyIndex] = $scope.privateSpecification;
        } else {
            $scope.temporaryTask.taskTypes[0].roles[$scope.roleIndexForEditFunctionalProperty].specification.push($scope.privateSpecification);
        }

        $scope.defaultParams();
    };

    //non functional
    $scope.saveProperty = function () {

        if (!$scope.commonProperty.interfaceClass) {
            delete $scope.commonProperty.interfaceClass;
        }

        if ($scope.valueToAdd.check) {
            $scope.commonProperty.value = angular.copy($scope.randomNumberGenerate(null, $scope.valueToAdd, $scope.mappingValues));
        }

        if (!$scope.editCommonPropertyClicked) {
            $scope.temporaryTask.taskTypes[0].specification.push(angular.copy($scope.commonProperty));
        }
        else {
            $scope.temporaryTask.taskTypes[0].specification[$scope.editCommonPropertyIndex] = angular.copy($scope.commonProperty);
        }

        $scope.defaultParams();

    };

    $scope.deleteFunctionalProperty = function () {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the functional property "' + $scope.temporaryTask.taskTypes[0].roles[$scope.roleIndexForEditFunctionalProperty].specification
                [$scope.editFunctionalPropertyIndex].name + '" at index #' + $scope.editFunctionalPropertyIndex + '?');

        dlg.result.then(function (btn) {
            $scope.temporaryTask.taskTypes[0].roles[$scope.roleIndexForEditFunctionalProperty].specification.splice($scope.editFunctionalPropertyIndex, 1);
        }, function (btn) {
        });

    };

    $scope.editCommonProperty = function (index) {
        $scope.editCommonPropertyClicked = true;
        $scope.editCommonPropertyIndex = index;
        $scope.commonProperty = $scope.temporaryTask.taskTypes[0].specification[index];
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
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the property "' + $scope.temporaryTask.taskTypes[0].specification[index].name + '" at index #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryTask.taskTypes[0].specification.splice(index, 1);
        }, function (btn) {
        });
    };


    $scope.updateTask = function () {

        if (!$scope.temporaryTask.taskTypes[0].tasksOccurance) {
            $scope.temporaryTask.taskTypes[0].tasksOccurance = $scope.defaultTasksOccurance;
        }

        $scope.loadValue.class = 'NormalDistribution';
        $scope.loadValue.check = true;
        $scope.loadValue.params.third = 1.0E-9;
        $scope.temporaryTask.taskTypes[0].load = angular.copy(
            $scope.randomNumberGenerate($scope.loadValue, $scope.valueToAdd, $scope.mappingValues));

        dialogs.wait(undefined, 'saving task', 99);
        var taskToUpdate = {
            'name': $scope.taskGeneratorName,
            'task': angular.toJson($scope.temporaryTask, true)
        };

        $http({
            method: 'PUT',
            url: URL + '/' + objectId,
            data: $.param(taskToUpdate),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $location.path('/simulation-task');
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error updating task', status, {409: 'todo brk look with the same exists'}));
            console.log('Error ' + data)
        });

    };

    $scope.backToHome = function () {
        var dlg = dialogs.confirm('Confirmation', 'Any unsaved changes will be lost. Continue?');
        dlg.result.then(function (btn) {
            $location.path('/simulation-task');
        }, function (btn) {
        });
    };

});