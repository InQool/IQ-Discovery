export class Utils {
	static quote(string) {
		return '\'' + string + '\'';
	}

	static doubleQuote(string) {
		return `\"${string}\"`;
	}

	static toArray(element) {
		return _.isArray(element) ? element : [element];
	}
}