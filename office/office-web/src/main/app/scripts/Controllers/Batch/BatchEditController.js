import {Utils} from '../../Services/Utils.js';
import {Errors} from '../../Services/Errors.js';
import {getAuthorizationHeader} from '../../Token.js';

export class BatchEditController {
    /**
     * @ngInject
     * @param $scope
     * @param $stateParams
     * @param $compile
     * @param $modal
     * @param batch
     * @param {Modal} Modal
     * @param {DataTable} DataTable
     * @param {BatchService} BatchService
     * @param {Configuration} Configuration
     * @param {Notifications} Notifications
     */
    constructor($scope, $stateParams, $compile, $modal, batch, Modal, DataTable, BatchService, Configuration, Notifications) {
        const vm = this;

        vm.batch = batch;

        vm.allowDocumentTypeChange = Configuration.document.allowDocumentTypeChange;
        vm.isUnfinished = isUnfinished;
        vm.addDocuments = addDocuments;
        vm.publishBatch = publishBatch;
        vm.changePublishDate = changePublishDate;
        vm.changeType = changeType;
        vm.removeFromBatch = removeFromBatch;
        vm.moveToBatch = moveToBatch;
        vm.deleteDocument = deleteDocument;
        vm.changeImageProperties = changeImageProperties;

        prepareTable();

        function isUnfinished() {
            return vm.batch.state === 'unfinished';
        }

        function isDiscarded() {
            return vm.batch.state === 'discarded';
        }

        function createColumns() {
            let columns = [
                DataTable.createCheckbox()
            ];

            if (isUnfinished() === false) {
                columns[0].notVisible();
            }

            columns = columns.concat([
                DataTable.createActions(obj => {
                    const buttons = [
                        DataTable.Button.edit('.document', {
                            documentId: obj.id
                        })
                    ];

                    if (isUnfinished()) {
                        buttons.push(
                            '<button type="button" class="btn btn-default" document-preview document-id="' + obj.id + '" tooltip="náhled"><i class="fa fa-search"></i></button>',
                            DataTable.Button.delete('vm.deleteDocument', [Utils.quote(obj.id), '$event'])
                        );
                    } else {
                        if (obj.state !== 'unpublished') {
                            buttons.push(
                                '<button type="button" class="btn btn-danger" document-unpublish document="' + obj.id + '" batch="' + obj.batchId + '"><i class="fa fa-ban"></i> znepřístupnit</button>'
                            );
                        }
                    }

                    return buttons;
                }).withOption('width', isUnfinished() ? '130px' : '150px'),
                DataTable.createColumn('invId', 'Inv. číslo', true),
                DataTable.createColumn('title', 'Název', true).withOption('width', '550px'),
                DataTable.createDocumentTypeColumn('type', 'Druh dokumentu'),
                DataTable.createDateColumn('created', 'Vytvořeno').withOption('width', '135px'),
                DataTable.createDateColumn('modified', 'Poslední změna').withOption('width', '135px')
            ]);

            if (isDiscarded()) {
                columns[1].notVisible();
            }

            return columns;
        }

        function createOptions() {
            let options = DataTable.fromPromise($scope, () => BatchService.findDocuments($stateParams.id));

            options.order = [2, 'asc'];
            options.withOption('createdRow', createdRow);

            if (isUnfinished() === false) {
                options = DataTable.disableTableTools(options);
            }

            return options;

            function createdRow(row, data) {
                $compile(angular.element(row).contents())($scope);
                if (data.hasOwnProperty('validToPublish') && data.validToPublish === false) {
                    angular.element(row).addClass('danger');
                }

                if (data.state === 'unpublished') {
                    angular.element(row).css('background', '#f5f5f5');
                }
            }
        }

        function prepareTable() {
            vm.options = createOptions();
            vm.columns = createColumns();
        }

        function setBatchPublished() {
            vm.batch.state = 'published';
            prepareTable();
        }

        function setBatchUnpublished() {
            vm.batch.state = 'unfinished';
            prepareTable();
        }

        function addDocuments() {
            $modal.open({
                windowClass: 'modal-documents',
                templateUrl: 'views/forms/Batch/addDocuments.html',
                controller: controller
            });

            function controller($scope, $state, User, DocumentService) {
                const options = DataTable.createOptions($scope);
                options.ajax = {
                    url: '/dcap/office/document',
                    headers: getAuthorizationHeader(User.getToken()),
                    data: () => {
                        return {
                            state: 'original'
                        };
                    }
                };

                options.dom = '<"pull-left"Tf>t<"dt-panelfooter clearfix"p>';

                options.withOption('initComplete', () => {
                    const published = angular.element('.published');
                    const unpublished = angular.element('.unpublished');

                    published.append(
                        '<input type="checkbox" id="documents-published">'
                    );

                    unpublished.append(
                        '<input type="checkbox" id="documents-unpublished">'
                    )
                });

                options.withOption('createdRow', (row, data) => {
                    if (data.publishingState === 'published') {
                        angular.element(row).css('background', '#e8ffc4');
                    }

                    if (data.publishingState === 'unpublished') {
                        angular.element(row).css('background', '#f9ffc4');
                    }
                });

                options.withTableToolsOption('fnPreRowSelect', () => true);
                options.withTableToolsOption('fnRowSelected', nodes => {
                    angular.element(nodes).find('input:checkbox').prop('checked', true);
                });
                options.withTableToolsOption('fnRowDeselected', nodes => {
                    angular.element(nodes).find('input:checkbox').prop('checked', false);
                });

                $scope.options = options;
                $scope.columns = [
                    DataTable.createCheckbox(),
                    DataTable.createColumn('invId', 'IČ'),
                    DataTable.createColumn('title', 'Název'),
                    DataTable.createDocumentTypeColumn('type', 'Druh dokumentu').withOption('width', '150px'),
                    DataTable.createDocumentStateColumn('state', 'Stav').renderWith((value, stage, obj) => {
                        let result = value;
                        if (obj.publishingState) {
                            result = obj.publishingState;
                        }

                        return DocumentService.getState(result);
                    }),
                    DataTable.createDateColumn('created', 'Vytvořeno'),
                    DataTable.createDateColumn('modified', 'Poslední změna')
                ];

                $scope.submit = () => {
                    $scope.loading = true;
                    const selected = DataTable.getSelectedRows($scope.getTable('documents'));
                    if (selected.length === 0) {
                        Notifications.addError('Musíte vybrat nějaké dokumenty.');
                        $scope.loading = false;
                        return;
                    }

                    batch.all('document').post(_.pluck(selected, 'id')).then(() => {
                        Notifications.addSuccess('Dokumenty byly přidány do dávky.');
                        $scope.$dismiss();
                        $state.reload();
                    }).catch(response => {
                        const message = Errors.getMessage(response.data.errorCode);
                        Notifications.addError(message({
                            batch: response.data.parameters[0]
                        }));

                        $scope.loading = false;
                    });
                };
            }
        }

        function publishBatch() {
            if (_.some($scope.getTableData(), {
                    validToPublish: false
                })) {
                Notifications.addError('Nepodařilo se zpřístupnit dávku, některé dokumenty nemají vyplněny povinné pole.');
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Batch/publishBatch.html',
                controller: ($scope) => {
                    $scope.submit = () => {
                        $scope.loading = true;

                        batch.one('publish').post().catch(() => {
                            setBatchUnpublished();
                            Notifications.addError('Objevila se chyba při publikování dávky.');
                        });

                        setBatchPublished();

                        Notifications.addSuccess('Dávka byla zpřístupněna.');
                        $scope.$dismiss();
                    };
                }
            });
        }

