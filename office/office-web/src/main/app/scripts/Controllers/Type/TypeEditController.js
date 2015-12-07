'use strict';
export class TypeEditController {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param $q
     * @param type
     * @param {DataTable} DataTable
     * @param {Notifications} Notifications
     */
    constructor($scope, $modal, $q, type, DataTable, Notifications) {
        $scope.type = type;

        let options = DataTable.fromPromise($scope, () => {
            let defer = $q.defer();
            defer.resolve(type.subTypes);

            return defer.promise;
        });

        $scope.options = DataTable.disableTableTools(options);

        $scope.columns = [
            DataTable.createActions(obj => [
                DataTable.Button.edit('update', obj.id),
                DataTable.Button.delete('remove', obj.id)
            ]),
            DataTable.createColumn('name', 'Název')
        ];

        $scope.submit = () => {
            if ($scope.form.$valid) {
                $scope.loading = true;
                $scope.type.save({name: $scope.type.name}).then(() => {
                    Notifications.changesSaved();
                    $scope.loading = false;
                });
            }
        };

        $scope.create = () => {
            $modal.open({
                templateUrl: 'views/forms/Type/subtype.html',
                controller: ($scope) => {
                    $scope.submit = () => {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        type.all('subtype').post('', {name: $scope.subtype.name}).then(id => {
                            type.subTypes.push({id: id, name: $scope.subtype.name});

                            Notifications.addSuccess('Podtyp byl vytvořen.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        });
                    };
                }
            });
        };

        $scope.update = (id) => {
            $modal.open({
                templateUrl: 'views/forms/Type/subtype.html',
                controller: ($scope) => {
                    $scope.subtype = _.clone(_.find(type.subTypes, {id: id}));

                    $scope.submit = function () {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;
                        type.one('subtype', id).put({name: $scope.subtype.name}).then(() => {
                            var subtype = _.find(type.subTypes, {id: id});
                            subtype.name = $scope.subtype.name;

                            Notifications.changesSaved();
                            $scope.reloadTable();
                            $scope.$dismiss();
                        });
                    };
                }
            });
        };

        $scope.remove = (id) => {
            $modal.open({
                templateUrl: 'views/forms/Type/deleteSubtype.html',
                controller: ($scope) => {
                    $scope.subtype = _.find(type.subTypes, {id: id});

                    $scope.submit = () => {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;
                        type.one('subtype', id).remove().then(() => {
                            _.remove(type.subTypes, $scope.subtype);

                            Notifications.addSuccess('Podtyp byl odstraněn.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }).catch(() => {
                            Notifications.addError('Nepodařilo se odstranit podtyp. Pravděpodobně ještě existují dokumenty s tímto typem.');
                        }).finally(() => {
                            $scope.loading = false;
                        });
                    };
                }
            });
        };
    }
}