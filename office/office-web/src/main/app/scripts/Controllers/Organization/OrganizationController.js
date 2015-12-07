export class OrganizationController {
    /**
     * @ngInject
     * @param $scope
     * @param {OrganizationService} OrganizationService
     * @param {DataTable} DataTable
     * @param {Notifications} Notifications
     */
    constructor($scope, OrganizationService, DataTable, Notifications) {
        const options = DataTable.fromPromise($scope, () => OrganizationService.findAll());
        $scope.options = DataTable.disableTableTools(options);

        $scope.columns = [
            DataTable.createActions(obj => [
                DataTable.Button.edit('.edit', {
                    id: obj.id
                })
            ]).withOption('width', '35px'),
            DataTable.createColumn('name', 'Název', true),
            DataTable.createColumn('id', 'Označení'),
            DataTable.createColumn('userCount', 'Počet uživatelů')
        ];

        $scope.resync = resync;

        function resync() {
            $scope.loading = true;
            OrganizationService.resync().then(function() {
                Notifications.addSuccess('Synchronizace dokončena.');
                $scope.reloadTable();
            }, function() {
                Notifications.addError('Synchronizace selhala.');
            }).finally(function() {
                $scope.loading = false;
            });
        }
    }
}
