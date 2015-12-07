import {Utils} from '../../Services/Utils.js';

export class OrganizationEditController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param $modal
	 * @param organization
	 * @param {User} User
	 * @param {OrganizationService} OrganizationService
	 * @param {DataTable} DataTable
	 * @param {Notifications} Notifications
	 */
	constructor($scope, $modal, organization, User, OrganizationService, DataTable, Notifications) {
		let options = DataTable.fromPromise($scope, () => organization.getList('user'));

		$scope.organization = organization;
		$scope.options = DataTable.disableTableTools(options);

		$scope.hideBackButton = User.hasRole('org_admin') && !User.hasRole('sys_admin');

		$scope.columns = [
			DataTable.createActions(user => [
				_.indexOf(user.roles, 'curator') === -1 ? null : DataTable.Button.create('handBatches', Utils.quote(user.idmId), 'btn btn-default', 'předat dokumenty', 'fa-fw fa-file')
			]),
			DataTable.createColumn('firstName', 'Jméno', true),
			DataTable.createColumn('lastName', 'Příjmení', true),
			DataTable.createColumn('roles', 'Role').renderWith(roles => roles.map(role => OrganizationService.getRole(role)).join(', ')),
			DataTable.createColumn('mail', 'Emailová adresa')
		];

		$scope.resync = () => {
			$scope.loading = true;
			organization.post('resync').then(() => {
				Notifications.addSuccess('Synchronizace dokončena.');
				$scope.reloadTable();
			}, () => {
				Notifications.addError('Synchronizace selhala.');
			}).finally(() => {
				$scope.loading = false;
			});
		};

		$scope.handBatches = (user) => {
			$modal.open({
				templateUrl: 'views/forms/Organization/handBatches.html',
				controller: ($scope, BatchService, PeopleService) => {
					$scope.selected = null;
					$scope.batches = [];
					$scope.value = {};

					BatchService.findUser(user, 'active').then(batches => {
						$scope.batches = batches;
					});

					PeopleService.findCurators(user).then(curators => {
						$scope.curators = curators;
					});

					$scope.submit = () => {
						if ($scope.form.$valid) {
							$scope.loading = true;

							if ($scope.selected === 'some') {
								BatchService.handOver(user, $scope.value.curator, $scope.value.batches).then(() => {
									Notifications.addSuccess('Vybrané dávky byly předány.');
									$scope.$dismiss();
								});
							}

							if ($scope.selected === 'all') {
								BatchService.handOverAll(user, $scope.value.curator).then(() => {
									Notifications.addSuccess('Všechny dávky byly předány.');
									$scope.$dismiss();
								});
							}
						}
					};
				}
			});
		};
	}
}