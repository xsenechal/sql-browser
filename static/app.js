angular.module('sql-browser', ['ui.bootstrap', 'cfp.hotkeys', 'ngStorage'])
    .controller('MainCtrl', MainCtrl);

function MainCtrl($scope, $http, $log, hotkeys, $localStorage, $document, $modal, orderByFilter) {
    $scope.$storage = $localStorage;
    $scope.requestMode = 'Manual';
    $scope.request = 'select * from TDO210_DOC_ID FETCH FIRST 10 ROWS ONLY;'//"select * from users;"
    $scope.headers = [];
    $scope.exeRequest = function(){
       $http.post('request', {con:$scope.$storage.con, request: $scope.request}).success(function(rows){
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
        $http.post('tables', {con:$scope.$storage.con}).success(function(rows){
            $scope.tables = rows;
        });
    };
    $scope.getTables();
    $scope.onTableSelect = function($item, $model, $label){
        $scope.getColumns($item.TABLE_NAME);
    };
    $scope.columns = [];
    $scope.criterias = {};
    $scope.getColumns = function(tableName){
        $http.post('columns/' + tableName, {con:$scope.$storage.con}).success(function(columns){
            $scope.columns = columns;
        });
    };


}