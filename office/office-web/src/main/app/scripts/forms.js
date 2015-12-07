angular.module('zdo.office.forms', [])
	.directive('loginForm', ($state, User, Notifications) => ({
		scope: {},
		restrict: 'E',
		templateUrl: 'views/forms/Login/form.html',
		link: (scope) => {
			scope.user = {};
			scope.submit = () => {
				if (scope.form.$valid) {
					scope.loading = true;

					try {
						User.login(scope.user).then(() => {
							Notifications.addSuccess('Byl jste přihlášen.');
							$state.go('secured.home');
						}).catch(response => {
							if (response.status === 401) {
								Notifications.addError('Špatné heslo.');
							} else if (response.status === 404) {
								Notifications.addError('Uživatelské jméno neexistuje.');
							}

							Notifications.addError('Něco se stalo.');
							scope.loading = false;
						});
					} catch (error) {
						Notifications.addError(error);
						scope.loading = false;
					}
				}
			};
		}
	}));