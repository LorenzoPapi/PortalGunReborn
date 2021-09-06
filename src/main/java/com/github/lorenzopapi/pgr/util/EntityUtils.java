package com.github.lorenzopapi.pgr.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class EntityUtils {
	public static RayTraceResult rayTrace(World world, Vector3d start, Vector3d end, Entity exception, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode, Predicate<Entity> filter) {
		BlockRayTraceResult rtr = world.rayTraceBlocks(new RayTraceContext(start, end, blockMode, fluidMode, exception));
		if (rtr.getType() == RayTraceResult.Type.BLOCK) {
			end = rtr.getHitVec();
		}
		EntityRayTraceResult rtr1 = ProjectileHelper.rayTraceEntities(world, exception, start, end, new AxisAlignedBB(start, end).grow(1F), filter);
		return rtr1 != null ? rtr1 : rtr;
	}

	public static Vector3d averagePairPosition(List<BlockPos> poses, Vector3d player) {
		double ax = 0, az = 0;
		for (BlockPos p : poses) {
			ax += p.getX();
			az += p.getZ();
		}
		return new Vector3d(ax / poses.size(), player.y, az / poses.size()).add(new Vector3d(getDecimals(player.x), 0, getDecimals(player.z)));
	}

	public static double getDecimals(double d) {
		String s = String.valueOf(d);
		return Double.parseDouble(s.substring(s.indexOf(".")));
	}
}
