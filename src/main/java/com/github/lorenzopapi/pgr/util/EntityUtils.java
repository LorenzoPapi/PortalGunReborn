package com.github.lorenzopapi.pgr.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class EntityUtils {
	public static void playSoundAtEntity(Entity e, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		e.getEntityWorld().playSound(e.world.isRemote ? Minecraft.getInstance().player : null, e.getPosX(), e.getPosY() + e.getEyeHeight(), e.getPosZ(), sound, category, volume, pitch);
	}

	public static RayTraceResult rayTrace(World world, Vector3d start, Vector3d end, Entity exception, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode, Predicate<Entity> filter) {
		BlockRayTraceResult rtr = world.rayTraceBlocks(new RayTraceContext(start, end, blockMode, fluidMode, exception));
		if (rtr.getType() == RayTraceResult.Type.BLOCK) {
			end = rtr.getHitVec();
		}
		EntityRayTraceResult rtr1 = ProjectileHelper.rayTraceEntities(world, exception, start, end, new AxisAlignedBB(start, end).grow(1F), filter);
		return rtr1 != null ? rtr1 : rtr;
	}
}
