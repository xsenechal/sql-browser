angular.module('sql-browser', ['ui.bootstrap', 'cfp.hotkeys'])
    .controller('MainCtrl', MainCtrl);

function MainCtrl($scope, $http, $log, hotkeys, $document, $modal, orderByFilter) {
    $scope.requestMode = 'Form';
    $scope.con = {
        url: 'jdbc:mysql://localhost:3306/wp',
        user: "root",
        pwd: ""
    };
    $scope.request = 'select * from information_schema.tables;';//"select * from users;"
    $scope.headers = [];
    $scope.exeRequest = function(){
        switch ($scope.requestMode) {
            case'Form':
                $scope.criteriaToSql($scope.table, $scope.metadata.columns, $scope.criterias);
                break;
        }

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
    $scope.criteriaToSql = function(table, columns, criterias){
        var isFirst = true, sql = 'SELECT * FROM ' + table;
        if(_.size(criterias) > 0){
            _.forEach(columns, function(column){
                var criteria = criterias[column.COLUMN_NAME];
                if(criteria){
                    sql += isFirst ? ' WHERE ' : ' AND ';
                    sql += criteria ? ' ' + column.COLUMN_NAME + '="' + criteria + '"' : '';
                    isFirst = false;
                }
            });

        }
        sql += ';';
        $scope.request = sql;
        $log.info(sql)
    };


    $scope.tables = [];
    $scope.getTables = function(){
        $http.post('tables', {con:$scope.con}).success(function(rows){
            $scope.tables = rows;
        });
    };

    $scope.onTableSelect = function($item, $model, $label){
        $scope.table = $item.TABLE_NAME;
        $scope.getMetadata($item.TABLE_NAME);
    };
    $scope.metadata = {};
    $scope.criterias = {};
    $scope.getMetadata = function(tableName){
        $http.post('metadata/' + tableName, {con:$scope.con}).success(function(metadata){
            $scope.metadata = metadata;
        });
    };



    //init
    $scope.getTables();

}