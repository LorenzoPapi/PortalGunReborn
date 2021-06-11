package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public class PGRSoundEvents {
	public static SoundEvent p_portal_enter;
	public static SoundEvent p_portal_exit;
	public static SoundEvent p_portal_fizzle;
	public static SoundEvent p_portal_invalid_surface;
	public static SoundEvent p_portal_open_blue;
	public static SoundEvent p_portal_open_red;
	public static SoundEvent p_wpn_portal_ambient_lp;
	public static SoundEvent pg_object_use_failure;
	public static SoundEvent pg_object_use_lp_loop;
	public static SoundEvent pg_object_use_lp_start;
	public static SoundEvent pg_object_use_stop;
	public static SoundEvent pg_portal_invalid_surface_swt;
	public static SoundEvent pg_wpn_portal_fizzler_shimmy;
	public static SoundEvent pg_wpn_portal_gun_fire_blue;
	public static SoundEvent pg_wpn_portal_gun_fire_red;
	public static SoundEvent pg_wpn_portalgun_activation;

	public static void init(RegistryEvent.Register<SoundEvent> registry) {
		p_portal_enter = register(registry, "portal.portal_enter");
		p_portal_exit = register(registry, "portal.portal_exit");
		p_portal_fizzle = register(registry, "portal.portal_fizzle");
		p_portal_invalid_surface = register(registry, "portal.portal_invalid_surface");
		p_portal_open_blue = register(registry, "portal.portal_open_blue");
		p_portal_open_red = register(registry, "portal.portal_open_red");
		p_wpn_portal_ambient_lp = register(registry, "portal.wpn_portal_ambient_lp");
		pg_object_use_failure = register(registry, "portalgun.object_use_failure");
		pg_object_use_lp_loop = register(registry, "portalgun.object_use_lp_loop");
		pg_object_use_lp_start = register(registry, "portalgun.object_use_lp_start");
		pg_object_use_stop = register(registry, "portalgun.object_use_stop");
		pg_portal_invalid_surface_swt = register(registry, "portalgun.portal_invalid_surface_swt");
		pg_wpn_portal_fizzler_shimmy = register(registry, "portalgun.wpn_portal_fizzler_shimmy");
		pg_wpn_portal_gun_fire_blue = register(registry, "portalgun.wpn_portal_gun_fire_blue");
		pg_wpn_portal_gun_fire_red = register(registry, "portalgun.wpn_portal_gun_fire_red");
		pg_wpn_portalgun_activation = register(registry, "portalgun.wpn_portalgun_activation");
	}

	private static SoundEvent register(RegistryEvent.Register<SoundEvent> registry, String name) {
		ResourceLocation rs = new ResourceLocation(Reference.MODID, name);
		SoundEvent event = new SoundEvent(rs).setRegistryName(rs);
		registry.getRegistry().register(event);
		return event;
	}

}
