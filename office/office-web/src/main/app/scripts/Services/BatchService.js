export class BatchService {
	/**
	 * @ngInject
	 * @param Restangular
	 */
	constructor(Restangular) {
		this.rest = Restangular;
		this.batch = Restangular.service('batch');
	}

	create(name) {
		return this.batch.post(null, {name: name});
	}

	find(batch) {
		return this.batch.one(batch).get();
	}

	findAll(state) {
		return this.batch.getList({state: state});
	}

	findDocuments(batch) {
		return this.batch.one(batch).all('document').getList();
	}

	findTree(batch) {
		return this.batch.one(batch).one('tree').get();
	}

	findUser(user, state) {
		return this.rest.one('batch/ofUser', user).getList('', {state: state});
	}

	remove(batches) {
		return this.rest.all('batch/delete').post(batches);
	}

	handOver(source, target, batches) {
		return this.rest.all('batch').customPOST(batches, 'handOver', {sourceUserId: source, targetUserId: target});
	}

	handOverAll(source, target) {
		return this.rest.all('batch/handOverAllBatches').post(null, {sourceUserId: source, targetUserId: target});
	}
}