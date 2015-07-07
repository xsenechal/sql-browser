angular.module('sql-browser', ['ui.bootstrap', 'cfp.hotkeys'])
    .controller('MainCtrl', MainCtrl);

function MainCtrl($scope, $http, $log, hotkeys, $document, $modal, orderByFilter) {
    $scope.requestMode = 'Manual';
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
    $scope.tables = [];
    $scope.getTables = function(){
        $http.post('tables', {con:$scope.con}).success(function(rows){
            $scope.tables = rows;
        });
    };
    $scope.getTables();
    $scope.onTableSelect = function($item, $model, $label){
        $scope.getColumns($item.TABLE_NAME);
    };
    $scope.metadata = [];
    $scope.criterias = {};
    $scope.getColumns = function(tableName){
        $http.post('metadata/' + tableName, {con:$scope.con}).success(function(metadata){
            $scope.metadata = metadata;
        });
    };


}