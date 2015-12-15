
var app = angular.module('hcuapp', [
    'ngCookies', 'ngResource', 'ngSanitize', 'ngRoute',
    'ui.bootstrap', 'dialogs.main', 'pascalprecht.translate', 'dialogs.default-translations'
]);

app.filter('takeLastWordAfterDot', function () {
    // function that's invoked each time Angular runs $digest()
    // pass in `item` which is the single Object we'll manipulate
    return function (item) {

        if(item === undefined) {
            return "";
        }

        return item.split(".").pop();
    };
});

app.config(function ($routeProvider) {
    $routeProvider.when('/peer', {
        templateUrl: 'views/list-peer.html',
        controller: 'PeerListCtrl'
    }).when('/simulation', {
        templateUrl: 'views/simulation/home.html',
        controller: 'SimulationCtrl'
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

function HeaderController($scope, $location, $rootScope)
{ 
    $scope.isActive = function (viewLocation) { 
        return viewLocation === $location.path();
    };



    // ========= default unit JSON ===========
    $rootScope.unit = {
        seed: 1001,
        numberOfElements: 200,
        namePrefix: "Citizen",
        singleElementSingleServices: true,
        connectedness: {
            probabilityToConnect: "1.0",
            weight: {
                clazz: "NormalDistribution",
                params: ["10", "1", "1.0E-9"]
            }
        },
        providedServices: [
            {
                functionality: "Collector",
                probabilityToHave: "0.7",
                properties: [
                    {
                        type: "skill",
                        name: "skill_collector",
                        value: {
                            clazz: "NormalDistribution",
                            params: ["0.7", "0.2", "1.0E-9"]
                        },
                        probabilityToHave: "1.0"
                    }
                ]
            },
            {
                functionality: "Assessor",
                probabilityToHave: "0.5",
                properties: [
                    {
                        type: "skill",
                        name: "skill_assessor",
                        value: {
                            clazz: "NormalDistribution",
                            params: ["0.7", "0.2", "1.0E-9"]
                        },
                        probabilityToHave: "1.0"
                    }
                ]
            }
        ],
        commonProperties: [
            {
                type: "static",
                name: "performance_rating",
                value: {
                    clazz: "NormalDistribution",
                    params: ["0.7", "0.2", "1.0E-9"]
                },
                probabilityToHave: "1.0"
            },
            {
                type: "static",
                name: "fault_probability",
                value: {
                    clazz: "NormalDistribution",
                    params: ["0.30", "0.10", "1.0E-9"]
                },
                probabilityToHave: "1.0"
            },
            {
                type: "static",
                name: "assignment_priority",

                integerValue: 1,
                probabilityToHave: "1.0"
            },
            {
                type: "metric",
                name: "reliability",
                interfaceClass: "at.ac.tuwien.dsg.hcu.cloud.monitor.ReliabilityMonitor",
                probabilityToHave: "1.0"
            },
            {
                type: "static",
                name: "cost",
                value: {
                    clazz: "NormalDistribution",
                    params: ["2", "0.5", "1.0E-9"]
                },
                probabilityToHave: "1.0"
            },
            {
                type: "static",
                name: "location",
                value: {
                    clazz: "UniformIntegerDistribution",
                    params: ["0", "2"],
                    mapping: {
                        first: "Sector-A", second: "Sector-B", third: "Sector-C"
                    }
                },
                probabilityToHave: "1.0"
            },
            {
                type: "metric",
                name: "response_time",
                interfaceClass: "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                probabilityToHave: "1.0"
            },
            {
                type: "metric",
                name: "execution_time",
                interfaceClass: "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                probabilityToHave: "1.0"
            },
            {
                type: "metric",
                name: "earliest_availability",
                interfaceClass: "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                probabilityToHave: "1.0"
            }
        ]
    };

    $rootScope.citizenUnit = angular.copy($scope.unit);
    $rootScope.sensorUnit = {
        "seed": 1001,
        "numberOfElements": 50,
        "namePrefix": "Sensor",
        "singleElementSingleServices": false,
        "connectedness": {
            "probabilityToConnect": "1.0",
            "weight": {"clazz": "NormalDistribution", "params": ["10", "1", "1.0E-9"]}
        },
        "providedServices": [
            {
                "functionality": "Sensor",
                "probabilityToHave": "1.0",
                "properties": []
            }
        ],
        "commonProperties": [
            {
                "type": "static",
                "name": "performance_rating",
                "value": {"clazz": "NormalDistribution", "params": ["0.97", "0.01", "1.0E-9"]},
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "fault_rate",
                "value": {"clazz": "NormalDistribution", "params": ["0.01", "0.005", "1.0E-9"]},
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "reliability",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.cloud.monitor.ReliabilityMonitor",
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "cost",
                "value": {"clazz": "NormalDistribution", "params": ["3", "0.5", "1.0E-9"]},
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "location",
                "value": {
                    "clazz": "UniformIntegerDistribution", "params": ["0", "2"],
                    "mapping": {first: "Sector-A", second: "Sector-B", third: "Sector-C"}

                },
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "response_time",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "execution_time",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "earliest_availability",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                "probabilityToHave": "1.0"
            }
        ]
    };
    $rootScope.surveyorUnit = {
        "seed": 1001,
        "numberOfElements": 10,
        "namePrefix": "Surveyor",
        "singleElementSingleServices": true,
        "connectedness": {
            "probabilityToConnect": "1.0",
            "weight": {"clazz": "NormalDistribution", "params": ["10", "1", "1.0E-9"]}
        },
        "providedServices": [
            {
                "functionality": "Collector",
                "probabilityToHave": "0.7",
                "properties": [
                    {
                        "type": "skill",
                        "name": "skill_collector",
                        "value": {"clazz": "NormalDistribution", "params": ["0.7", "0.2", "1.0E-9"]},
                        "probabilityToHave": "1.0"
                    }
                ]
            },
            {
                "functionality": "Assessor",
                "probabilityToHave": "0.5",
                "properties": [
                    {
                        "type": "skill",
                        "name": "skill_assessor",
                        "value": {"clazz": "NormalDistribution", "params": ["0.7", "0.2", "1.0E-9"]},
                        "probabilityToHave": "1.0"
                    }
                ]
            }
        ],
        "commonProperties": [
            {
                "type": "static",
                "name": "performance_rating",
                "value": {"clazz": "NormalDistribution", "params": ["0.95", "0.01", "1.0E-9"]},
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "fault_probability",
                "value": {"clazz": "NormalDistribution", "params": ["0.05", "0.01", "1.0E-9"]},
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "assignment_priority", // saved as integer in model
                "_value": 3, //todo @karaoglan there already exist value object this is another thing
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "reliability",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.cloud.monitor.ReliabilityMonitor",
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "cost",
                "value": {"clazz": "NormalDistribution", "params": ["5", "0.5", "1.0E-9"]},
                "probabilityToHave": "1.0"
            },
            {
                "type": "static",
                "name": "location",
                "value": {
                    "clazz": "UniformIntegerDistribution", "params": ["0", "2"],
                    "mapping": {first: "Sector-A", second: "Sector-B", third: "Sector-C"}
                },
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "response_time",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "execution_time",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                "probabilityToHave": "1.0"
            },
            {
                "type": "metric",
                "name": "earliest_availability",
                "interfaceClass": "at.ac.tuwien.dsg.hcu.simulation.adapter.gridsim.monitor.GSAvailabilityMonitor",
                "probabilityToHave": "1.0"
            }
        ]
    };

    // ==========DEFAULT UNIT JSON END==========
    $rootScope.unitValues = [
        {
            clazz: "NormalDistribution",
            params: ["0.30", "0.10", "1.0E-9"]
        },
        {
            clazz: "NormalDistribution",
            params: ["0.7", "0.2", "1.0E-9"]
        },
        {
            clazz: "NormalDistribution",
            params: ["10", "1", "1.0E-9"]
        },
        {
            clazz: "NormalDistribution",
            params: ["2", "0.5", "1.0E-9"]
        },
        {
            clazz: "UniformIntegerDistribution",
            params: ["0", "2"]
        }
    ];
    /* default unit json end */

    /* =============== TASK START ================= */


    // ========DEFAULT JSON TASK ======
    $rootScope.task = {
        "seed": 1001,
        "taskTypes":  //todo @karaoglan ask array needed for taskType there is always just one type in json?
        {
            "name": "MachineSensing",
            "description": "MachineSensing",
            "isRootTask": true,
            "tasksOccurance": {"clazz": "java.lang.Integer", "params": ["10"], "sampleMethod": "doubleValue"},
            "load": {"clazz": "NormalDistribution", "params": ["3", "0.5", "1.0E-9"]},
            "roles": [
                {
                    "functionality": "Sensor",
                    "probabilityToHave": "1.0",
                    "relativeLoadRatio": "1.0",
                    "specification": []
                }
            ],
            "subTaskTypes": [],
            "specification": [
                {
                    "type": "static",
                    "name": "deadline",
                    "value": {"clazz": "NormalDistribution", "params": ["1000", "10", "1.0E-9"]},
                    "probabilityToHave": "1.0"
                },
                {
                    "type": "static",
                    "name": "cost_limit",
                    "value": {"clazz": "NormalDistribution", "params": ["1000", "1", "1.0E-9"]},
                    "probabilityToHave": "1.0"
                },
                {
                    "type": "static",
                    "name": "connectedness",
                    "value": {
                        "clazz": "UniformIntegerDistribution", "params": ["0", "1"],
                        "mapping": {"first": "poor", "second": "fair", "third": "good", "fourth": "very_good"}
                    },
                    "probabilityToHave": "1.0"
                },
                {
                    "type": "static",
                    "name": "location",
                    "value": {
                        "clazz": "UniformIntegerDistribution", "params": ["0", "2"],
                        "mapping": {"first": "Sector-A", "second": "Sector-B", "third": "Sector-C"}
                    },
                    "probabilityToHave": "1.0",
                    "comparator": "at.ac.tuwien.dsg.hcu.common.sla.comparator.StringComparator"
                }
            ]
        }



    };
    $rootScope.humanSensing = {
        "seed": 1001,
        "taskTypes":
            {
                "name": "HumanSensing",
                "description": "HumanSensing",
                "isRootTask": true,
                "tasksOccurance": {"clazz": "java.lang.Integer", "params": ["20"], "sampleMethod": "doubleValue"},
                "load": {"clazz": "NormalDistribution", "params": ["3", "0.5", "1.0E-9"]},
                "roles": [
                    {
                        "functionality": "Collector",
                        "probabilityToHave": "1.0",
                        "relativeLoadRatio": "1.0",
                        "specification": [],
                        "_specification": [
                            {
                                "type": "skill",
                                "name": "skill_collector",
                                "value": {
                                    "clazz": "UniformIntegerDistribution", "params": ["0", "1"],
                                    "mapping": {"first": "poor", "second": "fair", "third": "good", "fourth": "very_good"}
                                },
                                "probabilityToHave": "1.0",
                                "comparator": "at.ac.tuwien.dsg.hcu.common.sla.comparator.FuzzyComparator"
                            }
                        ]
                    },
                    {
                        "functionality": "Assessor",
                        "probabilityToHave": "1.0",
                        "relativeLoadRatio": "1.0",
                        "dependsOn": ["*Collector"],
                        "specification": [],
                        "_specification": [
                            {
                                "type": "skill",
                                "name": "skill_assessor",
                                "value": {
                                    "clazz": "UniformIntegerDistribution", "params": ["0", "2"],
                                    "mapping": {"first": "poor", "second": "fair", "third": "good", "fourth": "very_good"}
                                },
                                "probabilityToHave": "1.0",
                                "comparator": "at.ac.tuwien.dsg.hcu.common.sla.comparator.FuzzyComparator"
                            }
                        ]
                    }
                ],
                "subTaskTypes": [],
                "specification": [
                    {
                        "type": "static",
                        "name": "deadline",
                        "value": {"clazz": "NormalDistribution", "params": ["10000", "10", "1.0E-9"]},
                        "probabilityToHave": "1.0"
                    },
                    {
                        "type": "static",
                        "name": "cost_limit",
                        "value": {"clazz": "NormalDistribution", "params": ["1000", "1", "1.0E-9"]},
                        "probabilityToHave": "1.0"
                    },
                    {
                        "type": "static",
                        "name": "connectedness",
                        "value": {
                            "clazz": "UniformIntegerDistribution", "params": ["0", "1"],
                            "mapping": {"first": "poor", "second": "fair", "third": "good", "fourth": "very_good"}
                        },
                        "probabilityToHave": "1.0"
                    },
                    {
                        "type": "static",
                        "name": "location",
                        "value": {
                            "clazz": "UniformIntegerDistribution", "params": ["0", "2"],
                            "mapping": {"first": "Sector-A", "second": "Sector-B", "third": "Sector-C"}
                        },
                        "probabilityToHave": "1.0",
                        "comparator": "at.ac.tuwien.dsg.hcu.common.sla.comparator.StringComparator"
                    }
                ]
            }



    };
    $rootScope.maschineSensing = angular.copy($scope.task);
    $rootScope.mixedSensing = {
        "seed": 1001,
        "taskTypes":
            {
                "name": "MixedSensing",
                "description": "MixedSensing",
                "isRootTask": true,
                "tasksOccurance": {"clazz": "java.lang.Integer", "params": ["5"], "sampleMethod": "doubleValue"},
                "load": {"clazz": "NormalDistribution", "params": ["3", "0.5", "1.0E-9"]},
                "roles": [
                    {
                        "functionality": "Collector",
                        "probabilityToHave": "1.0",
                        "relativeLoadRatio": "1.0",
                        "specification": []
                    },
                    {
                        "functionality": "Assessor",
                        "probabilityToHave": "1.0",
                        "relativeLoadRatio": "1.0",
                        "dependsOn": ["*Collector"],
                        "specification": []
                    },
                    {
                        "functionality": "Sensor",
                        "probabilityToHave": "1.0",
                        "relativeLoadRatio": "1.0",
                        "specification": []
                    }
                ],
                "subTaskTypes": [],
                "specification": [
                    {
                        "type": "static",
                        "name": "deadline",
                        "value": {"clazz": "NormalDistribution", "params": ["1000", "10", "1.0E-9"]},
                        "probabilityToHave": "1.0"
                    },
                    {
                        "type": "static",
                        "name": "cost_limit",
                        "value": {"clazz": "NormalDistribution", "params": ["1000", "1", "1.0E-9"]},
                        "probabilityToHave": "1.0"
                    },
                    {
                        "type": "static",
                        "name": "connectedness",
                        "value": {
                            "clazz": "UniformIntegerDistribution", "params": ["0", "1"],
                            "mapping": {"first": "poor", "second": "fair", "third": "good", "fourth": "very_good"}
                        },
                        "probabilityToHave": "1.0"
                    },
                    {
                        "type": "static",
                        "name": "location",
                        "value": {
                            "clazz": "UniformIntegerDistribution", "params": ["0", "2"],
                            "mapping": {"first": "Sector-A", "second": "Sector-B", "third": "Sector-C"}
                        },
                        //todo @karaoglan ask why _compa --fixed remove underscore
                        "comparator": "at.ac.tuwien.dsg.hcu.common.sla.comparator.StringComparator",
                        "probabilityToHave": "1.0"
                    }
                ]
            }



    };

    // =========== DEFAULT JSON TASK END

    $rootScope.taskValues = [
        {
            clazz: "NormalDistribution",
            params: ["10000", "10", "1.0E-9"]
        },
        {
            clazz: "NormalDistribution",
            params: ["1000", "1", "1.0E-9"]
        },
        {
            clazz: "NormalDistribution",
            params: ["10", "1", "1.0E-9"]
        },
        {
            clazz: "NormalDistribution",
            params: ["2", "0.5", "1.0E-9"]
        },
        {
            clazz: "UniformIntegerDistribution",
            params: ["0", "1"],
            "mapping": {"first": "poor", "second": "fair", "third": "good", "fourth": "very_good"}
        }
    ];

}