        function moveToBatch() {
            const selected = DataTable.getSelectedRows($scope.getTable());
            if (selected.length === 0) {
                Notifications.addError('Nejsou vybrány žádné dokumenty.');
                return;
            }

            if (_.some(selected, {
                    type: 'issue'
                })) {
                Notifications.addError('Vydání nelze přesunout do jiné dávky.');
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Batch/moveToBatch.html',
                controller: ($scope) => {
                    $scope.batch = {};
                    $scope.mode = 'existing';

                    BatchService.findAll('unfinished').then(batches => {
                        // unset current batch from selection as it would be stupid to move to it
                        _.remove(batches, {
                            id: batch.id
                        });
                        $scope.batches = batches;
                    });

                    $scope.submit = submit;

                    function submit() {
                        if ($scope.form.$invalid) {
                            return;
                        }

                        $scope.loading = true;

                        function success() {
                            Notifications.addSuccess('Dokumenty byly přesunuty.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        }

                        function moveDocuments(id) {
                            batch.one('document/move', id).customPOST(_.pluck(selected, 'id')).then(success);
                        }

                        if ($scope.mode === 'new') {
                            BatchService.create($scope.batch.name).then(id => {
                                moveDocuments(id);
                            });
                        } else {
                            moveDocuments($scope.batch.id);
                        }
                    }
                }
            });
        }

        function deleteDocument(id, $event) {
            $event.stopPropagation();

            $modal.open({
                templateUrl: 'views/services/Modal/delete.html',
                controller: controller,
                controllerAs: 'vm'
            });

            /**
             * @ngInject
             * @param $scope
             */
            function controller($scope) {
                const vm = this;
                const data = DataTable.getData();
                const value = _.find(data, {
                    id: id
                });

                vm.subject = 'dokument';

                vm.submit = submit;
                vm.getValue = getValue;

                function getValue() {
                    return value.title;
                }

                function submit() {
                    vm.loading = true;
                    batch.all('document/delete').post([id]).then(() => {
                        Notifications.addSuccess('Dokument byl odstraněn z dávky.');
                        DataTable.reload();
                        $scope.$dismiss();
                    });
                }
            }
        }

        function removeFromBatch() {
            const table = DataTable.getInstance();
            const selected = DataTable.getSelectedRows(table);
            if (selected.length === 0) {
                Notifications.addError('Nejsou vybrány žádné dokumenty.');
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Batch/removeFromBatch.html',
                controller: ($scope) => {
                    $scope.submit = function() {
                        $scope.loading = true;
                        batch.all('document/delete').post(_.pluck(selected, 'id')).then(function() {
                            Notifications.addSuccess('Dokumenty byly odstraněny z dávky.');
                            $scope.reloadTable();
                            $scope.$dismiss();
                        });
                    };
                }
            });
        }

