export class ExpoService {
	/**
	 * @ngInject
	 */
	constructor(RestangularCachable) {
		this.expo = RestangularCachable.all('expo');
	}

	findAll() {
		return this.expo.getList();
	}

	find(id) {
		return this.expo.get(id);
	}
}