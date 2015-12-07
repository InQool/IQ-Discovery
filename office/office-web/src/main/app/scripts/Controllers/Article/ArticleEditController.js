export class ArticleEditController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param {Object} article
	 * @param {ArticleService} ArticleService
	 * @param {User} User
	 * @param {Notifications} Notifications
	 */
	constructor($scope, article, ArticleService, User, Notifications) {
		$scope.article = article;
		$scope.token = User.getToken();
		$scope.submit = () => {
			if ($scope.form.$valid) {
				$scope.loading = true;
				$scope.article.save().then(() => {
					if ($scope.image) {
						ArticleService.uploadImage(article, $scope.image).then(onComplete);
					} else {
						onComplete();
					}
				});
			}
		};

		function onComplete() {
			Notifications.changesSaved();
			$scope.reloadTable();
			$scope.$dismiss();
		}
	}
}