        function changeType() {
            const selected = DataTable.getSelectedRows($scope.getTable());
            if (selected.length === 0) {
                Notifications.addError('Nejsou vybrány žádné dokumenty.');
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Batch/changeType.html',
                controller: ($scope, DocumentService, Restangular) => {
                    $scope.document = {};
                    $scope.submit = submit;

                    function submit() {
                        if ($scope.form.$valid) {
                            $scope.loading = true;

                            let i, row, data = [],
                                temp;
                            for (i in selected) {
                                if (selected.hasOwnProperty(i)) {
                                    row = selected[i];

                                    temp = {
                                        docId: [row.id],
                                        documentType: $scope.document.documentType,
                                        documentSubType: $scope.document.documentSubType
                                    };

                                    data.push(Restangular.copy(temp));
                                }
                            }

                            DocumentService.save(data).then(() => {
                                Notifications.addSuccess('Typy dokumentů byly změněny.');
                                $scope.reloadTable();
                                $scope.$dismiss();
                            });
                        }
                    }
                }
            });
        }

        function changeImageProperties() {
            const selected = DataTable.getSelectedRows(DataTable.getInstance());
            if (selected.length === 0) {
                Notifications.addError('Nejsou vybrány žádné dokumenty.');
            } else {
                $modal.open({
                    templateUrl: 'views/forms/Batch/changeImageProperties.html',
                    controller: controller,
                    controllerAs: 'vm'
                });
            }

            /**
             *
             * @param $scope
             * @param {DocumentService} DocumentService
             * @param Restangular
             */
            function controller($scope, DocumentService, Restangular) {
                let vm = this;

                vm.values = {
                    allowContentPublicly: false,
                    allowPdfExport: true,
                    allowEpubExport: true,
                    watermark: true,
                    watermarkPosition: 'cc'
                };

                vm.watermarkPositions = DocumentService.WatermarkPositions;

                vm.submit = submit;

                function submit() {
                    if (vm.form.$valid) {
                        vm.loading = true;
                        const documents = _.map(selected, map);
                        DocumentService.save(documents).then(success);
                    }

                    function success() {
                        Notifications.addSuccess('Obrazová nastavení pro vybrané dokumenty byla změněna.');
                        DataTable.reload();
                        $scope.$close();
                    }

                    function map(doc) {
                        return Restangular.copy(_.merge(vm.values, {
                            docId: [doc.id]
                        }));
                    }
                }
            }
        }

        function changePublishDate() {
            const selected = DataTable.getSelectedRows($scope.getTable());
            if (selected.length === 0) {
                Notifications.addError('Nejsou vybrány žádné dokumenty.');
                return;
            }

            $modal.open({
                templateUrl: 'views/forms/Batch/changePublishDate.html',
                controller: ($scope, DocumentService, Restangular) => {
                    $scope.document = {};

                    $scope.isOpen = [false, false];
                    $scope.open = function(index, $event) {
                        $event.stopPropagation();

                        $scope.isOpen[index] = true;
                    };

                    $scope.submit = () => {
                        if ($scope.form.$valid) {
                            $scope.loading = true;

                            let i, row, data = [],
                                temp;
                            for (i in selected) {
                                if (selected.hasOwnProperty(i)) {
                                    row = selected[i];

                                    temp = {
                                        docId: [row.id],
                                        publishFrom: $scope.document.publishFrom,
                                        publishTo: $scope.document.publishTo
                                    };

                                    data.push(Restangular.copy(temp));
                                }
                            }

                            DocumentService.save(data).then(() => {
                                Notifications.addSuccess('Data zveřejnění byly změneny.');
                                $scope.reloadTable();
                                $scope.$dismiss();
                            });
                        }
                    };
                }
            });
        }
    }
}
