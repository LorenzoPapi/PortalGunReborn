package com.github.lorenzopapi.pgr.portalgun;

import com.github.lorenzopapi.pgr.handler.PGRConfig;
import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import com.github.lorenzopapi.pgr.handler.PGRSounds;
import com.github.lorenzopapi.pgr.portal.PortalStructure;
import com.github.lorenzopapi.pgr.util.EntityHelper;
import com.github.lorenzopapi.pgr.util.PortalGunHelper;
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
import net.minecraft.util.SoundCategory;
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
	
	public PortalProjectileEntity(World worldIn) {
		this(PGRRegistry.PPE_TYPE, worldIn);
	}

	public PortalProjectileEntity(EntityType<PortalProjectileEntity> t, World worldIn) {
		super(t, worldIn);
		this.pWidth = 1;
		this.pHeight = 2;
		this.structure = null;
	}

	public PortalProjectileEntity(World worldIn, Entity shooter, PortalStructure info) {
		this(worldIn);
		if (shooter instanceof LivingEntity) {
			this.getDataManager().set(SHOOTER, shooter.getEntityId());
			this.shooter = (LivingEntity)shooter;
		}
		this.pWidth = info.width;
		this.pHeight = info.height;
		this.structure = info;
		setColor(this.structure.color);
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

	public void setColor(int i) {
		this.getDataManager().set(COLOR, i);
	}

	public int getColor() {
		return this.getDataManager().get(COLOR);
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
					getDataManager().set(SHOOTER, -1);
				}
			}
		}
		Vector3d start = this.getPositionVec();
		Vector3d end = this.getPositionVec().add(this.getMotion());
		//TODO: check on glass and liquids
		RayTraceResult rtr = EntityHelper.rayTrace(this.world, start, end, this, false, RayTraceContext.BlockMode.COLLIDER, blockInfo -> blockInfo.state.getMaterial() != Material.GLASS, RayTraceContext.FluidMode.NONE, entity -> true);
		//RayTraceResult rtr = EntityHelper.rayTrace(this.world, start, end, this, false, RayTraceContext.BlockMode.COLLIDER, blockInfo -> PGRConfig.COMMON.canFireThroughGlass.get() || blockInfo.state.getMaterial() != Material.GLASS, PGRConfig.COMMON.canFireThroughLiquid.get() ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, entity -> true);
		if (rtr != null && rtr.getType() == RayTraceResult.Type.BLOCK) {
			BlockRayTraceResult brtr = (BlockRayTraceResult) rtr;
			BlockPos pos = brtr.getPos();
			BlockState state = this.world.getBlockState(pos);

			if (state.getMaterial() != Material.AIR)
				state.onEntityCollision(this.world, pos, this);

			if (state.getMaterial() == Material.GLASS) {
				setPosition(brtr.getHitVec().x - this.getMotion().x * 0.98D, brtr.getHitVec().y - this.getMotion().y * 0.98D, brtr.getHitVec().z - this.getMotion().z * 0.98D);
			} else {
				if (!this.world.isRemote) {
					Reference.LOGGER.info("Encountered block!");
					Vector3d lookDir = Vector3d.copy(Direction.fromAngle(this.rotationYaw).getDirectionVec());
					Direction look = Direction.getFacingFromVector(lookDir.x, 0, lookDir.z);
					if (PortalGunHelper.spawnPortal(this.world, pos, brtr.getFace(), look, this.structure, this.pWidth, this.pHeight)) {
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
							EntityHelper.playSoundAtEntity(this.shooter, this.structure.isTypeA ? PGRSounds.p_portal_open_blue : PGRSounds.p_portal_open_red, this.shooter.getSoundCategory(), 0.2F, 1.0F + (this.shooter.getRNG().nextFloat() - this.shooter.getRNG().nextFloat()) * 0.1F);
						}
					} else {
						if (this.shooter != null) {
							EntityHelper.playSoundAtEntity(this.shooter, PGRSounds.pg_portal_invalid_surface_swt, this.shooter.getSoundCategory(), 0.2F, 1.0F + (this.shooter.getRNG().nextFloat() - this.shooter.getRNG().nextFloat()) * 0.1F);
						}
						EntityHelper.playSoundAtEntity(this, PGRSounds.p_portal_invalid_surface, SoundCategory.BLOCKS, 0.4F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
					}
				}
				setDead();
				return;
			}
		}
		Vector3d newPos = this.getPositionVec().add(this.getMotion());
		this.setPosition(newPos.x, newPos.y, newPos.z);
		if (isInWater())
			for (int i1 = 0; i1 < 4; i1++) {
				float f8 = 0.25F;
				this.world.addParticle(ParticleTypes.BUBBLE, this.getPosX() - this.getMotion().x * f8, this.getPosY() - this.getMotion().y * f8, this.getPosZ() - this.getMotion().z * f8, this.getMotion().x, this.getMotion().y, this.getMotion().z);
			}
		this.recenterBoundingBox();
		this.doBlockCollisions();
		this.age++;
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
