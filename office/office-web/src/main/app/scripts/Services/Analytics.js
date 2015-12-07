export class Analytics {
	/**
	 * @ngInject
	 */
	constructor(Restangular) {
		this.rest = Restangular;
	}

	getToken() {
		return this.rest.one('gatoken').get();
	}
}