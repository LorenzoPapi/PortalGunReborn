package com.github.lorenzopapi.pgr.mixin;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.EntityUtils;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public World world;

	@Shadow public abstract AxisAlignedBB getBoundingBox();

	@Shadow public abstract BlockPos getPosition();

	@Shadow public abstract Vector3d getPositionVec();

	@Shadow public float rotationYaw;

	@Shadow public float rotationPitch;

	@Shadow public abstract void setPositionAndRotation(double x, double y, double z, float yaw, float pitch);

	@Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);

	@Shadow public abstract float getHeight();

	@Inject(at = @At("HEAD"), method = "baseTick")
	public void handleTeleport(CallbackInfo ci) {
		for (PortalStructure portal : PGRUtils.findPortalsInAABB(world, getBoundingBox())) {
			if (portal.hasPair() && portal.behinds.contains(getPosition())) {
				Vector3d pairPos = EntityUtils.averagePairPosition(portal.pair.positions, getPositionVec());
				double x = pairPos.getX() + portal.pair.direction.getXOffset();
				double y = pairPos.getY() + (portal.pair.upDirection != PortalStructure.UpDirection.WALL ? portal.pair.upDirection.toDirection().getYOffset() - getHeight() : 0);
				double z = pairPos.getZ() + portal.pair.direction.getZOffset();
				float yaw = (portal.direction.getOpposite() == portal.pair.direction) ? 0 : (portal.direction == portal.pair.direction) ? 180 : portal.direction.getHorizontalAngle() - portal.pair.direction.getHorizontalAngle();
				setPositionAndRotation(x, y, z, yaw + rotationYaw, rotationPitch);
				playSound(PGRRegistry.PGRSounds.PORTAL_ENTER, 0.01F, 1.0F + world.rand.nextFloat() * 0.1F);
				playSound(PGRRegistry.PGRSounds.PORTAL_EXIT, 0.01F, 1.0F + world.rand.nextFloat() * 0.1F);
			}
		}
	}
}
