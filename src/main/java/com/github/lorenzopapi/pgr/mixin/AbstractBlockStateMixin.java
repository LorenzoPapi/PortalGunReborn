package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
	@Inject(at = @At("HEAD"), method = "isSuffocating", cancellable = true)
	public void preCheckSuffocating(IBlockReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (world instanceof World) {
			List<PortalStructure> structureList = PGRUtils.findPortalsInAABB(
					(World)world, new AxisAlignedBB(pos.add(-1, -1, -1), pos.add(2, 2, 2))
			);
			for (PortalStructure structure : structureList) {
				if (structure.behinds.contains(pos)) cir.setReturnValue(false);
			}
			if (world.getBlockState(pos).getBlock() == PGRRegistry.PORTAL_BLOCK)
				cir.setReturnValue(false);
		}
	}
}
