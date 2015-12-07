'use strict';
export class SourceZ3950Controller {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param SourceService
     * @param DataTable
     * @param Modal
     * @param Notifications
     */
    constructor($scope, $modal, SourceService, DataTable, Modal, Notifications) {
        let options = DataTable.fromPromise($scope, () => SourceService.findAllZ3950());
        $scope.options = DataTable.disableTableTools(options);

        $scope.columns = [
            DataTable.createActions(obj => {
                return [
                    DataTable.Button.edit('update', obj.id),
                    DataTable.Button.delete('remove', obj.id)
                ];
            }),
            DataTable.createColumn('name', 'Název'),
            DataTable.createColumn('url', 'URL'),
            DataTable.createColumn('databaseName', 'Databáze')
        ];

        $scope.create = function () {
            $modal.open({
                templateUrl: 'views/forms/Source/Z3950/form.html',
                controller: function ($scope) {
                    $scope.submit = function () {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;
                        SourceService.createZ3950($scope.source).then(function () {
                            Notifications.addSuccess('Zdroj byl vytvořen.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }).catch(function (response) {
                            if (response.data === -1) {
                                Notifications.addError('Tento zdroj již existuje.');
                            }

                            if (response.data === -2) {
                                Notifications.addError('Zdroj je nefunkční.');
                            }
                        }).finally(function () {
                            $scope.loading = false;
                        });
                    };
                }
            });
        };

        $scope.update = function (id) {
            $modal.open({
                templateUrl: 'views/forms/Source/Z3950/form.html',
                controller: function ($scope) {
                    var data = $scope.getTableData();
                    $scope.source = _.find(data, {id: id});

                    $scope.submit = function () {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;
                        $scope.source.save().then(function () {
                            Notifications.changesSaved();
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }).catch(function (response) {
                            if (response.data === -1) {
                                Notifications.addError('Tento zdroj již existuje.');
                            }

                            if (response.data === -2) {
                                Notifications.addError('Zdroj je nefunkční.');
                            }
                        }).finally(function () {
                            $scope.loading = false;
                        });
                    };
                }
            });
        };

        $scope.remove = function (id) {
            Modal.deleteWindow({
                id: id,
                subject: 'zdroj',
                success: function () {
                    Notifications.addSuccess('Zdroj byl odstraněn.');
                }
            });
        };
    }
}