package com.github.lorenzopapi.pgr.portal.gun;

import com.github.lorenzopapi.pgr.handler.PGRConfig;
import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.portal.structure.PortalStructure;
import com.github.lorenzopapi.pgr.util.EntityUtils;
import com.github.lorenzopapi.pgr.util.PGRUtils;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortalProjectileEntity extends Entity {

	private static final DataParameter<Integer> COLOR = EntityDataManager.createKey(PortalProjectileEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> SHOOTER = EntityDataManager.createKey(PortalProjectileEntity.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> MAX_DISTANCE = EntityDataManager.createKey(PortalProjectileEntity.class, DataSerializers.VARINT);
	private static final DataParameter<BlockPos> SPAWN_POS = EntityDataManager.createKey(PortalProjectileEntity.class, DataSerializers.BLOCK_POS);
	private static final DataParameter<Rotations> VELOCITY = EntityDataManager.createKey(PortalProjectileEntity.class, DataSerializers.ROTATIONS);
	public int age;
	public int pWidth;
	public int pHeight;
	public PortalStructure structure;
	public LivingEntity shooter;

	public PortalProjectileEntity(EntityType<PortalProjectileEntity> t, World worldIn) {
		super(t, worldIn);
		this.pWidth = 1;
		this.pHeight = 2;
		this.structure = null;
	}

	public PortalProjectileEntity(World world) {
		this(PGRRegistry.PPE_TYPE, world);
	}

	public PortalProjectileEntity(World worldIn, Entity shooter, PortalStructure info) {
		this(worldIn);
		if (shooter instanceof LivingEntity) {
			this.getDataManager().set(SHOOTER, shooter.getEntityId());
			this.shooter = (LivingEntity) shooter;
		}
		this.pWidth = info.width;
		this.pHeight = info.height;
		this.structure = info;
		setColor(this.structure.portalColor);
		getDataManager().set(MAX_DISTANCE, PGRConfig.COMMON.maxShootDistance.get());
		Vector3d location = shooter.getEyePosition(1.0F);
		Vector3d look = shooter.getLook(1.0F).normalize();
		setLocationAndAngles(location.x, location.y, location.z, shooter.rotationYaw, 0.0F);
		double velocity = 4.98D + Math.random() * 0.02D;
		setMotion(look.x * velocity, look.y * velocity, look.z * velocity);
		getDataManager().set(SPAWN_POS, new BlockPos(this.getPosition()));
		getDataManager().set(VELOCITY, new Rotations((float) this.getMotion().x, (float) this.getMotion().y, (float) this.getMotion().z));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getBoundingBox().getAverageEdgeLength() * 10.0D;
		if (Double.isNaN(d0)) {
			d0 = 1.0D;
		}
		d0 = d0 * 64.0D * getRenderDistanceWeight();
		return distance < d0 * d0;
	}

	@Override
	protected void registerData() {
		this.getDataManager().register(COLOR, 16777215);
		this.getDataManager().register(SHOOTER, -1);
		this.getDataManager().register(MAX_DISTANCE, 10000);
		this.getDataManager().register(SPAWN_POS, BlockPos.ZERO);
		this.getDataManager().register(VELOCITY, new Rotations(0, 0, 0));
	}

	public int getColor() {
		return this.getDataManager().get(COLOR);
	}

	public void setColor(int i) {
		this.getDataManager().set(COLOR, i);
	}

	public BlockPos getSpawnPos() {
		return this.getDataManager().get(SPAWN_POS);
	}

	public Rotations getVelocity() {
		return this.getDataManager().get(VELOCITY);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.age > PGRConfig.COMMON.maxShootDistance.get() / 5.0 + 10.0 || getDistanceSq(getSpawnPos().getX(), getSpawnPos().getY(), getSpawnPos().getZ()) > Math.pow(PGRConfig.COMMON.maxShootDistance.get() + 5, 2)) {
			this.setDead();
			return;
		}
		if (this.world.isRemote) {
			if (this.age == 0) {
				this.setLocationAndAngles(getSpawnPos().getX(), getSpawnPos().getY(), getSpawnPos().getZ(), this.rotationYaw, this.rotationPitch);
				this.setMotion(getVelocity().getX(), getVelocity().getY(), getVelocity().getZ());
			}
			if (this.shooter == null && getDataManager().get(SHOOTER) >= 0) {
				Entity ent = this.world.getEntityByID(getDataManager().get(SHOOTER));
				if (ent instanceof LivingEntity) {
					this.shooter = (LivingEntity) ent;
				} else {
					this.getDataManager().set(SHOOTER, -1);
				}
			}
		}
		RayTraceResult rtr = EntityUtils.rayTrace(this.world, this.getPositionVec(), this.getPositionVec().add(this.getMotion()), this, RayTraceContext.BlockMode.COLLIDER, PGRConfig.COMMON.canFireThroughLiquid.get() ? RayTraceContext.FluidMode.NONE : RayTraceContext.FluidMode.ANY, entity -> true);
		if (rtr.getType() == RayTraceResult.Type.BLOCK) {
			BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
			BlockPos pos = brtr.getPos();
			BlockState state = this.world.getBlockState(pos);

			if (state.getMaterial() != Material.AIR)
				state.onEntityCollision(this.world, pos, this);

			if (PGRConfig.COMMON.canFireThroughGlass.get() && state.getMaterial() == Material.GLASS) {
				this.setPosition(brtr.getHitVec().x - this.getMotion().x * 0.98D, brtr.getHitVec().y - this.getMotion().y * 0.98D, brtr.getHitVec().z - this.getMotion().z * 0.98D);
			} else {
				if (!this.world.isRemote) {
					Vector3d lookDir = Vector3d.copy(Direction.fromAngle(this.rotationYaw).getDirectionVec());
					if (PGRUtils.spawnPortal(this.world, pos, brtr.getFace(), Direction.getFacingFromVector(lookDir.x, 0, lookDir.z), this.structure, this.pWidth, this.pHeight)) {
						if (this.shooter != null) {
							ItemStack is = this.shooter.getHeldItemMainhand();
							if (is.getTag() != null) {
								is.getTag().putBoolean("lastFired", this.structure.isTypeA);
								if (this.shooter instanceof PlayerEntity) {
									((PlayerEntity) this.shooter).inventory.markDirty();
								} else {
									this.shooter.setItemStackToSlot(EquipmentSlotType.MAINHAND, is);
								}
							}
							this.shooter.getEntityWorld().playSound(null, shooter.getPosition(), this.structure.isTypeA ? PGRRegistry.PGRSounds.PORTAL_OPEN_BLUE : PGRRegistry.PGRSounds.PORTAL_OPEN_RED, this.shooter.getSoundCategory(), 0.2F, 1.0F + (this.shooter.getRNG().nextFloat() - this.shooter.getRNG().nextFloat()) * 0.1F);
						}
					} else {
						if (this.shooter != null) {
							this.shooter.getEntityWorld().playSound(null, shooter.getPosition(), PGRRegistry.PGRSounds.PORTAL_GUN_INVALID_SURFACE, this.shooter.getSoundCategory(), 0.2F, 1.0F + (this.shooter.getRNG().nextFloat() - this.shooter.getRNG().nextFloat()) * 0.1F);
						}
						this.playSound(PGRRegistry.PGRSounds.PORTAL_INVALID_SURFACE, 0.4F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
					}
				}
				this.setDead();
				return;
			}
		}
		Vector3d newPos = this.getPositionVec().add(this.getMotion());
		this.setPosition(newPos.x, newPos.y, newPos.z);
		if (this.isInWater())
			for (int i = 0; i < 4; i++) {
				float scale = 0.25F;
				this.world.addParticle(ParticleTypes.BUBBLE, this.getPosX() - this.getMotion().x * scale, this.getPosY() - this.getMotion().y * scale, this.getPosZ() - this.getMotion().z * scale, this.getMotion().x, this.getMotion().y, this.getMotion().z);
			}
		this.recenterBoundingBox();
		this.doBlockCollisions();
		this.age++;
	}

	@Override
	protected void setDead() {
		Reference.LOGGER.info("DED");
		super.setDead();
	}

	@Override
	protected void readAdditional(CompoundNBT tag) {
		setColor(tag.getInt("color"));
		this.pWidth = tag.getInt("portalWidth");
		this.pHeight = tag.getInt("portalHeight");
		this.structure = new PortalStructure().setWorld(world).setWidthAndHeight(pWidth, pHeight).readFromNBT(tag.getCompound("portalStructure"));
	}

	@Override
	protected void writeAdditional(CompoundNBT tag) {
		tag.putInt("color", getColor());
		tag.putInt("portalWidth", this.pWidth);
		tag.putInt("portalHeight", this.pHeight);
		tag.put("portalStructure", this.structure.writeToNBT(new CompoundNBT()));
	}

	@Override
	public boolean canRenderOnFire() {
		return false;
	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return new SSpawnObjectPacket(this);
	}
}
