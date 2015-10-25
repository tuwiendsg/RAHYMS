
app.controller('PeerListCtrl', function ($rootScope, $scope, $http, $location, dialogs) {
    $scope.Util = Util 
    $scope.is_loading = true;
    $http.get('/rest/api/peer?page=1').success(function (data) {
        $scope.peers = {};
        for (var i=0; i<data.length; i++) {
            $scope.peers[data[i].email] = data[i];
        }
        $scope.is_loading = false;
    }).error(function (data, status) {
        $scope.is_loading = false;
        dialogs.error(undefined, Util.error('Error loading peers', status, undefined));
        console.log('Error ' + data)
    })

    $scope.deletePeer = function (email) {
        var dlg = dialogs.confirm('Confirmation', 'Are you sure want to delete peer ' +  email + '?');
        dlg.result.then(function(btn){
            dialogs.wait(undefined, 'Deleting peer ' + email, 99);
            $http({
                method: 'DELETE',
                url: '/rest/api/peer/' + email,
            }).success(function (data) {
                $rootScope.$broadcast('dialogs.wait.complete');
                $location.path('/peer');
                delete $scope.peers[email];
            }).error(function (data, status) {
                $rootScope.$broadcast('dialogs.wait.complete');
                dialogs.error(undefined, Util.error('Error deleting peer', status, undefined));
                console.log('Error ' + data)
            })
        },function(btn){
        });
    }
});

app.controller('PeerCreateCtrl', function ($rootScope, $scope, $http, $location, dialogs) {

    $scope.peer = {};

    $scope.createPeer = function () {
        console.log($scope.peer);
        dialogs.wait(undefined, 'Creeating peers', 99);
        $http({
            method: 'POST',
            url: '/rest/api/peer',
            data: $.param($scope.peer),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $rootScope.$broadcast('dialogs.wait.complete');
            $location.path('/peer');
        }).error(function (data, status) {
            $rootScope.$broadcast('dialogs.wait.complete');
            dialogs.error(undefined, Util.error('Error creating peer', status, {409: 'Peer with the same email exists'}));
            console.log('Error ' + data)
        })
    }
});