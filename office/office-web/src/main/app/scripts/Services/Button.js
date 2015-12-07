export class Button {
	/**
	 * @ngInject
	 * @param $state
	 */
	constructor($state) {
		this.state = $state;
	}

	static _create(element, attributes, content) {
		if (element === 'button') {
			attributes.type = 'button';
		}

		attributes = _.map(attributes, (value, key) => {
			return key + '="' + value + '"'
		});

		return '<' + element + ' ' + attributes.join(' ') + '>' + content + '</' + element + '>';
	}

	create(method, parameters, className, text, icon, tooltip) {
		let element,
			attributes = {
				'class': className
			};

		if (tooltip) {
			attributes.tooltip = tooltip;
		}

		if (_.isObject(parameters)) {
			element = 'a';
			attributes.href = this.state.href(method, parameters);
		} else {
			element = 'button';
			if (!_.isArray(parameters)) {
				parameters = [parameters];
			}

			attributes['ng-click'] = method + '(' + parameters.join(', ') + ')';
		}

		return Button._create(element, attributes, '<i class="fa ' + icon + '"></i> ' + text);
	}

	edit(destination, value) {
		let element,
			attributes = {
				'class': 'btn btn-default',
				'tooltip': 'upravit'
			};

		if (_.isObject(value)) {
			element = 'a';
			attributes.href = this.state.href(destination, value);
		} else {
			if (_.isArray(value)) {
				value = value.join(', ');
			}

			element = 'button';
			attributes['ng-click'] = destination + '(' + value + ')';
		}

		return Button._create(element, attributes, '<i class="fa fa-pencil"></i>');
	}

	'delete'(method, value) {
		if (_.isArray(value)) {
			value = value.join(', ');
		}

		return Button._create('button', {
			'class': 'btn btn-danger',
			'ng-click': method + '(' + value + ')',
			'tooltip': 'odstranit'
		}, '<i class="fa fa-trash"></i>');
	}
}