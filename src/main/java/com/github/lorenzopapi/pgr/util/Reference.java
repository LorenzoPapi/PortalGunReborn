package com.github.lorenzopapi.pgr.util;

import com.github.lorenzopapi.pgr.handler.PGRClientEventHandler;
import com.github.lorenzopapi.pgr.handler.PGREventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final Logger LOGGER = LogManager.getLogger("Portal Gun Reborn");
	public static final String MODID = "portalgunreborn";
	public static PGREventHandler pgrEventHandler;
	public static PGRClientEventHandler pgrClientEventHandler;
}
