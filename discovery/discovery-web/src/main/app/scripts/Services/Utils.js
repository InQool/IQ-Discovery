/**
 * @param {String} string
 * @return {String}
 */
export function quote(string) {
    return '"' + string + '"';
}

export function quoteEscaped(string) {
    return `\\"${string}\\"`;
}

/**
 * @param {String} string
 * @return {String}
 */
export function stripHtml(string) {
    return string.replace(/(<([^>]+)>)/ig, '');
}

const SOLR_SPECIAL_CHARS_REGEXP = /(\+|\-|[&]{2}|[\|]{2}|!|\(|\)|\{|}|\[|]|\^|"|~|:|\\|\/)/;

/**
 * @param {String} value
 * @return {String}
 */
export function escapeQuery(value) {
    let words = value.split(' ');
    if (words.length === 1) {
        let match = value.match(SOLR_SPECIAL_CHARS_REGEXP);
        if (match && match.length > 0) {
            return quoteEscaped(value);
        }
    } else {
        return quoteEscaped(value);
    }

    return value;
}