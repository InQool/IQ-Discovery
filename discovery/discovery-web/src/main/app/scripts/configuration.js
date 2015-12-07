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

Configuration = _.merge(Configuration, {
    development: process.env.STAGE === 'development' || process.env.TARGET === 'zlinskykraj',
    subtitle: undefined,
    disabledStates: undefined,
    clipoard: {
        reservationOfferVerification: undefined
    },
    document: {
        allowDocumentSubType: undefined,
        hideInventoryId: undefined,
        showFieldsForAnonymousUser: undefined,
        showCustomFieldsForAnonymousUser: undefined
    }
});

angular.module('zdo.discovery.configuration', []).constant('Configuration', Configuration);
