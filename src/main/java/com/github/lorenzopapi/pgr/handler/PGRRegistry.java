package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.portal.block.PortalBlock;
import com.github.lorenzopapi.pgr.portal.block.PortalBlockTileEntity;
import com.github.lorenzopapi.pgr.portal.customizer.PGCustomizerBlock;
import com.github.lorenzopapi.pgr.portal.customizer.PGCustomizerContainer;
import com.github.lorenzopapi.pgr.portal.gun.PGItem;
import com.github.lorenzopapi.pgr.portal.gun.PortalProjectileEntity;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PGRRegistry {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MOD_ID);
	private static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Reference.MOD_ID);
	private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Reference.MOD_ID);
	public static final RegistryObject<Item> PORTAL_GUN = ITEMS.register("portal_gun", PGItem::new);
	public static final PortalBlock PORTAL_BLOCK = new PortalBlock();
	public static final PGCustomizerBlock PG_CUSTOMIZER = new PGCustomizerBlock();
	public static final EntityType<PortalProjectileEntity> PPE_TYPE = EntityType.Builder.<PortalProjectileEntity>create(PortalProjectileEntity::new, EntityClassification.MISC).size(0.3f, 0.3f).build("portal_projectile");
	public static final RegistryObject<TileEntityType<PortalBlockTileEntity>> PORTAL_TILE_ENTITY = TILE_ENTITIES.register("portal_tile_entity", () -> TileEntityType.Builder.create(PortalBlockTileEntity::new, PORTAL_BLOCK).build(null));
	public static final RegistryObject<ContainerType<PGCustomizerContainer>> PG_CUSTOMIZER_CONTAINER = CONTAINERS.register("pg_customizer_container", () -> new ContainerType<>(PGCustomizerContainer::new));

	public static void register(IEventBus modBus) {
		ITEMS.register(modBus);
		TILE_ENTITIES.register(modBus);
		CONTAINERS.register(modBus);
	}

	@SubscribeEvent
	public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
		registerItem(e, new Item(new Item.Properties().group(ItemGroup.MATERIALS)), "ender_pearl_dust");
		registerItem(e, new Item(new Item.Properties().maxStackSize(1).group(ItemGroup.MATERIALS)) {
			@Override
			public boolean hasEffect(ItemStack stack) {
				return true;
			}
		}, "miniature_black_hole");
	}

	@SubscribeEvent
	public static void onBlockRegistry(final RegistryEvent.Register<Block> e) {
		e.getRegistry().register(PORTAL_BLOCK.setRegistryName(new ResourceLocation(Reference.MOD_ID, "portal_block")));
		e.getRegistry().register(PG_CUSTOMIZER.setRegistryName(new ResourceLocation(Reference.MOD_ID, "pg_customizer")));
	}

	@SubscribeEvent
	public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> e) {
		e.getRegistry().register(PPE_TYPE.setRegistryName(new ResourceLocation(Reference.MOD_ID, "portal_projectile")));
	}

	private static void registerItem(RegistryEvent.Register<Item> e, Item item, String name) {
		e.getRegistry().register(item.setRegistryName(new ResourceLocation(Reference.MOD_ID, name)));
	}

	@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class PGRSounds {
//		public static SoundEvent pg_object_use_failure;
//		public static SoundEvent pg_object_use_lp_loop;
//		public static SoundEvent pg_object_use_lp_start;
//		public static SoundEvent pg_object_use_stop;
		public static SoundEvent PORTAL_AMBIENCE;
		public static SoundEvent PORTAL_ENTER;
		public static SoundEvent PORTAL_EXIT;
		public static SoundEvent PORTAL_INVALID_SURFACE;
		public static SoundEvent PORTAL_OPEN_BLUE;
		public static SoundEvent PORTAL_OPEN_RED;
		public static SoundEvent PORTAL_CLOSE;
		public static SoundEvent PORTAL_GUN_INVALID_SURFACE;
		public static SoundEvent PORTAL_GUN_RESET_PORTALS;
		public static SoundEvent PORTAL_GUN_FIRE_BLUE;
		public static SoundEvent PORTAL_GUN_FIRE_RED;
		public static SoundEvent PORTAL_GUN_EQUIP;

		@SubscribeEvent
		public static void onSoundRegistry(RegistryEvent.Register<SoundEvent> e) {
//			pg_object_use_failure = register(e, "portalgun.object_use_failure");
//			pg_object_use_lp_loop = register(e, "portalgun.object_use_lp_loop");
//			pg_object_use_lp_start = register(e, "portalgun.object_use_lp_start");
//			pg_object_use_stop = register(e, "portalgun.object_use_stop");
			PORTAL_ENTER = register(e, "portal.enter");
			PORTAL_EXIT = register(e, "portal.exit");
			PORTAL_INVALID_SURFACE = register(e, "portal.invalid_surface");
			PORTAL_OPEN_BLUE = register(e, "portal.open_blue");
			PORTAL_OPEN_RED = register(e, "portal.open_red");
			PORTAL_CLOSE = register(e, "portal.close");
			PORTAL_AMBIENCE = register(e, "portal.ambience");
			PORTAL_GUN_INVALID_SURFACE = register(e, "pg.invalid_surface");
			PORTAL_GUN_RESET_PORTALS = register(e, "pg.reset");
			PORTAL_GUN_FIRE_BLUE = register(e, "pg.fire_blue");
			PORTAL_GUN_FIRE_RED = register(e, "pg.fire_red");
			PORTAL_GUN_EQUIP = register(e, "pg.equip");
		}

		private static SoundEvent register(RegistryEvent.Register<SoundEvent> e, String name) {
			ResourceLocation rl = new ResourceLocation(Reference.MOD_ID, name);
			SoundEvent event = new SoundEvent(rl).setRegistryName(rl);
			e.getRegistry().register(event);
			return event;
		}

	}
}
