require('angular-cookies');
require('angular-gettext');

angular.module('zdo.discovery.translations', [
	'ngCookies',
	'gettext'
]).run(run);

/**
 * @param $rootScope
 * @param {Translator} Translator
 */
function run($rootScope, Translator) {
	Translator.initialize();

	$rootScope.language = Translator.getCurrent();
}