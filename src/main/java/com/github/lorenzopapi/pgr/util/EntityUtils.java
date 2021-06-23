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
	public static RayTraceResult rayTrace(World world, Vector3d origin, Vector3d dest, Entity exception, boolean checkEntityCollision, RayTraceContext.BlockMode blockMode, Predicate<BlockInfo> blockFilter, RayTraceContext.FluidMode fluidMode, Predicate<Entity> filter) {
		RayTraceResult raytraceresult = IBlockReader.doRayTrace(new RayTraceContext(origin, dest, blockMode, fluidMode, exception), (context, pos) -> {
			BlockState blockstate = world.getBlockState(pos);
			if (blockFilter.test(new BlockInfo(world, blockstate, pos))) //Taken from IBlockReader.rayTraceBlocks
			{
				FluidState ifluidstate = world.getFluidState(pos);
				Vector3d vec3d = context.getStartVec();
				Vector3d vec3d1 = context.getEndVec();
				VoxelShape voxelshape = context.getBlockShape(blockstate, world, pos);
				BlockRayTraceResult blockraytraceresult = world.rayTraceBlocks(vec3d, vec3d1, pos, voxelshape, blockstate);
				VoxelShape voxelshape1 = context.getFluidShape(ifluidstate, world, pos);
				BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vec3d, vec3d1, pos);
				double d0 = blockraytraceresult == null ? Double.MAX_VALUE : context.getStartVec().squareDistanceTo(blockraytraceresult.getHitVec());
				double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : context.getStartVec().squareDistanceTo(blockraytraceresult1.getHitVec());
				return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
			}
			return null;
		}, (context) -> {
			Vector3d vec3d = context.getStartVec().subtract(context.getEndVec()); //getStartVec, getEndVec
			return BlockRayTraceResult.createMiss(context.getEndVec(), Direction.getFacingFromVector(vec3d.x, vec3d.y, vec3d.z), new BlockPos(context.getEndVec()));
		});
		if (checkEntityCollision) {
			if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
				dest = raytraceresult.getHitVec();
			}

			AxisAlignedBB aabb = new AxisAlignedBB(origin, dest).grow(1F);
			RayTraceResult raytraceresult1 = ProjectileHelper.rayTraceEntities(world, exception, origin, dest, aabb, filter);
			if (raytraceresult1 != null) {
				raytraceresult = raytraceresult1;
			}
		}

		return raytraceresult;
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
