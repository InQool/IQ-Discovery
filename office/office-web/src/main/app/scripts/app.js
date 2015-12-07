require('babelify/polyfill');

const moment = require('moment');
const NProgress = require('nprogress');

// noinspection Eslint
global.jQuery = global.$ = require('jquery');
global.PNotify = null;
global._ = require('lodash');
global.CodeMirror = require('codemirror');

require('angular');
require('angular-deferred-bootstrap');
require('angular-i18n/cs');
require('angular-sanitize');
require('angular-bootstrap');
require('ngAnalytics');

require('restangular');
require('codemirror/mode/css/css.js');

require('flot-charts');
require('flot-charts/jquery.flot.time');
require('flot.tooltip');

require('angular-file-model');

require('eonasdan-bootstrap-datetimepicker');
require('moment/locale/cs');

require('jquery-ui');
require('pnotify');
require('ui-select');
require('jquery.fancytree/dist/jquery.fancytree');

require('./configuration');
require('./states');
require('./services');
require('./controllers');
require('./directives');
require('./forms');
require('./templates');

import ROLES from './Roles.js';
import {getAuthorizationHeader} from './Token.js';

// noinspection Eslint
window.deferredBootstrapper.bootstrap({
    element: document.body,
    module: 'zdo.office',
    bootstrapConfig: {
        strictDi: true
    },
    resolve: {
        /**
         * @ngInject
         */
        userInfo: ($http) => {
            let token = localStorage.getItem('token');
            return $http({
                method: 'GET',
                url: '/dcap/office/token',
                headers: token ? getAuthorizationHeader(token) : {}
            }).success((data, status, headers) => {
                token = headers('authctoken');
                if (token) {
                    localStorage.setItem('token', token);
                }

                return data;
            }).catch(() => {
                return null;
            });
        }
    }
});

angular.module('zdo.office', [
    'ngSanitize',
    'ui.select',
    'ui.bootstrap',
    'restangular',
    'file-model',
    'ngAnalytics',
    'zdo.office.states',
    'zdo.office.services',
    'zdo.office.controllers',
    'zdo.office.directives',
    'zdo.office.forms',
    'zdo.office.templates',
    'zdo.office.configuration'
]).config(config).run(run);

function config(RestangularProvider, $tooltipProvider) {
    RestangularProvider.setBaseUrl('/dcap/office');

    $tooltipProvider.options({
        appendToBody: true
    });
}

/**
 * @param $rootScope
 * @param $window
 * @param $state
 * @param Restangular
 * @param {DocumentService} DocumentService
 * @param {User} User
 * @param {DataTable} DataTable
 * @param {Configuration} Configuration
 * @param {Notifications} Notifications
 * @param userInfo
 */
function run($rootScope, $window, $state, Restangular, DocumentService, User, DataTable, Configuration, Notifications, userInfo) {
    if (userInfo) {
        User.setInfo(userInfo);
        if ($state.is('login')) {
            $state.go('secured.home');
        }
    }

    configureRestangular(Restangular, Notifications, Configuration, DocumentService, User);

    /**
     * @param event
     * @param {Object} toState
     * @param {String} toState.name
     * @param {Object} [toState.data]
     */
    function stateChangeStart(event, toState) {
        NProgress.start();

        if (toState.hasOwnProperty('data')) {
            if (toState.data.hasOwnProperty('restricted') && toState.data.restricted === true && User.isLoggedIn() === false) {
                event.preventDefault();
                $state.go('login');
            }

            if (User.isAllowed(toState) === false) {
                preventAndGoToHomepage(event);
            }

            if (Configuration.disabledStates.indexOf(toState.name) !== -1) { // is disabled state
                preventAndGoToHomepage(event);
            }
        }

        if (toState.name === 'secured.organization' && User.hasRole(ROLES.ORG_ADMIN) && User.hasRole(ROLES.SYS_ADMIN) === false) {
            event.preventDefault();
            $state.go('secured.organization.edit', {id: User.getGroup()});
        }

        function preventAndGoToHomepage(event) {
            event.preventDefault();
            $state.go('secured.home');
        }
    }

    function loadingDone() {
        NProgress.done();
    }

    function stateChangeSuccess() {
        loadingDone();
        $window.scrollTo(0, 0);
    }

    $rootScope.$on('$stateChangeStart', stateChangeStart);
    $rootScope.$on('$stateChangeSuccess', stateChangeSuccess);
    $rootScope.$on('$stateChangeError', loadingDone);

    $rootScope.getTable = function (name) {
        return DataTable.getInstance(name);
    };

    $rootScope.getTableData = function (name) {
        return DataTable.getData(this.getTable(name));
    };

    $rootScope.reloadTable = function (name) {
        let table = this.getTable(name);
        DataTable.reload(table);
    };
}

