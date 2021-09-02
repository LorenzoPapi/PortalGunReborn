package com.github.lorenzopapi.pgr.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class EntityUtils {
	public static void playSoundAtEntity(Entity ent, SoundEvent soundEvent, SoundCategory soundCategory, float volume, float pitch) {
		ent.getEntityWorld().playSound(ent.getEntityWorld().isRemote() ? Minecraft.getInstance().player : null, ent.getPosX(), ent.getPosY() + ent.getEyeHeight(), ent.getPosZ(), soundEvent, soundCategory, volume, pitch);
	}

	//TODO: wth does this mean why cannot I use wolrld ray trace smh sfsfhsfuishf
	public static RayTraceResult rayTrace(World world, Vector3d start, Vector3d end, Entity exception, boolean checkCollision, RayTraceContext.BlockMode blockMode, Predicate<BlockInfo> blockFilter, RayTraceContext.FluidMode fluidMode, Predicate<Entity> filter) {
		RayTraceResult rtr = IBlockReader.doRayTrace(new RayTraceContext(start, end, blockMode, fluidMode, exception), (context, pos) -> {
			BlockState blockstate = world.getBlockState(pos);
			if (blockFilter.test(new BlockInfo(world, blockstate, pos))) {
				FluidState fluid = world.getFluidState(pos);
				Vector3d vec3d = context.getStartVec();
				Vector3d vec3d1 = context.getEndVec();

				BlockRayTraceResult brtr = world.rayTraceBlocks(vec3d, vec3d1, pos, context.getBlockShape(blockstate, world, pos), blockstate);
				double d0 = brtr == null ? Double.MAX_VALUE : vec3d.squareDistanceTo(brtr.getHitVec());

				BlockRayTraceResult brtr1 = context.getFluidShape(fluid, world, pos).rayTrace(vec3d, vec3d1, pos);
				double d1 = brtr1 == null ? Double.MAX_VALUE : vec3d.squareDistanceTo(brtr1.getHitVec());

				return d0 <= d1 ? brtr : brtr1;
			}
			return null;
		}, (context) -> {
			Vector3d vec3d = context.getStartVec().subtract(context.getEndVec());
			return BlockRayTraceResult.createMiss(context.getEndVec(), Direction.getFacingFromVector(vec3d.x, vec3d.y, vec3d.z), new BlockPos(context.getEndVec()));
		});
		if (checkCollision) {
			if (rtr.getType() != RayTraceResult.Type.MISS) {
				end = rtr.getHitVec();
			}

			RayTraceResult rtr1 = ProjectileHelper.rayTraceEntities(world, exception, start, end, new AxisAlignedBB(start, end).grow(1F), filter);
			if (rtr1 != null) {
				rtr = rtr1;
			}
		}
		return rtr;
	}

	public static class BlockInfo {
		public final IWorldReader world;
		public final BlockState state;
		public final BlockPos pos;

		public BlockInfo(IWorldReader world, BlockState state, BlockPos pos) {
			this.world = world;
			this.state = state;
			this.pos = pos;
		}
	}
}
