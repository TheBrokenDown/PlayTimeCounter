package ru.delusive.ptc;

import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;

public class NucleusIntegration {

    private NucleusAFKService afkService;

    public NucleusIntegration(){
        afkService = NucleusAPI.getAFKService().get();
    }

    public NucleusAFKService getAfkService() {
        return afkService;
    }

}
