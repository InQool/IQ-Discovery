export function getAuthorizationHeader(token) {
    return {'Authorization': `Token ${token}`};
}