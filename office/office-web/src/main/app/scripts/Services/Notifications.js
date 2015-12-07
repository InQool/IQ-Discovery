'use strict';

let _disabled = false;

export class Notifications {
	_create(type, title, message) {
		if (_disabled) {
			return;
		}

		new PNotify({
			title: title,
			text: message,
			addclass: 'stack_bar_top',
			type: type,
			shadow: false,
			stack: {
				"dir1": "down",
				"dir2": "right",
				"push": "top",
				"spacing1": 0,
				"spacing2": 0
			},
			styling: 'fontawesome',
			delay: 3000,
			width: '100%'
		});
	}

	disable() {
		_disabled = true;
	}

	addSuccess(message) {
		this._create('success', 'A je to!', message);
	}

	addError(message) {
		this._create('error', 'Chyba!', message);
	}

	changesSaved() {
		this.addSuccess('Změny byly uloženy.');
	}
}