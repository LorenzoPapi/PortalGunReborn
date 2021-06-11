package com.github.lorenzopapi.pgr.handler;

import com.github.lorenzopapi.pgr.block.PortalBlock;
import com.github.lorenzopapi.pgr.entity.PortalProjectileEntity;
import com.github.lorenzopapi.pgr.item.PortalGunItem;
import com.github.lorenzopapi.pgr.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PGRRegistry {
	public static final PortalGunItem PORTAL_GUN = new PortalGunItem();
	public static final PortalBlock PORTAL_BLOCK = new PortalBlock();
	public static final EntityType<PortalProjectileEntity> PPE_TYPE = EntityType.Builder.<PortalProjectileEntity>create(PortalProjectileEntity::new, EntityClassification.MISC).size(0.3f, 0.3f).build("portal_projectile");

	//	@SubscribeEvent
//	public void onModelBake(ModelBakeEvent event) {
//		//event.getModelRegistry().putObject(new ModelResourceLocation("portalgun:item_portalgun", "inventory"), (new ModelBaseWrapper((IModelBase)new ItemRenderPortalGun())).setItemDualHanded());
//	}

	@SubscribeEvent
	public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
		registerItem(e, new Item(new Item.Properties().group(ItemGroup.MATERIALS)), "ender_pearl_dust");
		registerItem(e, new Item(new Item.Properties().maxStackSize(1).group(ItemGroup.MATERIALS)) {
			@Override
			public boolean hasEffect(ItemStack stack) {
				return true;
			}
		}, "miniature_black_hole");
		registerItem(e, PORTAL_GUN, "portal_gun");
	}

	@SubscribeEvent
	public static void onBlockRegistry(final RegistryEvent.Register<Block> e) {
		registerBlock(e, PORTAL_BLOCK, "portal_block");
	}

	@SubscribeEvent
	public static void onEntityRegistry(final RegistryEvent.Register<EntityType<?>> e) {
		e.getRegistry().register(PPE_TYPE.setRegistryName(new ResourceLocation(Reference.MODID, "portal_projectile")));
	}

	@SubscribeEvent
	public static void onSoundRegistry(final RegistryEvent.Register<SoundEvent> e) {
		PGRSoundEvents.init(e);
	}

	private static void registerItem(RegistryEvent.Register<Item> e, Item item, String name) {
		e.getRegistry().register(item.setRegistryName(new ResourceLocation(Reference.MODID, name)));
	}

	private static void registerBlock(RegistryEvent.Register<Block> e, Block block, String name) {
		e.getRegistry().register(block.setRegistryName(new ResourceLocation(Reference.MODID, name)));
	}
}
