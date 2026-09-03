package com.example.curiosfix;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CuriosFix.MODID)
public class CuriosFix {
    public static final String MODID = "curios_fix";
    public static final Logger LOGGER = LogManager.getLogger();

    public CuriosFix() {
        LOGGER.info("Curios Fix loaded - fixing slot size persistence bug");
    }
}
