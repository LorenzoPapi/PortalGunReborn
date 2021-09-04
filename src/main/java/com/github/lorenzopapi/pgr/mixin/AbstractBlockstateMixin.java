package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.portal.PortalStructure;
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
public class AbstractBlockstateMixin {
	@Inject(at = @At("HEAD"), method = "isSuffocating", cancellable = true)
	public void preCheckSuffocating(IBlockReader blockReaderIn, BlockPos blockPosIn, CallbackInfoReturnable<Boolean> cir) {
		if (blockReaderIn instanceof World) {
			List<PortalStructure> structureList = PGRUtils.findPortalsInAABB(
					(World)blockReaderIn, new AxisAlignedBB(blockPosIn.add(-1, -1, -1), blockPosIn.add(2, 2, 2))
			);
			for (PortalStructure structure : structureList) {
				if (structure.behinds.contains(blockPosIn)) cir.setReturnValue(false);
			}
		}
	}
}
