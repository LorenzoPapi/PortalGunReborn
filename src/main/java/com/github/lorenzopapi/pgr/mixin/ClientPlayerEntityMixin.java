package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.portalgun.PortalBlock;
import com.github.lorenzopapi.pgr.portalgun.UpDirection;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

	@Shadow
	@Final
	protected Minecraft mc;

	final BlockPos.Mutable oldPos = new BlockPos.Mutable();
	List<BlockPos> oldBehinds = new ArrayList<>();

	@Inject(method = "shouldBlockPushPlayer", at = @At("HEAD"), cancellable = true)
	public void teleportTrough(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockState current = mc.world.getBlockState(pos);
		PortalStructure structure = PGRUtils.findPortalByPosition(mc.world, pos);
		if (current.getBlock() == PGRRegistry.PORTAL_BLOCK && structure != null) {
			oldPos.setPos(pos);
			oldBehinds = structure.behinds;
			cir.setReturnValue(false);
		} else {
			if (oldBehinds.contains(pos)) {
				PortalStructure struct = PGRUtils.findPortalByPosition(mc.world, oldPos.toImmutable());
				if (struct != null && struct.hasPair()) {
					BlockPos pairPos = struct.pair.positions.get(struct.positions.indexOf(oldPos));
					UpDirection upDir = mc.world.getBlockState(pairPos).get(PortalBlock.UP_FACING);
					double x = pairPos.getX() + getDecimals(mc.player.getPosX());
					double y = pairPos.getY() + getDecimals(mc.player.getPosY()) + (upDir != UpDirection.WALL ? upDir.toDirection().getYOffset() - mc.player.getHeight() : 0);
					double z = pairPos.getZ() + getDecimals(mc.player.getPosZ());
					mc.player.setLocationAndAngles(x, y, z, mc.player.rotationYaw, mc.player.rotationPitch);
					cir.setReturnValue(false);
				}
			}
		}
	}

	private double getDecimals(double d) {
		String s = String.valueOf(d);
		return Double.parseDouble(s.substring(s.indexOf(".")));
	}
}
