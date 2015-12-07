const isDevelopment = process.env.STAGE === 'development';

const Configuration = {
    developmentShowWarning: isDevelopment,
    googleAnalyticsID: isDevelopment ? 'ga:111301937' : 'ga:111994347',
    logoutRedirect: 'logout.html',
    logo: 'logo.zlinskykraj.gif',
    document: {
        allowDocumentTypeChange: true,
        allowEnums: true
    }
};

module.exports = Configuration;
