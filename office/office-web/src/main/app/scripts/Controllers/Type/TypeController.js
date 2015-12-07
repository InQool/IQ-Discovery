'use strict';
export class TypeController {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param $compile
     * @param TypeService
     * @param DataTable
     * @param Notifications
     */
    constructor($scope, $modal, $compile, TypeService, DataTable, Notifications) {
        let options = DataTable.fromPromise($scope, () => TypeService.findAll());

        options.withOption('createdRow', (row, data) => {
            $compile(angular.element(row).contents())($scope);
            if (data.subtypeCount === 0) {
                $(row).addClass('danger');
            }
        });

        $scope.options = DataTable.disableTableTools(options);
        $scope.columns = [
            DataTable.createActions(obj => [
                DataTable.Button.edit('.edit', {id: obj.id}),
                DataTable.Button.delete('remove', obj.id)
            ]),
            DataTable.createColumn('name', 'Název'),
            DataTable.createColumn('subtypeCount', 'Počet podtypů')
        ];

        $scope.create = () => {
            $modal.open({
                templateUrl: 'views/forms/Type/form.html',
                controller: ($scope) => {
                    $scope.type = {};
                    $scope.submit = () => {
                        if ($scope.form.$valid) {
                            $scope.loading = true;
                            TypeService.create($scope.type.name).then(() => {
                                Notifications.addSuccess('Typ byl vytvořen.');
                                $scope.reloadTable();
                                $scope.$dismiss();
                            });
                        }
                    };
                }
            });
        };

        $scope.remove = (id) => {
            $modal.open({
                templateUrl: 'views/forms/Type/delete.html',
                controller: ($scope) => {
                    var data = $scope.getTableData();
                    $scope.type = _.find(data, {id: id});

                    $scope.submit = () => {
                        $scope.loading = true;
                        TypeService.remove(id).then(() => {
                            Notifications.addSuccess('Typ byl odstraněn.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }).catch(() => {
                            Notifications.addError('Nepodařilo se odstranit typ. Pravděpodobně ještě existují dokumenty s tímto typem.');
                            $scope.loading = false;
                        });
                    };
                }
            });
        };
    }
}