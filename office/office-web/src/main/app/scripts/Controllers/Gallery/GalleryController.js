export class GalleryController {
	/**
	 * @ngInject
	 * @param $scope
	 * @param {GalleryService} GalleryService
	 * @param {Modal} Modal
	 * @param {DataTable} DataTable
	 * @param {Notifications} Notifications
	 */
	constructor($scope, GalleryService, Modal, DataTable, Notifications) {
		let vm = this;

		vm.options = DataTable.fromPromise($scope, () => GalleryService.findAll());
		vm.options = DataTable.disableTableTools(vm.options);
		vm.options.order = [3, 'desc'];

		vm.columns = createColumns();

		vm.activate = activate;
		vm.deactivate = deactivate;
		vm.remove = remove;

		function reloadTable() {
			DataTable.reload();
		}

		function activate(id) {
			let gallery = _.find(DataTable.getData(), {id: id});
			gallery.active = true;
			gallery.save().then(reloadTable);
		}

		function deactivate(id) {
			let gallery = _.find(DataTable.getData(), {id: id});
			gallery.active = false;
			gallery.save().then(reloadTable);
		}

		function remove(id) {
			Modal.deleteWindow({
				id: id,
				subject: 'virtuální výstavu',
				value: 'title',
				success: () => {
					Notifications.addSuccess('Virtuální výstava byla odstraněna.');
				}
			});
		}

		function createColumns() {
			return [
				DataTable.createActions(obj => [
					DataTable.Button.edit('secured.gallery.edit', {id: obj.id}),
					//DataTable.Button.create('preview', obj.id, 'btn btn-default', '', 'fa-search', 'zobrazit v Discovery', true),
					DataTable.Button.delete('vm.remove', obj.id),
					obj.active ? DataTable.Button.create('vm.deactivate', obj.id, 'btn btn-danger', '', 'fa-fw fa-ban', 'deaktivovat', false)
						: DataTable.Button.create('vm.activate', obj.id, 'btn btn-success', '', 'fa-fw fa-check', 'aktivovat', false)
				]).withOption('width', '150px'),
				DataTable.createColumn('title', 'Název', true),
				DataTable.createBoolColumn('active', 'Aktivní'),
				DataTable.createDateColumn('created', 'Vytvořeno'),
				DataTable.createDateColumn('publishedFrom', 'Zveřejněno od'),
				DataTable.createDateColumn('publishedTo', 'Zveřejněno do')
			];
		}
	}
}