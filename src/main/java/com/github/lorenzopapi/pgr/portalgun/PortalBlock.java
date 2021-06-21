package com.github.lorenzopapi.pgr.portalgun;

import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PortalBlock extends Block {

	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

	public PortalBlock() {
		super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F, 10000.0F).setLightLevel(value -> 10));
		this.setDefaultState(this.getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(HORIZONTAL_FACING)) {
			case EAST:
				return Block.makeCuboidShape(0, 0, 0, 1, 16, 16);
			case WEST:
				return Block.makeCuboidShape(15, 0, 0, 16, 16, 16);
			case SOUTH:
				return Block.makeCuboidShape(0, 0, 0, 16, 16, 1);
			case NORTH:
				return Block.makeCuboidShape(0, 0, 15, 16, 16, 16);
		}
		return super.getShape(state, worldIn, pos, context);
	}

	@Override
	public void neighborChanged(BlockState current, World worldIn, BlockPos currentPos, Block changed, BlockPos changedPos, boolean isMoving) {
//		if (changedPos.equals(currentPos.offset(current.get(HORIZONTAL_FACING).getOpposite()))) {
//			if (!PGRUtils.isDirectionSolid(worldIn, changedPos, current.get(HORIZONTAL_FACING))) {
//				worldIn.removeBlock(currentPos, isMoving);
//			}
//		}
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity ent) {
		//		PortalStructure struct = Reference.serverEH.getWorldSaveData(world.getDimensionKey()).findPortalByPosition(pos);
////		if (struct != null && struct.pair != null) {
////			BlockPos pairPos = struct.pair.positions.get(struct.positions.indexOf(pos)).toImmutable();
////			BlockState pairState = world.getBlockState(pairPos);
////			Direction pairDir = pairState.get(HORIZONTAL_FACING);
////			double x = pairPos.getX() + pairDir.getXOffset();
////			double z = pairPos.getZ() + pairDir.getZOffset();
////			Reference.LOGGER.info("Entity: {}, {}, {}", ent.getPosX(), ent.getPosY(), ent.getPosZ());
////			Reference.LOGGER.info("Pair: {}, {}, {}", x, pairPos.getY(), z);
////			ent.setPositionAndRotation(x, pairPos.getY(), z, pairDir.getHorizontalAngle(), ent.rotationPitch);
////		}
//		if (struct == null) {
//			world.removeBlock(pos, false);
//		}
	}

	@Override
	public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Lists.newArrayList();
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {}

	@Override
	public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
		return false;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PortalBlockTileEntity();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		PortalStructure struct = Reference.serverEH.getWorldSaveData(worldIn.getDimensionKey()).findPortalByPosition(pos);
		if (struct != null) {
			Reference.serverEH.getWorldSaveData(worldIn.getDimensionKey()).removePortal(struct);
		}
	}
	
	// TODO: add a block state which indicates that it is the bottom center of the portal if it's a wall portal or dead center if it's a floor portal
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public boolean canDropFromExplosion(Explosion explosionIn) {
		return false;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return Items.DRAGON_EGG.getDefaultInstance();
		// LUL
		// yes.
	}
}
