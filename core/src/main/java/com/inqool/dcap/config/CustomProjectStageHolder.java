package com.inqool.dcap.config;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.projectstage.ProjectStageHolder;

/**
 * @author Lukas Jane (inQool) 8. 4. 2015.
 */
public class CustomProjectStageHolder implements ProjectStageHolder
{
    public static final class DevelopmentMatus extends ProjectStage {}
    public static final class DevelopmentLukess extends ProjectStage {}
    public static final class DevelopmentKudlajz extends ProjectStage {}
    public static final class StagingSCK extends ProjectStage {}
    public static final class ProductionSCK extends ProjectStage {}

    public static final DevelopmentMatus DevelopmentMatus = new DevelopmentMatus();
    public static final DevelopmentLukess DevelopmentLukess = new DevelopmentLukess();
    public static final DevelopmentKudlajz DevelopmentKudlajz = new DevelopmentKudlajz();
    public static final StagingSCK StagingSCK = new StagingSCK();
    public static final ProductionSCK ProductionSCK = new ProductionSCK();
}




