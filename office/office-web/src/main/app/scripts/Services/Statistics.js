export class Statistics {
	/**
	 * @ngInject
	 */
	constructor(Restangular) {
		this.rest = Restangular.all('stats');
	}

	_get(url, limit = 5) {
		return this.rest.one(url).get({limit: limit});
	}

	_getList(url, limit = 5) {
		return this.rest.all(url).getList({limit: limit});
	}

	findTopCurators() {
		return this._getList('topCurators');
	}

	findTopOrganizations() {
		return this._getList('topOrganizations');
	}

	findTopDocTypes(organization = null) {
		return this.rest.one('topDocTypes').get({limit: 15, organization: organization});
	}

	findTopDocSubTypes(organization = null) {
		return this.rest.one('topDocSubTypes').get({limit: 15, organization: organization});
	}

	findTopZdoTypes(organization = null) {
		return this.rest.one('topZdoTypes').get({limit: 15, organization: organization});
	}

	findTopViewedDocs() {
		return this._get('topViewedDocs');
	}

	findTopFavoriteDocs() {
		return this._get('topFavoriteDocs');
	}

	findWeeklyDocumentStats(organization = null, user = null) {
		return this.rest.all('weeklyDocStats').getList({limit: 20, organization: organization, user: user});
	}
}