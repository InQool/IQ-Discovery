export class AccountSettingsFavoritedController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param $window
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($scope, $window, User, Notifications) {
		User.getFavoriteDocuments().then(documents => {
			$scope.documents = documents;
		});

		$scope.remove = (doc) => {
			if ($window.confirm(__('Opravdu chcete odebrat dokument z oblíbených?'))) {
				doc.remove();
				_.remove($scope.documents, doc);
				Notifications.addSuccess(__('Dokument byl odebrán.'));
			}
		};
	}
}