export class TypeService {
	/**
	 * @ngInject
	 * @param Restangular
	 */
	constructor(Restangular) {
		this.rest = Restangular;
	}

	findAll() {
		return this.rest.all('type').getList();
	}

	find(id) {
		return this.rest.one('type', id).get();
	}

	create(name) {
		return this.rest.all('type').post('', {name: name});
	}

	remove(id) {
		return this.rest.one('type', id).remove();
	}
}