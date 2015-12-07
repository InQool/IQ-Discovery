package com.inqool.dcap.office.config;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

@SuppressWarnings("unused")
@ApplicationScoped
public class Liquibase {
    @Resource(mappedName = "java:jboss/datasources/DcapDS")
    @Produces
    @LiquibaseType
    private DataSource myDataSource;

    @Inject
    private ProjectStage projectStage;

    @Inject
    @ConfigProperty(name = "liquibase.changelog")
    private String changelog;

    @Produces
    @LiquibaseType
    public CDILiquibaseConfig createConfig() {
        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog(changelog);
        config.setContexts(projectStage.toString());
        return config;
    }

    @Produces
    @LiquibaseType
    public ResourceAccessor create() {
        return new ClassLoaderResourceAccessor(getClass().getClassLoader());
    }
}
