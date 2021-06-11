package com.github.lorenzopapi.pgr.block;

import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.util.Reference;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class PortalBlock extends Block {
	public static final VoxelShape CUBE = makeCuboidShape(8, 8, 8, 8, 8, 8);

	public PortalBlock() {
		super(Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F, 10000.0F).setLightLevel(value -> 10));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return CUBE;
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
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		Reference.LOGGER.info("Replacing!");
		PortalStructure struct = Reference.pgrEventHandler.getWorldSaveData(worldIn.getDimensionKey()).findPortalByPosition(worldIn, pos);
		if (struct != null) {
			Reference.pgrEventHandler.getWorldSaveData(worldIn.getDimensionKey()).removePortal(struct);
		} else {
			worldIn.removeBlock(pos, isMoving);
		}
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
