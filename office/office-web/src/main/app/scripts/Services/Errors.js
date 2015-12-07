const _messages = {
	3: 'Koncept periodika tohoto vydání je již vytvořen v dávce <strong><%= batch %></strong>.',
	5: 'Dávku nelze předat. Její dokumenty se nachází ve více dávkách.'
};

export class Errors {
	static getMessage(code) {
		if (_messages.hasOwnProperty(code) === false) {
			throw 'Invalid error code.';
		}

		return _.template(_messages[code]);
	}
}