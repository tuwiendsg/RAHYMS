/**
 * Created by karaoglan on 05/10/15.
 */

app.controller('SimulationCtrl', function ($rootScope, $scope, $http, $location, dialogs) {
    $scope.Util = Util;
    $scope.is_loading = true;

    $scope.loaded = "page successfully loaded!";

    /* home */

    $scope.taskGeneratorList = function () {
        $scope.showTaskGenerators = true;
        $scope.showUnitGenerators = false;
    };

    $scope.temporaryTask = {};

    var taskDetailIndex = undefined;

    $scope.taskDetail = function (index) {
        if(!$scope.deleteTaskOrUnitClicked) {
            taskDetailIndex = index;
            $scope.showTaskDetail = true;
            $scope.showUnitDetail = false;
            $scope.showUnitGenerators = false;
            $scope.showTaskGenerators = false;
            $scope.taskGeneratorName = angular.copy($scope.tasks[index].name);

            if (!$scope.tasks[index].task) {
                $scope.temporaryTask = angular.copy($scope.task);
            } else {
                $scope.temporaryTask = $scope.tasks[index].task;
            }
            $scope.temporaryTaskValues = angular.copy($scope.taskValues);
            $scope.refreshPlacesForLoad();
        }

        $scope.deleteTaskOrUnitClicked = false;
    };

    /* ================= UNIT START ===========================*/

    /* UNIT VALUES GENERAL */

    $scope.unitGeneratorList = function () {
        $scope.showUnitGenerators = true;
        $scope.showTaskGenerators = false;
    };

    $scope.temporaryUnit = {};

    var unitDetailIndex = undefined;

    $scope.unitDetail = function (index) {
        if(!$scope.deleteTaskOrUnitClicked) {
            unitDetailIndex = index;
            $scope.showUnitDetail = true;
            $scope.showTaskDetail = false;
            $scope.showUnitGenerators = false;
            $scope.showTaskGenerators = false;
            $scope.unitGeneratorName = angular.copy($scope.units[index].name);

            if (!$scope.units[index].unit) {
                $scope.temporaryUnit = angular.copy($scope.unit);
            } else {
                $scope.temporaryUnit = $scope.units[index].unit;
            }
            $scope.temporaryUnitValues = angular.copy($scope.unitValues);
            $scope.refreshPlaceholderWeight();
        }
        $scope.deleteTaskOrUnitClicked = false;
    };

    /*  END HOME */


    /* UNIT DETAIL DEFAULT */

    $scope.saveService = function () {
        $scope.temporaryUnit.providedServices.push($scope.providedService);
    };

    $scope.deleteService = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the service #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryUnit.providedServices.splice(index, 1);
        }, function (btn) {
        });
    };

    /*
     todo @karaoglan add validation for fields
     */

    $scope.providedService = {};

    /* SELECT AND DELETE COMMON PROPERTY */
    $scope.addPropForServiceAtIndex = undefined;

    $scope.privateProperty = {};


    $scope.openAddPropertyModalForIndex = function (index) {
        $scope.addPropForServiceAtIndex = index;
    };

    $scope.savePrivateProperty = function () {
        if (!$scope.temporaryUnit.providedServices[$scope.addPropForServiceAtIndex].properties) {
            $scope.temporaryUnit.providedServices[$scope.addPropForServiceAtIndex].properties = [];
        }
        $scope.temporaryUnit.providedServices[$scope.addPropForServiceAtIndex].properties.push($scope.privateProperty);
        $scope.privateProperty = {};
    };

    $scope.commonProperty = {};

    $scope.saveProperty = function () {
        $scope.temporaryUnit.commonProperties.push($scope.commonProperty);
        $scope.commonProperty = {};
    };

    $scope.deleteProperty = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the property #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryUnit.commonProperties.splice(index, 1);
        }, function (btn) {
        });
    };

    /* COMMON PROPERTY FINISH */

    /* VALUE */

    $scope.selectionValues = [];

    $scope.selectionValue = function (idx) {

        var pos = $scope.selectionValues.indexOf(idx);

        if ($scope.selectionValues.length > 0 && pos == -1) {
            return;
        }

        if (pos == -1) {
            $scope.selectionValues.push(idx);
        } else {
            $scope.selectionValues.splice(pos, 1);
        }
    };

    $scope.valueAddForPrivateClicked = false;


    $scope.addValueForPrivateProperty = function () {
        $scope.valueAddForPrivateClicked = true;
    };

    $scope.valueAddForCommonClicked = false;
    $scope.valueAddForCommonAtIndex = undefined;

    $scope.addValueForCommonProperty = function (index) {
        $scope.valueAddForCommonAtIndex = index;
        $scope.valueAddForCommonClicked = true;
    };

    $scope.valueAddForWeightClicked = false;

    $scope.addValueForWeight = function () {
        $scope.valueAddForWeightClicked = true;
    };

    $scope.selectValue = function () {
        var ind = $scope.selectionValues[0];

        $scope.weightObj = {};

        $scope.weightObj.clazz = $scope.temporaryUnitValues[ind].clazz;
        $scope.weightObj.params = $scope.temporaryUnitValues[ind].params;
        $scope.weightObj.mapping = $scope.temporaryUnitValues[ind].mapping;

        if ($scope.valueAddForCommonClicked) {
            $scope.temporaryUnit.commonProperties[$scope.valueAddForCommonAtIndex].value = angular.copy($scope.weightObj);
        } else if ($scope.valueAddForWeightClicked) {
            $scope.temporaryUnit.connectedness.weight = angular.copy($scope.weightObj);
            $scope.refreshPlaceholderWeight();
        } else {
            $scope.privateProperty.value = angular.copy($scope.weightObj);
            $scope.refreshPlaceholderPrivateWeight();
        }

        $scope.valueAddForCommonClicked = false;
        $scope.valueAddForWeightClicked = false;
        $scope.valueAddForPrivateClicked = false;

        $scope.selectionValues = [];
    };

    $scope.refreshPlaceholderPrivateWeight = function () {
        $scope.placeholderForPrivateWeight = $scope.weightObj.clazz + ' { ' + $scope.weightObj.params + ' } ';
    };

    $scope.refreshPlaceholderWeight = function () {
        $scope.placeholderForWeight = $scope.temporaryUnit.connectedness.weight.clazz + ' { ' + $scope.temporaryUnit.connectedness.weight.params + ' } ';
    };

    $scope.valueToAdd = {};

    $scope.saveValue = function () {

        var valueValidated = {};
        valueValidated.clazz = $scope.valueToAdd.clazz;
        valueValidated.params = [];
        valueValidated.mapping = {};
        valueValidated.params.push($scope.valueToAdd.params.first);
        valueValidated.params.push($scope.valueToAdd.params.second);

        if ($scope.valueToAdd.params.third) {
            valueValidated.params.push($scope.valueToAdd.params.third);
        }

        if ($scope.valueToAdd.mapping) {
            valueValidated.mapping = $scope.valueToAdd.mapping;
        }

        $scope.temporaryUnitValues.push(valueValidated);

        $scope.valueToAdd = {};
    };


    $scope.deleteValue = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the value #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryUnitValues.splice(index, 1);
        }, function (btn) {
        });
    };

    /* END UNIT VALUES GENERAL */


    /* VALUE FINISH */

    $scope.startSimulation = function () {

        $http({
            method: 'POST',
            url: '/rest/api/simulation',
            data: $scope.simulationData
        }).success(function (response) {
            console.log("response from simulation start is : " + response);
        }).error(function (response, status) {
            dialogs.error(undefined, Util.error('Error starting simulation',
                status, {404: 'Simulation not found', 503: 'Simulation Service unavailable'}));
        })
    };

    /* UNIT DETAIL DEFAULT END */

    $scope.backToHome = function () {

        if ($scope.showTaskDetail) {
            $scope.showTaskDetail = false;
            $scope.showTaskGenerators = true;
        } else if ($scope.showUnitDetail) {
            $scope.showUnitDetail = false;
            $scope.showUnitGenerators = true;
        } else {
            $scope.showUnitGenerators = false;
            $scope.showTaskGenerators = false;
            $scope.showTaskDetail = false;
            $scope.showUnitDetail = false;
        }


    };

    /*  LISTS UNITS AND TASKS */

    /* ================= UNIT END ===========================*/

    $scope.units = [
        {
            name: 'citizen-generator',
            unit: $scope.citizenUnit
        },
        {
            name: 'sensor-generator',
            unit: $scope.sensorUnit
        },
        {
            name: 'surveyor-generator',
            unit: $scope.surveyorUnit
        }
    ];


    $scope.deleteUnit = function (index) {
        $scope.deleteTaskOrUnitClicked = true;
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the unit #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.units.splice(index, 1);
        }, function (btn) {
        });

    };

    /* LIST END */


    //TASK START
    // ====== ADD TASK OR UNIT ===========

    $scope.tasks = [
        {
            name: 'human-sensing-task',
            task: $scope.humanSensing
        },
        {
            name: 'machine-sensing-task',
            task: $scope.maschineSensing
        },
        {
            name: 'mixed-sensing-task',
            task: $scope.mixedSensing
        }
    ];

    $scope.simulationData = {
        "tasks": $scope.tasks,
        "units": $scope.units
    };

    $scope.deleteTaskOrUnitClicked = false;

    $scope.deleteTask = function (index) {
        $scope.deleteTaskOrUnitClicked = true;
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the task #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.tasks.splice(index, 1);
        }, function (btn) {
        });
    };

    $scope.addTask = {};
    $scope.addUnit = {};

    $scope.saveTaskOrUnit = function () {
        console.log("saveTaskOrUnit clicked");

        var tempObject = {};

        if ($scope.showUnitDetail) {
            console.log("unit detail");
            tempObject.name = $scope.unitGeneratorName;
            tempObject.unit = $scope.temporaryUnit;
            $scope.units[unitDetailIndex] = tempObject;

            $scope.showUnitDetail = false;
            $scope.showUnitGenerators = true;
        } else {
            console.log("task detail");
            //todo same functionality as unit but for temporaryTask

            $scope.showTaskDetail = false;
            $scope.showTaskGenerators = true;
        }
    };

    $scope.createTask = function () {
        $scope.tasks.push($scope.addTask);
        $scope.addTask = {};
    };

    $scope.createUnit = function () {
        $scope.units.push($scope.addUnit);
        $scope.addUnit = {};
    };

    // ======= ADD TASK OR UNIT END ========


    $scope.deleteRole = function (index) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete the role #' + index + '?');
        dlg.result.then(function (btn) {
            $scope.temporaryTaskValues.splice(index, 1);
        }, function (btn) {
        });
    };

    $scope.addSpecForRoleAtIndex = undefined;

    $scope.privateSpecification = {};

    $scope.openAddPrivateSpecificationModalForIndex = function (index) {
        $scope.addPropForServiceAtIndex = index;
    };

    $scope.selectionTaskValues = [];

    $scope.selectionTaskPrivateSpecValue = function (idx) {

        var pos = $scope.selectionTaskValues.indexOf(idx);

        if ($scope.selectionTaskValues.length > 0 && pos == -1) {
            return;
        }

        if (pos == -1) {
            $scope.selectionTaskValues.push(idx);
        } else {
            $scope.selectionTaskValues.splice(pos, 1);
        }
    };

    $scope.valueAddForPrivateSpecClicked = false;

    $scope.addValueForPrivateSpecification = function () {
        $scope.valueAddForPrivateSpecClicked = true;
    };

    var privateSpecValueObj = {};

    $scope.selectTaskPrivateSpecValue = function () {
        var ind = $scope.selectionTaskValues[0];

        privateSpecValueObj.clazz = $scope.temporaryTaskValues[ind].clazz;
        privateSpecValueObj.params = $scope.temporaryTaskValues[ind].params;
        privateSpecValueObj.mapping = $scope.temporaryTaskValues[ind].mapping;

        if (!$scope.valueAddForPrivateClicked) {
            $scope.temporaryUnit.connectedness.weight = privateSpecValueObj;
            $scope.refreshPlaceholderWeight();
        } else {
            $scope.privateProperty.value = privateSpecValueObj;
            $scope.refreshPlaceholderPrivateValue();
        }

        privateSpecValueObj = {};
        $scope.selectionTaskValues = [];
    };

    $scope.refreshPlacesForLoad = function() {
        //$scope.placeholderForOccurance = $scope.temporaryTask.taskTypes.tasksOccurance.class +
        //    ' { ' + $scope.temporaryTask.taskTypes.tasksOccurance.params + ' | ' + $scope.temporaryTask.taskTypes.tasksOccurance.sampleMethod + ' } ';
        $scope.placeholderForLoad      = $scope.temporaryTask.taskTypes.load.clazz + ' { ' + $scope.temporaryTask.taskTypes.load.params + ' } ';
    };

