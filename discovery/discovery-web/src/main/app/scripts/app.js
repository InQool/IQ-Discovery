require('babelify/polyfill');

const $ = require('jquery');
const _ = require('lodash');

global.jQuery = global.$ = $;
global._ = _;
global.__ = (string) => string; // for extracting strings to translate

require('angular');
require('angular-sanitize');
require('angular-bootstrap');
require('ui-select');
require('angular-jwt');
require('angular-i18n/cs');

require('bootstrap/js/transition');
require('bootstrap/js/carousel');

require('flot');
require('flot.selection');

require('restangular');

require('./states');
require('./controllers');
require('./directives');
require('./services');
require('./templates');
require('./filters');
require('./translations');
require('./configuration');
require('./Enums');

angular.module('zdo.discovery', [
	'ngSanitize',
	'ui.bootstrap',
	'ui.select',
	'restangular',
	'angular-jwt',
	'zdo.discovery.enums',
	'zdo.discovery.states',
	'zdo.discovery.controllers',
	'zdo.discovery.directives',
	'zdo.discovery.services',
	'zdo.discovery.templates',
	'zdo.discovery.filters',
	'zdo.discovery.translations',
	'zdo.discovery.configuration'
])
	.config(config)
	.run(run);

function config(RestangularProvider, $tooltipProvider) {
	RestangularProvider.setBaseUrl('/dcap/discovery');
	RestangularProvider.setDefaultHeaders({
		'Content-Type': 'application/json'
	});

	$tooltipProvider.options({
		appendToBody: true
	});
}

/**
 *
 * @param $rootScope
 * @param $window
 * @param $state
 * @param {User} User
 * @param {Page} Page
 * @param {Notifications} Notifications
 * @param {DataService} DataService
 * @param Restangular
 */
function run($rootScope, $window, $state, User, Page, Notifications, DataService, Restangular) {

	loadTitle();
	registerListeners();

	// Facebook SDK
	$window.fbAsyncInit = function () {
		FB.init({
			appId: '1633408920244543',
			version: 'v2.4'
		});
	};

	Restangular.addFullRequestInterceptor(requestInterceptor);
	Restangular.addErrorInterceptor(errorInterceptor);

	function requestInterceptor(element, operation, what, url, headers) {
		let token = User.getToken();
		if (token) {
			headers.Authorization = `Token ${User.getToken()}`;
		}

		return {
			headers: headers
		};
	}

	function errorInterceptor(response) {
		if (response.status === 401) {
			Notifications.addError(__('Musíte být přihlášen.'));
		}
	}

	function stateChangeStart(event, toState) {
		if (toState.hasOwnProperty('data')) {
			if (toState.data.hasOwnProperty('restricted') && toState.data.restricted === true && User.isAllowedToEnterPage() === false) {
				event.preventDefault();
				$state.go('login');
			}

			if (toState.data.hasOwnProperty('loggedIn') && toState.data.loggedIn === true && User.isLoggedIn() === false) {
				event.preventDefault();
				$state.go('secured.login');
			}
		}
	}

	function stateChangeError() {
		throw arguments[5];
	}

	function registerListeners() {
		let onStateChangeStartListener = $rootScope.$on('$stateChangeStart', stateChangeStart);
		let onStateChangeErrorListener = $rootScope.$on('$stateChangeError', stateChangeError);

		$rootScope.$on('$destroy', unregisterListeners);

		function unregisterListeners() {
			onStateChangeStartListener();
			onStateChangeErrorListener();
		}
	}

	function loadTitle() {
		DataService.getPortalSettings().then(settings => {
			Page.setTitle(settings.discoveryTitle);
		});
	}
}