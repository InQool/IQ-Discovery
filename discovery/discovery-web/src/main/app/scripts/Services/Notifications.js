let _currentMessage = null, _timeout;

export class Notifications {
	/**
	 * @ngInject
	 * @param $timeout
	 */
	constructor($timeout) {
		this.timeout = $timeout;
	}

	_add(message, type) {
		_currentMessage = {
			message: message,
			type: 'alert-' + type
		};

		this._cancelTimeout();
		_timeout = this.timeout(() => {
			_currentMessage = null;

			this._cancelTimeout();
		}, 3000);
	}

	_cancelTimeout() {
		if (_timeout) {
			this.timeout.cancel(_timeout);
		}
	}

	getMessage() {
		return _currentMessage;
	}

	addSuccess(message) {
		this._add(message, 'success');
	}

	addError(message) {
		this._add(message, 'danger');
	}

	loginRequired() {
		this.addError(__('Musíte být přihlášen.'));
	}
}