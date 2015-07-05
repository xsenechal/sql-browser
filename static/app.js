angular.module('sql-browser', ['ui.bootstrap', 'cfp.hotkeys'])
    .controller('MainCtrl', MainCtrl);

function MainCtrl($scope, $http, $log, hotkeys, $document, $modal, orderByFilter) {
    $scope.con = {
        url: 'jdbc:mysql://localhost:3306/wp',
        user: "root",
        password: ""
    };
    $scope.request = 'select * from information_schema.tables;'//"select * from users;"
    $scope.headers = [];
    $scope.exeRequest = function(){
       $http.post('request', {con:$scope.con, request: $scope.request}).success(function(rows){
           $scope.headers = _.keys(rows[0]);
           $scope.rows = rows;
       });
    };
    hotkeys.add({
        combo: 'ctrl+enter',
        callback: function(e) {
            e.preventDefault();
            $scope.exeRequest();
        }
    });

}