/**
 *
 * @param Restangular
 * @param {Notifications} Notifications
 * @param {Configuration} Configuration
 * @param {DocumentService} DocumentService
 * @param {User} User
 */
function configureRestangular(Restangular, Notifications, Configuration, DocumentService, User) {
    const booleanProps = ['allowContentPublicly', 'allowEpubExport', 'allowPdfExport', 'watermark'],
        singleStringProps = ['zdoType', 'publishFrom', 'publishTo', 'documentType', 'documentSubType', 'watermarkPosition'],
        booleanPropsToNegate = ['allowContentPublicly'],
        dcTerms = DocumentService.DCTerms,
        dcTermsMultiselect = _.filter(dcTerms, term => term.multiselect);

    function transformArrayToBoolean(data, prop) {
        if (data.hasOwnProperty(prop) && _.isArray(data[prop])) {
            data[prop] = data[prop][0] === 'true';
        }
    }

    function transformBooleanToArray(data, prop) {
        if (data.hasOwnProperty(prop)) {
            data[prop] = [data[prop] ? 'true' : 'false'];
        }
    }

    function errorInterceptor(response) {
        if (response.status === 403) { // forbidden
            Notifications.addError('Nemáte oprávnění použít tuto službu.');
        }

        if (response.status === 404 && response.data.includes('F5 Networks')) {
            expiredSession();
            return false;
        }
    }

    function fullRequestInterceptor(element, operation, what, url, headers, params) {
        const newHeaders = _.merge(headers, getAuthorizationHeader(User.getToken()));

        if (operation === 'get' || operation === 'getList') {
            params._t = moment().unix(); // add timestamp to prevent caching (I'm talking to you Internet Explorer!)
        }

        return {
            headers: newHeaders,
            params: params
        };
    }

    let isAlertShowed = false;

    function expiredSession() {
        if (isAlertShowed === false) {
            alert('Vaše relace již vypršela. Budete přesměrován/a na přihlašovací obrazovku.');
            location.href = location.origin + '/my.policy';
            isAlertShowed = true;
            throw new Error('Session expired.');
        }
    }

    /**
     *
     * @param data
     * @param operation
     * @param what
     * @param {String} url
     * @return {*}
     */
    function responseInterceptor(data, operation, what) {
        if (_.isString(data) && data.includes('F5 Networks')) {
            expiredSession();
        }

        if (operation === 'get') {
            if (what === 'document/detail') {
                angular.forEach(['concept', 'kdr'], type => {
                    if (data.hasOwnProperty(type) === false || _.isNull(data[type])) {
                        return;
                    }

                    const doc = data[type];
                    const fields = {};

                    if (doc.hasOwnProperty('title') === false) {
                        doc.title = null;
                    }

                    for (const prop of booleanProps) {
                        transformArrayToBoolean(doc, prop);
                    }

                    for (const prop of booleanPropsToNegate) {
                        doc[prop] = !doc[prop];
                    }

                    for (const prop of singleStringProps) {
                        if (doc.hasOwnProperty(prop) && _.isArray(doc[prop])) {
                            doc[prop] = doc[prop][0];
                        }
                    }

                    doc._order = [];

                    for (const prop of dcTerms) {
                        if (doc.hasOwnProperty(prop.key)) {
                            doc._order.push(prop.key);
                        }

                        // transform prop visibility
                        const key = prop.key + '_visibility';
                        transformArrayToBoolean(doc, key);
                        if (type === 'concept' && doc.hasOwnProperty(key) === false) {
                            doc[key] = true;
                        }
                    }

                    for (const prop of dcTerms) {
                        const key = prop.key;
                        const isMultiselect = Boolean(_.find(dcTermsMultiselect, {key: key}));

                        let allowEnums = Boolean(Configuration.document.allowEnums);
                        if (allowEnums === false) {
                            allowEnums = doc.zdoType !== 'cho';
                        }

                        if (_.isUndefined(doc[key]) || doc[key].length === 0) {
                            doc[key] = (isMultiselect && allowEnums) ? [] : [''];
                        }
                    }

                    for (const key in doc) {
                        if (doc.hasOwnProperty(key)) {
                            const value = doc[key];
                            if (_.startsWith(key, 'customField_')) {
                                fields[key] = value;
                            }
                        }
                    }

                    for (const prop of ['publishFrom', 'publishTo']) {
                        if (doc.hasOwnProperty(prop)) {
                            doc[prop] = moment.unix(doc[prop]);
                        }
                    }

                    if (doc.hasOwnProperty('watermarkPosition') === false) {
                        doc.watermarkPosition = 'cc';
                    }

                    doc.customFields = [];

                    for (const key in fields) {
                        if (fields.hasOwnProperty(key)) {
                            if (!_.endsWith(key, '_name') && !_.endsWith(key, '_visibility')) {
                                let index = _.lastIndexOf(key, '_') + 1;
                                index = Number(key.substr(index));

                                const name = fields['customField_' + index + '_name'];
                                const visibility = fields['customField_' + index + '_visibility'];
                                const values = fields['customField_' + index];

                                doc.customFields.push({
                                    name: name[0],
                                    deleted: false,
                                    visibility: visibility ? (visibility[0] === 'true') : true,
                                    order: index,
                                    values: values
                                });

                                delete doc['customField_' + index + '_name'];
                                delete doc['customField_' + index + '_visibility'];
                                delete doc['customField_' + index];
                            }
                        }
                    }
                });
            }

            if (data.hasOwnProperty('publishedFrom')) {
                data.publishedFrom = moment.unix(data.publishedFrom);
            }

            if (data.hasOwnProperty('publishedTo')) {
                data.publishedTo = moment.unix(data.publishedTo);
            }
        }

        return data;
    }

    function requestInterceptor(data, operation, what) {
        if (operation === 'put') {
            if (what === 'document/detail') {
                let clones = _.cloneDeep(data), customFields = {}, result;

                angular.forEach(clones, (clone, index) => {
                    clone = clone.plain();

                    if (clone.hasOwnProperty('title') && _.isArray(clone.title) === false) {
                        clone.title = [clone.title];
                    }

                    angular.forEach(booleanPropsToNegate, prop => {
                        clone[prop] = !clone[prop];
                    });

                    angular.forEach(booleanProps, prop => {
                        transformBooleanToArray(clone, prop);
                    });

                    angular.forEach(singleStringProps, prop => {
                        if (clone.hasOwnProperty(prop)) {
                            clone[prop] = [clone[prop] || ''];
                        }
                    });

                    angular.forEach(dcTerms, prop => {
                        if (clone.hasOwnProperty(prop.key) === false && _.isArray(clones) === false) {
                            clone[prop.key] = [''];
                        }

                        transformBooleanToArray(clone, prop.key + '_visibility');
                    });

                    if (clone.hasOwnProperty('customFields')) {
                        angular.forEach(clone.customFields, field => {
                            result = {};
                            if (field.deleted === true) {
                                result['customField_' + field.order + '_name'] = [];
                                result['customField_' + field.order + '_visibility'] = [];
                                result['customField_' + field.order] = [];
                            } else {
                                result['customField_' + field.order + '_name'] = [field.name];
                                result['customField_' + field.order + '_visibility'] = [field.visibility ? 'true' : 'false'];
                                result['customField_' + field.order] = field.values;
                            }

                            customFields = _.merge(customFields, result);
                        });

                        clone = _.merge(clone, customFields);
                        delete clone.customFields;
                    }

                    clones[index] = clone;
                });

                return clones;
            }
        }

        return data;
    }

    Restangular.addErrorInterceptor(errorInterceptor);
    Restangular.addFullRequestInterceptor(fullRequestInterceptor);
    Restangular.addResponseInterceptor(responseInterceptor);
    Restangular.addRequestInterceptor(requestInterceptor);
}
