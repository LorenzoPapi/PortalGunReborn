package com.github.lorenzopapi.pgr.util;

import com.github.lorenzopapi.pgr.handler.PGRClientHandler;
import com.github.lorenzopapi.pgr.handler.PGRServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
	public static final Logger LOGGER = LogManager.getLogger("Portal Gun Reborn");
	public static final String MOD_ID = "portalgunreborn";
	public static PGRServerHandler serverEH;
	public static PGRClientHandler clientEH;
}
