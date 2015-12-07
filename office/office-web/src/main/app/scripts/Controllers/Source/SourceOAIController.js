export class SourceOAIController {
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
        const options = DataTable.fromPromise($scope, () => SourceService.findAllOAI());

        $scope.options = DataTable.disableTableTools(options);

        $scope.columns = [
            DataTable.createActions(obj => [
                DataTable.Button.edit('update', obj.id),
                DataTable.Button.delete('remove', obj.id)
            ]),
            DataTable.createColumn('name', 'Název'),
            DataTable.createColumn('shortcut', 'Zkratka'),
            DataTable.createColumn('set', 'Set'),
            DataTable.createColumn('url', 'URL')
        ];

        $scope.create = () => {
            $modal.open({
                templateUrl: 'views/forms/Source/OAI/form.html',
                controller: ($scope) => {
                    $scope.submit = () => {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;
                        SourceService.createOAI($scope.source).then(() => {
                            Notifications.addSuccess('Zdroj byl vytvořen.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }).catch(response => {
                            if (response.data === -1) {
                                Notifications.addError('Tento zdroj již existuje.');
                            }

                            if (response.data === -2) {
                                Notifications.addError('Zdroj je nefunkční.');
                            }
                        }).finally(() => {
                            $scope.loading = false;
                        });
                    };
                }
            });
        };

        $scope.update = function (id) {
            $modal.open({
                templateUrl: 'views/forms/Source/OAI/form.html',
                controller: ($scope) => {
                    var data = $scope.getTableData();
                    $scope.source = _.find(data, {id: id});

                    $scope.submit = () => {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;
                        $scope.source.save().then(() => {
                            Notifications.changesSaved();
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }).catch(response => {
                            if (response.data === -1) {
                                Notifications.addError('Tento zdroj již existuje.');
                            }

                            if (response.data === -2) {
                                Notifications.addError('Zdroj je nefunkční.');
                            }
                        }).finally(() => {
                            $scope.loading = false;
                        });
                    };
                }
            });
        };

        $scope.remove = (id) => {
            Modal.deleteWindow({
                id: id,
                subject: 'zdroj',
                success: () => {
                    Notifications.addSuccess('Zdroj byl odstraněn.');
                }
            });
        };
    }
}
