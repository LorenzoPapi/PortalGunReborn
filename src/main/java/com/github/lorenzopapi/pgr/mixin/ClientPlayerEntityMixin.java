package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portalgun.PortalBlock;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

	@Shadow
	@Final
	protected Minecraft mc;

	BlockPos.Mutable oldPos = new BlockPos.Mutable();
	Direction oldDir;

	@Inject(method = "shouldBlockPushPlayer", at = @At("HEAD"), cancellable = true)
	public void teleportTrough(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		// might add map of entity <-> position of portal is in
		if (mc.world.getBlockState(pos).getBlock() == PGRRegistry.PORTAL_BLOCK) {
			oldPos.setPos(pos);
			oldDir = mc.world.getBlockState(pos).get(PortalBlock.HORIZONTAL_FACING).getOpposite();
			cir.setReturnValue(false);
		} else {
			if (Direction.fromAngle(mc.player.rotationYaw) == oldDir && Reference.serverEH.getWorldSaveData(mc.world.getDimensionKey()).behinds.contains(pos)) {
				PortalStructure struct = PGRUtils.findPortalByPosition(mc.world, oldPos.toImmutable());
				if (struct != null && struct.pair != null) {
					BlockPos pairPos = struct.pair.positions.get(0);
					Direction pairDir = mc.world.getBlockState(pairPos).get(PortalBlock.HORIZONTAL_FACING);
					double x = pairPos.getX() + 0.5;
					double z = pairPos.getZ() + 0.5;
					mc.player.setPositionAndRotation(x, pairPos.getY(), z, pairDir.getHorizontalAngle(), mc.player.rotationPitch);
					mc.player.setMotion(mc.player.getMotion());
					cir.setReturnValue(false);
				}
			}
		}
	}
}
