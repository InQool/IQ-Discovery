import {getAuthorizationHeader} from '../../Token.js';

export class GalleryCreateController {
    /**
     * @ngInject
     * @param $scope
     * @param $modal
     * @param {GalleryService} GalleryService
     * @param {DataTable} DataTable
     * @param {User} User
     * @param {Notifications} Notifications
     */
    constructor($scope, $modal, GalleryService, DataTable, User, Notifications) {
        const vm = this;

        vm.gallery = {
            active: true,
            documents: []
        };

        vm.image = null;
        vm.submit = submit;
        vm.openAddDocumentsModal = openAddDocumentsModal;
        vm.removeDocument = removeDocument;

        function submit() {
            if (vm.form.$valid) {
                if (vm.gallery.documents.length === 0) {
                    Notifications.addError('Musíte vybrat nějaké dokumenty.');
                    return;
                }

                vm.loading = true;

                GalleryService.create(vm.gallery).then(gallery => {
                    if (vm.image) {
                        GalleryService.uploadImage(gallery, vm.image).then(success);
                    } else {
                        success();
                    }

                    function success() {
                        Notifications.addSuccess('Virtuální výstava byla vytvořena.');
                        DataTable.reload();
                        $scope.$close();
                    }
                });
            }
        }

        function getDocuments() {
            return vm.gallery.documents;
        }

        function addDocuments(documents) {
            angular.forEach(documents, doc => {
                delete doc.type;
                delete doc.orgName;
                vm.gallery.documents.push(doc);
            });
        }

        function removeDocument(doc) {
            _.remove(vm.gallery.documents, doc);
        }

        function openAddDocumentsModal() {
            $modal.open({
                windowClass: 'modal-documents',
                templateUrl: 'views/controllers/Gallery/addDocuments.html',
                controller: controller,
                controllerAs: 'vm'
            });

            function controller($scope) {
                const vm = this;

                vm.documents = null;
                vm.options = createOptions();
                vm.columns = [
                    DataTable.createColumn('invId', 'Inventární číslo'),
                    DataTable.createColumn('title', 'Název'),
                    DataTable.createDocumentTypeColumn('type', 'Typ dokumentu'),
                    DataTable.createColumn('orgName', 'Organizace')
                ];

                vm.submit = submit;

                function submit() {
                    const table = DataTable.getInstance('documents');
                    const selected = DataTable.getSelectedRows(table);
                    addDocuments(selected);
                    $scope.$close();
                }

                function createOptions() {
                    const options = _.merge(DataTable.createOptions(vm), {
                        ajax: {
                            url: '/dcap/office/document/forExpo',
                            headers: getAuthorizationHeader(User.getToken()),
                            data: function (data) {
                                return {
                                    limit: 100,
                                    inventoryId: data.search.value
                                };
                            },
                            dataSrc: data => {
                                return _.filter(data, filter);

                                function filter(obj) {
                                    return _.some(getDocuments(), {invId: obj.invId}) === false;
                                }
                            }
                        },
                        dom: '<"pull-left"Tf>t',
                        info: false,
                        ordering: false,
                        serverSide: true,
                        paging: false,
                        pageLength: null
                    });

                    options.withTableToolsOption('fnPreRowSelect', () => true);

                    options.withTableToolsOption('fnRowSelected', nodes => {
                        angular.element(nodes).find('input:checkbox').prop('checked', true);
                    });
                    options.withTableToolsOption('fnRowDeselected', nodes => {
                        angular.element(nodes).find('input:checkbox').prop('checked', false);
                    });

                    return options;
                }
            }
        }
    }
}
