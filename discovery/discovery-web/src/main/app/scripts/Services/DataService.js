export class DataService {
	/**
	 * @ngInject
	 * @param RestangularCachable
	 */
	constructor(RestangularCachable) {
		this.data = RestangularCachable.all('data');
		this.portal = RestangularCachable.one('portal/settings');
		this.organization = RestangularCachable.all('orgSettings');
	}

	getOrganizationSettings(organization) {
		return this.organization.get(organization);
	}

	getPortalSettings() {
		return this.portal.get();
	}

	findOCR(invId, page) {
		return this.data.one('ocr', invId).one('', page).get();
	}
}