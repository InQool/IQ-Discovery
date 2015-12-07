import {Errors} from '../../Services/Errors.js';

export class BatchController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param $modal
	 * @param $stateParams
	 * @param {Modal} Modal
	 * @param {BatchService} BatchService
	 * @param {DataTable} DataTable
	 * @param {Notifications} Notifications
	 */
	constructor($scope, $modal, $stateParams, Modal, BatchService, DataTable, Notifications) {
		$scope.state = $stateParams.state || 'unfinished';

		function isUnfinished() {
			return $scope.state === 'unfinished';
		}

		$scope.isUnfinished = isUnfinished;

		let columns, options;

		columns = [
			DataTable.createCheckbox(),
			DataTable.createActions(obj => {
				let buttons = [
					DataTable.Button.edit('.edit', {id: obj.id})
				];

				if (isUnfinished()) {
					buttons.push(
						DataTable.Button.delete('removeBatch', [obj.id, '$event'])
					);
				}

				return buttons;
			}),
			DataTable.createColumn('name', 'Název', true),
			DataTable.createColumn('numDocs', 'Počet dokumentů').withOption('width', '150px'),
			DataTable.createDateColumn('created', 'Vytvořeno').withOption('width', '300px'),
			DataTable.createDateColumn('modified', 'Poslední změna').withOption('width', '300px')
		];

		options = DataTable.fromPromise($scope, () => BatchService.findAll($scope.state)).withOption('order', [isUnfinished() ? 2 : 1, 'asc']);

		if (!isUnfinished()) {
			columns.splice(0, 1);
			columns[0].width = '30px';
			options = DataTable.disableTableTools(options);
		}

		$scope.columns = columns;
		$scope.options = options;

		$scope.create = () => {
			$modal.open({
				templateUrl: 'views/forms/Batch/create.html',
				controller: ($scope, BatchService, Notifications) => {
					$scope.submit = () => {
						if ($scope.form.$invalid) {
							return;
						}

						$scope.loading = true;
						BatchService.create($scope.name).then(() => {
							Notifications.addSuccess('Dávka byla vytvořena.');
							$scope.reloadTable();
							$scope.$dismiss();
						});
					};
				}
			});
		};

		$scope.changeBatchesOwner = () => {
			let selected = DataTable.getSelectedRows($scope.getTable());
			if (selected.length === 0) {
				Notifications.addError('Musíte vybrat nějaké dávky.');
				return;
			}

			$modal.open({
				templateUrl: 'views/forms/Batch/changeOwnership.html',
				controller: ($scope, PeopleService, User) => {
					$scope.values = {
						owner: null
					};

					$scope.submit = submit;

					findCurators();

					function submit() {
						if ($scope.form.$valid) {
							$scope.loading = true;
							BatchService.handOver(User.getUsername(), $scope.values.owner, _.pluck(selected, 'id'))
								.then(success)
								.catch(error);
						}

						function success() {
							Notifications.addSuccess('Vlastník úspěšně změněn pro vybrané dávky.');
							$scope.reloadTable();
							$scope.$dismiss();
						}

						function error(response) {
							let message = Errors.getMessage(response.data.errorCode);
							Notifications.addError(message);

							$scope.loading = false;
						}
					}

					function findCurators() {
						PeopleService.findCurators().then(users => {
							$scope.users = users;
						});
					}
				}
			});
		};

		$scope.removeBatch = (id, $event) => {
			$event.stopPropagation();

			Modal.deleteWindow({
				id: id,
				subject: 'dávku',
				success: () => {
					Notifications.addSuccess('Dávka byla odstraněna.');
				}
			});
		};

		$scope.removeBatches = () => {
			let selected = DataTable.getSelectedRows($scope.getTable());
			if (selected.length === 0) {
				Notifications.addError('Musíte vybrat nějaké dávky.');
				return;
			}

			$modal.open({
				templateUrl: 'views/forms/Batch/removeBatches.html',
				controller: ($scope) => {
					$scope.submit = () => {
						$scope.loading = true;
						BatchService.remove(_.pluck(selected, 'id')).then(() => {
							Notifications.addSuccess('Vybrané dávky byly odstraněny.');
							$scope.reloadTable();
							$scope.$dismiss();
						});
					};
				}
			});
		};
	}
}