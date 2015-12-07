import {Utils} from '../../Services/Utils.js';

export class DocumentController {
    /**
     * @ngInject
     * @param $scope
     * @param $stateParams
     * @param {DocumentService} DocumentService
     * @param {Modal} Modal
     * @param {DataTable} DataTable
     * @param {Notifications} Notifications
     */
    constructor($scope, $stateParams, DocumentService, $modal, DataTable, Notifications) {
        $scope.state = $stateParams.state || 'concept';

        $scope.options = createOptions();

        function isConcept() {
            return $scope.state === 'concept';
        }

        function createDocumentStateParams(batchId, documentId) {
            return {
                id: batchId,
                documentId: documentId
            };
        }

        function createOptions() {
            let options = DataTable.fromPromise($scope, () => DocumentService.findAll($scope.state));
            options = DataTable.disableTableTools(options);

            return options;
        }

        $scope.columns = [
            DataTable.createActions(obj => {
                const documentStatePath = 'secured.batch.edit.document';
                let buttons;

                if (isConcept()) {
                    buttons = [
                        DataTable.Button.edit(documentStatePath, createDocumentStateParams(obj.batchId, obj.id)),
                        '<button type="button" class="btn btn-default" document-preview document-id="' + obj.id + '" tooltip="náhled" tooltip-append-to-body="true"><i class="fa fa-search"></i></button>',
                        DataTable.Button.delete('remove', [Utils.quote(obj.id), '$event'])
                    ];
                } else {
                    buttons = [
                        DataTable.Button.create(documentStatePath, createDocumentStateParams(obj.batchId, obj.id), 'btn btn-default', '', 'fa-search', 'detail'),
                        '<button type="button" class="btn btn-danger" document-unpublish document="' + obj.id + '" batch="' + obj.batchId + '"><i class="fa fa-ban"></i> znepřístupnit</button>'
                    ];
                }

                return buttons;
            }).withOption('width', isConcept() ? '110px' : '150px'),
            DataTable.createColumn('invId', 'Inv. číslo', true),
            DataTable.createColumn('title', 'Název', true).withOption('width', '500px'),
            DataTable.createColumn('batchName', 'Dávka'),
            DataTable.createDocumentTypeColumn('type', 'Druh dokumentu'),
            DataTable.createDateColumn('created', 'Vytvořeno'),
            DataTable.createDateColumn('modified', 'Poslední změna')
        ];

        $scope.remove = (id, $event) => {
            $event.stopPropagation();

            $modal.open({
                templateUrl: 'views/services/Modal/delete.html',
                controller: controller,
                controllerAs: 'vm'
            });

            function controller($scope) {
                const vm = this;
                const doc = _.find(DataTable.getData(), { id: id });

                vm.subject = 'dokument';
                vm.submit = submit;
                vm.getValue = getValue;

                function getValue() {
                    return doc.title;
                }

                function submit() {
                    vm.loading = true;
                    DocumentService.remove(doc.batchId, id).then(() => {
                        Notifications.addSuccess('Dokument byl odstraněn.');
                        DataTable.reload();
                        $scope.$dismiss();
                    });
                }
            }
        };
    }
}
