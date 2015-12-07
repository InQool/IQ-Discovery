export class NotificationController {
    /**
     * @ngInject
     * @param $scope
     * @param {DataTable} DataTable
     * @param {MailNotificationService} MailNotificationService
     */
    constructor($scope, DataTable, MailNotificationService) {
        let options = DataTable.fromPromise($scope, () => MailNotificationService.findAll());

        $scope.options = DataTable.disableTableTools(options);
        $scope.columns = [
            DataTable.createActions(obj => [
                DataTable.Button.edit('.edit', {id: obj.id})
            ]).withOption('width', '35px'),
            DataTable.createColumn('subject', 'Předmět')
        ];
    }
}