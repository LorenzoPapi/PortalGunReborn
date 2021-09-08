package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.EntityUtils;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
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

	//TODO: ok so everything here is for WALL PORTALS, but it should be relatively simple to bring it to FLOOR/CEILING PORTALS
	//future me remember this
	//just do checks about updirection and it'll be fine
	@Inject(method = "shouldBlockPushPlayer", at = @At("HEAD"), cancellable = true)
	public void teleportTrough(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (mc.world.getBlockState(pos).getBlock() == PGRRegistry.PORTAL_BLOCK ) {
			PortalStructure possible = PGRUtils.findPortalByPosition(mc.world, pos);
			if (possible != null && !oldPos.toImmutable().equals(pos)) {
				oldPos.setPos(pos);
				oldBehinds = possible.behinds;
			}
			cir.setReturnValue(false);
		} else if (oldBehinds.contains(pos)) {
			PortalStructure current = PGRUtils.findPortalByPosition(mc.world, oldPos.toImmutable());
			if (current != null && current.hasPair()) {
				Vector3d pairPos = EntityUtils.averagePairPosition(current.pair.positions, mc.player.getPositionVec());
				double x = pairPos.getX() + current.pair.direction.getXOffset() * 0.01;
				double y = pairPos.getY() + (current.pair.upDirection != PortalStructure.UpDirection.WALL ? current.pair.upDirection.toDirection().getYOffset() - mc.player.getHeight() : 0);
				double z = pairPos.getZ() + current.pair.direction.getZOffset() * 0.01;
				if (current.direction.getXOffset() == 1) x += 0.5;
				else if (current.direction.getZOffset() == 1) z += 0.5;
				float yaw = (current.direction.getOpposite() == current.pair.direction) ? 0 : (current.direction == current.pair.direction) ? 180 : current.direction.getHorizontalAngle() - current.pair.direction.getHorizontalAngle();
				mc.player.setPositionAndRotation(x, y, z, yaw + mc.player.rotationYaw, mc.player.rotationPitch);
				mc.player.playSound(PGRRegistry.PGRSounds.PORTAL_ENTER, SoundCategory.PLAYERS, 0.01F, 1.0F + mc.player.getEntityWorld().rand.nextFloat() * 0.1F);
				mc.player.playSound(PGRRegistry.PGRSounds.PORTAL_EXIT, SoundCategory.PLAYERS, 0.01F, 1.0F + mc.player.getEntityWorld().rand.nextFloat() * 0.1F);
				cir.setReturnValue(false);
			}
		}
	}
}
