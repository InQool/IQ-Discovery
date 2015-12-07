"use strict";
export class ArticleCreateController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param {ArticleService} ArticleService
	 * @param {Notifications} Notifications
	 */
	constructor($scope, ArticleService, Notifications) {
		$scope.article = {
			active: true
		};

		$scope.$watch('image', () => {
			if ($scope.image) {
				let fileReader = new FileReader();
				fileReader.readAsDataURL($scope.image);
				fileReader.onload = () => {
					$scope.imageUrl = fileReader.result;
				};
			}
		});

		$scope.submit = () => {
			if ($scope.form.$valid) {
				$scope.loading = true;
				ArticleService.create($scope.article).then(article => {
					ArticleService.uploadImage(article, $scope.image).then(() => {
						Notifications.addSuccess('Článek byl vytvořen.');
						$scope.reloadTable();
						$scope.$dismiss();
					});
				})
			}
		}
	}
}