//--
    $scope.valueAddForLoadClicked = false;

    $scope.addValueForWeight = function () {
        $scope.valueAddForLoadClicked = true;
    };

    $scope.selectValue = function () {
        var ind = $scope.selectionValues[0];

        $scope.taskWeightObj = {};

        $scope.taskWeightObj.clazz = $scope.temporaryUnitValues[ind].clazz;
        $scope.taskWeightObj.params = $scope.temporaryUnitValues[ind].params;
        $scope.taskWeightObj.mapping = $scope.temporaryUnitValues[ind].mapping;

        if ($scope.valueAddForCommonClicked) {
            $scope.temporaryUnit.commonProperties[$scope.valueAddForCommonAtIndex].value = angular.copy($scope.taskWeightObj);
        } else {
            $scope.privateProperty.value = angular.copy($scope.taskWeightObj);
            $scope.refreshPlaceholderPrivateWeight();
        }

        $scope.valueAddForCommonClicked = false;
        $scope.valueAddForWeightClicked = false;
        $scope.valueAddForPrivateClicked = false;

        $scope.selectionValues = [];
    };




    //--
    $scope.refreshPlaceholderPrivateValue = function () {
        $scope.placeholderForPrivateValue = privateSpecValueObj.clazz + ' { ' + privateSpecValueObj.params + ' } ';
    };


    $scope.savePrivateSpecification = function () {
        if (!$scope.temporaryTask.taskTypes.roles[addSpecForRoleAtIndex]._specification) {
            $scope.temporaryTask.taskTypes.roles[addSpecForRoleAtIndex]._specification = [];
        }
        $scope.temporaryTask.taskTypes.roles[addSpecForRoleAtIndex]._specification.push($scope.privateSpecification);
    };

    $scope.saveRole = function () {
        $scope.temporaryTask.taskTypes.roles.push($scope.role);
    };

    var commonSpecValueObj = {};

    $scope.addValueForCommonSpecification = function ($index) {
        //todo @karaoglan change private to common obj ayni sekilde yukarida unit icinde aynisi gecerli
        $scope.temporaryTask.taskTypes.specification[$index].value = privateSpecValueObj;
    };


    /* ================== TASK END ========================*/

});

