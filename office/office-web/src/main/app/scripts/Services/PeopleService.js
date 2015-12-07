export class PeopleService {
	/**
	 * @ngInject
	 * @param Restangular
	 */
	constructor(Restangular) {
		this.rest = Restangular;
		this.people = Restangular.all('people');
	}

	findAll() {
		return this.people.getList();
	}

	findAllCurators() {
		return this.people.all('curators').getList();
	}

	findCurators(curator) {
		if (curator) {
			return this.rest.one('people/sameOrgCurators/ofUser', curator).getList();
		}

		return this.rest.all('people/sameOrgCurators').getList();
	}

	getUserForVerification(code) {
		return this.rest.one('people/discoveryUser/byCode', code).get();
	}

	verifyUser(user) {
		return this.rest.all('people/discoveryUser/verify').post(user);
	}
}