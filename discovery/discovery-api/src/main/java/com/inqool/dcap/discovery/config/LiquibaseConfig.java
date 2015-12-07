package com.inqool.dcap.discovery.config;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
* @author Lukas Jane (inQool) 16. 2. 2015.
*/
@ApplicationScoped
@SuppressWarnings("unused")
public class LiquibaseConfig {
    @Resource(mappedName = "java:jboss/datasources/DcapDS")
    @Produces
    @LiquibaseType
    private DataSource myDataSource;

    @Inject
    private ProjectStage projectStage;

    @Produces
    @LiquibaseType
    public CDILiquibaseConfig createConfig() {
        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog(ConfigResolver.getProjectStageAwarePropertyValue("liquibase.changelog"));

        config.setContexts(projectStage.toString());
        return config;
    }

    @Produces
    @LiquibaseType
    public ResourceAccessor create() {
        return new ClassLoaderResourceAccessor(getClass().getClassLoader());
    }
}
