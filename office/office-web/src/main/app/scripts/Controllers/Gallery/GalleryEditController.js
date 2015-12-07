import {getAuthorizationHeader} from '../../Token.js';

export class GalleryEditController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param $modal
	 * @param {Object} gallery
	 * @param {GalleryService} GalleryService
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($scope, $modal, gallery, GalleryService, User, Notifications) {
		let vm = this;

		vm.image = null;
		vm.gallery = gallery;
		vm.token = User.getToken();

		vm.submit = submit;
		vm.openAddDocumentsModal = openAddDocumentsModal;
		vm.removeDocument = removeDocument;

		function submit() {
			if (vm.form.$valid) {
				vm.loading = true;

				vm.gallery.save().then(() => {
					if (vm.image) {
						GalleryService.uploadImage(gallery, vm.image).then(success);
					} else {
						success();
					}
				});
			}

			function success() {
				Notifications.changesSaved();
				$scope.reloadTable();
				$scope.$dismiss();
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

		function removeDocument(d) {
			_.remove(vm.gallery.documents, d);
		}

		function openAddDocumentsModal() {
			$modal.open({
				windowClass: 'modal-documents',
				templateUrl: 'views/controllers/Gallery/addDocuments.html',
				controller: controller,
				controllerAs: 'vm'
			});

			function controller($scope, DataTable) {
				let vm = this;

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
					let table = DataTable.getInstance('documents'), selected = DataTable.getSelectedRows(table);
					addDocuments(selected);
					$scope.$close();
				}

				function createOptions() {
					let options = _.merge(DataTable.createOptions(vm), {
						ajax: {
							url: '/dcap/office/document/forExpo',
							headers: getAuthorizationHeader(User.getToken()),
							data: function (data) {
								return {
									limit: 100,
									inventoryId: data.search.value
								};
							},
							dataSrc: function (data) {
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

					options.withTableToolsOption('fnPreRowSelect', function () {
						return true;
					});

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