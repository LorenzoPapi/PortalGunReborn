package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
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
		PortalStructure possible = PGRUtils.findPortalByPosition(mc.world, pos);
		if (mc.world.getBlockState(pos).getBlock() == PGRRegistry.PORTAL_BLOCK && possible != null) {
			oldPos.setPos(pos);
			oldBehinds = possible.behinds;
			cir.setReturnValue(false);
		} else {
			if (oldBehinds.contains(pos)) {
				PortalStructure current = PGRUtils.findPortalByPosition(mc.world, oldPos.toImmutable());
				if (current != null && current.hasPair()) {
					Vector3d pairPos = averagePosOfList(current.pair.positions);
					double x = pairPos.getX() + getDecimals(mc.player.getPosX());
					double y = pairPos.getY() + getDecimals(mc.player.getPosY()) + (current.pair.upDirection != PortalStructure.UpDirection.WALL ? current.pair.upDirection.toDirection().getYOffset() - mc.player.getHeight() : 0);
					double z = pairPos.getZ() + getDecimals(mc.player.getPosZ());
					float yaw = current.pair.direction.getHorizontalAngle();
					Reference.LOGGER.info("Current: {}, Portal: {}, Pair: {}", mc.player.rotationYaw, current.direction.getHorizontalAngle(), yaw);
					mc.player.setLocationAndAngles(x, y, z, yaw % 360, mc.player.rotationPitch);
					mc.player.playSound(PGRRegistry.PGRSounds.PORTAL_ENTER, mc.player.getSoundCategory(), 0.01F, 1.0F + mc.player.getEntityWorld().rand.nextFloat() * 0.1F);
					mc.player.playSound(PGRRegistry.PGRSounds.PORTAL_EXIT, mc.player.getSoundCategory(), 0.01F, 1.0F + mc.player.getEntityWorld().rand.nextFloat() * 0.1F);
					cir.setReturnValue(false);
				}
			}
		}
	}
	
	private Vector3d averagePosOfList(List<BlockPos> poses) {
		double ax = 0, az = 0;
		for (BlockPos p : poses) {
			ax += p.getX();
			az += p.getZ();
		}
		ax /= poses.size();
		az /= poses.size();
		return new Vector3d(ax, poses.get(0).getY(), az);
	}

	private double getDecimals(double d) {
		String s = String.valueOf(d);
		return Double.parseDouble(s.substring(s.indexOf(".")));
	}
}
