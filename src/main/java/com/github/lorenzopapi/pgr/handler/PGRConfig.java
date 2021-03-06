package com.github.lorenzopapi.pgr.handler;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

// TODO: translate
public class PGRConfig {
	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;

	static {
		final Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = clientPair.getRight();
		CLIENT = clientPair.getLeft();
		final Pair<Common, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = commonPair.getRight();
		COMMON = commonPair.getLeft();

	}

	public static class KeyBinds {
		public static final KeyBinding keyGrab = new KeyBinding("key.grab", GLFW.GLFW_KEY_G, "key.categories.portalgun");
		public static final KeyBinding keyReset = new KeyBinding("key.resetPortals", GLFW.GLFW_KEY_R, "key.categories.portalgun");
		public static final KeyBinding keyZoom = new KeyBinding("key.zoom", InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, "key.categories.portalgun");

		public static void registerKeyBindings() {
			ClientRegistry.registerKeyBinding(keyGrab);
			ClientRegistry.registerKeyBinding(keyReset);
			ClientRegistry.registerKeyBinding(keyZoom);
		}
	}

	public static class Client {
		public final ForgeConfigSpec.IntValue portalgunIndicatorSize;

		public Client(ForgeConfigSpec.Builder builder) {
			builder.comment("Rendering-related settings").push("client-only");
			portalgunIndicatorSize = builder
					.comment("Portal Gun indicator size percentage")
					.defineInRange("size", 30, 0, 100);
			builder.pop();
		}
	}

	public static class Common {
		public final ForgeConfigSpec.IntValue maxShootDistance;
		public final ForgeConfigSpec.BooleanValue canFireThroughLiquid;
		public final ForgeConfigSpec.BooleanValue canFireThroughGlass;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Portal Gun Settings").push("portalgun");
			maxShootDistance = builder
					.comment("Maximum distance to fire portals from the Portal Gun.")
					.defineInRange("distance", 10000, 1, Integer.MAX_VALUE);
			canFireThroughLiquid = builder
					.comment("Can the projectile fired by the Portal Gun go through liquid?")
					.define("canGoThroughLiquid", false);
			canFireThroughGlass = builder
					.comment("Can the projectile fired by the Portal Gun go through glass?")
					.define("canGoThroughGlass", false);
			builder.pop();
		}
	}
}
