if (_.isUndefined(process.env.TARGET)) {
    throw new Error('Target is not defined.');
}

if (_.isUndefined(process.env.STAGE)) {
    throw new Error('Stage is not defined.');
}

let Configuration;
if (process.env.TARGET === 'zlinskykraj') {
    Configuration = require('./Configurations/config.zlinskykraj');
} else if (process.env.TARGET === 'stredoceskykraj') {
    Configuration = require('./Configurations/config.stredoceskykraj');
}

Configuration = Object.assign({}, {
    logo: undefined,
    loginViaCredentialsAllowed: undefined,
    logoutRedirect: undefined,
    development: process.env.STAGE === 'development',
    disabledStates: [],
    document: {
        allowDocumentTypeChange: undefined,
        allowEnums: undefined
    },
    googleAnalyticsID: undefined
}, Configuration);

angular.module('zdo.office.configuration', []).constant('Configuration', Configuration);
