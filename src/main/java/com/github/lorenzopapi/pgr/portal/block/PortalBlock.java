package com.github.lorenzopapi.pgr.portal.block;

import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PortalBlock extends Block {

	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<PortalStructure.UpDirection> UP_FACING = EnumProperty.create("up_facing", PortalStructure.UpDirection.class);

	public PortalBlock() {
		super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F, 10000.0F).setLightLevel(value -> 10));
		this.setDefaultState(this.getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH).with(UP_FACING, PortalStructure.UpDirection.WALL));
	}

	//FOR DEBUG ONLY, REMOVE ON RELEASE
	// wait no, I need this
	// I can use this to test if the player is selecting a portal, then if they are, raytrace the blocks on the other side
	// and also, ontop of that, it allows me to easily get part of a render shape going
	// huh ok, won't remove then
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(UP_FACING)) {
			case WALL:
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
			case UP:
				return Block.makeCuboidShape(0, 0, 0, 16, 1, 16);
			case DOWN:
				return Block.makeCuboidShape(0, 15, 0, 16, 16, 16);
		}
		return VoxelShapes.empty();
	}

	@Override
	public void neighborChanged(BlockState current, World worldIn, BlockPos currentPos, Block changed, BlockPos changedPos, boolean isMoving) {
		if (current.get(UP_FACING) == PortalStructure.UpDirection.WALL) {
			if (changedPos.equals(currentPos.offset(current.get(HORIZONTAL_FACING).getOpposite()))) {
				if (!PGRUtils.isDirectionSolid(worldIn, changedPos, current.get(HORIZONTAL_FACING))) {
					worldIn.removeBlock(currentPos, isMoving);
				}
			}
		} else {
			if (changedPos.equals(currentPos.offset(current.get(UP_FACING).toDirection().getOpposite()))) {
				if (!PGRUtils.isDirectionSolid(worldIn, changedPos, current.get(UP_FACING).toDirection())) {
					worldIn.removeBlock(currentPos, isMoving);
				}
			}
		}
	}

	@Override
	public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, UP_FACING);
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
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
	}

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
		worldIn.removeTileEntity(pos);
		PortalStructure struct = PGRUtils.findPortalByPosition(worldIn, pos);
		if (struct != null)
			Reference.serverEH.getPGRDataForDimension(worldIn.getDimensionKey()).removePortal(struct);
		super.onReplaced(state, worldIn, pos, newState, isMoving);
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
	}
}
