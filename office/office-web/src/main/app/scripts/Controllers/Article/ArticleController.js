export class ArticleController {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param {ArticleService} ArticleService
     * @param {DataTable} DataTable
     * @param {Modal} Modal
     * @param {Notifications} Notifications
     */
    constructor($scope, $modal, ArticleService, DataTable, Modal, Notifications) {
        let vm = this;

        vm.options = DataTable.fromPromise($scope, () => ArticleService.findAll());
        vm.options.order = [4, 'desc'];
        vm.columns = createColumns();

        vm.activate = activate;
        vm.deactivate = deactivate;
        vm.remove = remove;
        vm.massRemove = massRemove;

        function createColumns() {
            return [
                DataTable.createCheckbox(),
                DataTable.createActions(obj => [
                    DataTable.Button.edit('secured.article.edit', {id: obj.id}),
                    DataTable.Button.delete('vm.remove', obj.id),
                    obj.active
                        ? DataTable.Button.create('vm.deactivate', obj.id, 'btn btn-danger', '', 'fa-fw fa-ban', 'deaktivovat')
                        : DataTable.Button.create('vm.activate', obj.id, 'btn btn-success', '', 'fa-fw fa-check', 'aktivovat')
                ]).withOption('width', '115px'),
                DataTable.createColumn('title', 'Název', true),
                DataTable.createColumn('active', 'Aktivní').renderWith(cell => cell ? '<i class="fa fa-check"></i>' : '<i class="fa fa-remove"></i>').withOption('width', '65px'),
                DataTable.createDateColumn('created', 'Vytvořeno'),
                DataTable.createDateColumn('publishedFrom', 'Zveřejněno od'),
                DataTable.createDateColumn('publishedTo', 'Zveřejněno do')
            ];
        }

        function reloadTable() {
            DataTable.reload();
        }

        function activate(id) {
            let article = _.find(DataTable.getData(), {id: id});
            article.active = true;
            article.save().then(reloadTable);
        }

        function deactivate(id) {
            let article = _.find(DataTable.getData(), {id: id});
            article.active = false;
            article.save().then(reloadTable);
        }

        function remove(id) {
            Modal.deleteWindow({
                id: id,
                subject: 'článek',
                value: 'title',
                success: success
            });

            function success() {
                Notifications.addSuccess('Článek byl odstraněn.');
            }
        }

        function massRemove() {
            let table = DataTable.getInstance(), selected = DataTable.getSelectedRows(table);
            if (selected.length === 0) {
                Notifications.addError('Nejsou vybrány žádné články.');
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Article/remove.html',
                controller: controller,
                controllerAs: 'vm'
            });

            function controller($scope) {
                let vm = this;

                vm.submit = submit;

                function submit() {
                    vm.loading = true;
                    ArticleService.remove(_.pluck(selected, 'id')).then(() => {
                        Notifications.addSuccess('Vybrané články byly odstraněny.');
                        reloadTable();
                        $scope.$dismiss();
                    });
                }
            }
        }
    }
}
