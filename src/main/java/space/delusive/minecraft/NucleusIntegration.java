package space.delusive.minecraft;

import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.module.afk.NucleusAFKService;

public class NucleusIntegration {
    private final NucleusAFKService afkService;

    public NucleusIntegration() {
        afkService = NucleusAPI.getAFKService().get();
    }

    public NucleusAFKService getAfkService() {
        return afkService;
    }

}
