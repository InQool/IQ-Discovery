import {getAuthorizationHeader} from '../Token.js';

const ROLES = {
	'org_admin': 'Administrátor instituce',
	'redactor': 'Redaktor',
	'curator': 'Kurátor',
	'sys_admin': 'Administrátor systému'
};

export class OrganizationService {
	/**
	 * @ngInject
	 * @param Restangular
	 */
	constructor(Restangular) {
		this.rest = Restangular;
		this.settings = Restangular.one('orgSettings/current');
		this.organization = Restangular.service('organization');
	}

	findAll() {
		return this.organization.getList();
	}

	find(id) {
		return this.organization.one(id).get();
	}

	getRole(name) {
		return ROLES[name];
	}

	resync(id) {
		return this.organization.one(id).post('resync');
	}

	getSettings() {
		return this.settings.get();
	}

	saveSettings(data) {
		return this.settings.customPUT(data);
	}

	uploadFile(type, file) {
		let formData = new FormData();
		formData.append('file', file);

		return this.settings.withHttpConfig({transformRequest: angular.identity, transformResponse: undefined}).customPOST(formData, type, undefined, {'Content-Type': undefined});
	}

	removeFile(type) {
		return this.settings.one(type).remove();
	}
}