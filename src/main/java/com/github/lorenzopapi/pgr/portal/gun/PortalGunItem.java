package com.github.lorenzopapi.pgr.portal.gun;

import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class PortalGunItem extends Item {
	public PortalGunItem() {
		super(new Properties().maxStackSize(1).maxDamage(0).group(ItemGroup.TOOLS));
	}

	public static void setRandomNBTTags(ItemStack is, PlayerEntity player) {
		CompoundNBT tag = new CompoundNBT();
		tag.putString("uuid", UUIDTypeAdapter.fromUUID(player.getGameProfile().getId()));
		tag.putString("username", player.getName().getString());
		tag.putString("channelName", "Random Channel #" + is.hashCode());
		tag.putInt("width", 1);
		tag.putInt("height", 2);
		tag.putInt("grabStrength", 4);
		tag.putBoolean("lastFired", true);
		is.setTag(tag);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, PlayerEntity player) {
		return true;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		return true;
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		return ActionResultType.SUCCESS;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (isInGroup(group)) {
			ItemStack stack = new ItemStack(this, 1);
			CompoundNBT stackTag = new CompoundNBT();
			stackTag.putString("uuid", "Global");
			stackTag.putString("username", "Global");
			stackTag.putString("channelName", "Chell");
			stackTag.putInt("width", 1);
			stackTag.putInt("height", 2);
			stackTag.putInt("grabStrength", 4);
			stackTag.putBoolean("lastFired", true);
			stack.setTag(stackTag);
			items.add(stack);
			ItemStack stack1 = new ItemStack(this, 1);
			CompoundNBT stackTag1 = new CompoundNBT();
			stackTag1.putString("uuid", "Global");
			stackTag1.putString("username", "Global");
			stackTag1.putString("channelName", "Atlas");
			stackTag1.putInt("width", 1);
			stackTag1.putInt("height", 2);
			stackTag1.putInt("grabStrength", 4);
			stackTag1.putBoolean("lastFired", true);
			stack1.setTag(stackTag1);
			items.add(stack1);
			ItemStack stack2 = new ItemStack(this, 1);
			CompoundNBT stackTag2 = new CompoundNBT();
			stackTag2.putString("uuid", "Global");
			stackTag2.putString("username", "Global");
			stackTag2.putString("channelName", "P-body");
			stackTag2.putInt("width", 1);
			stackTag2.putInt("height", 2);
			stackTag2.putInt("grabStrength", 4);
			stackTag2.putBoolean("lastFired", true);
			stack2.setTag(stackTag2);
			items.add(stack2);
			ItemStack is = new ItemStack(this, 1);
			CompoundNBT tag = new CompoundNBT();
			tag.putString("uuid", Minecraft.getInstance().getSession().getPlayerID());
			tag.putString("username", Minecraft.getInstance().getSession().getUsername());
			tag.putString("channelName", "Creative Inventory");
			tag.putInt("width", 1);
			tag.putInt("height", 2);
			tag.putInt("grabStrength", 4);
			tag.putBoolean("lastFired", true);
			is.setTag(tag);
			items.add(is);
			ItemStack is1 = new ItemStack(this, 1);
			CompoundNBT tag1 = new CompoundNBT();
			tag1.putString("uuid", Minecraft.getInstance().getSession().getPlayerID());
			tag1.putString("username", Minecraft.getInstance().getSession().getUsername());
			tag1.putString("channelName", "Creative Inventory Type #2");
			tag1.putInt("width", 1);
			tag1.putInt("height", 2);
			tag1.putInt("grabStrength", 4);
			tag1.putBoolean("lastFired", true);
			is1.setTag(tag1);
			items.add(is1);
			ItemStack is2 = new ItemStack(this, 1);
			CompoundNBT tag2 = new CompoundNBT();
			tag2.putString("uuid", Minecraft.getInstance().getSession().getPlayerID());
			tag2.putString("username", Minecraft.getInstance().getSession().getUsername());
			tag2.putString("channelName", "Creative Inventory Type #3");
			tag2.putInt("width", 1);
			tag2.putInt("height", 2);
			tag2.putInt("grabStrength", 4);
			tag2.putBoolean("lastFired", true);
			is2.setTag(tag2);
			items.add(is2);
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		CompoundNBT tag = stack.getTag();
		if (tag != null) {
			String name = tag.getString("username");
			String channel = tag.getString("channelName");
			if (name.equals("Global")) {
				tooltip.add(new TranslationTextComponent("pgr.info.global"));
			} else if (!name.isEmpty()) {
				tooltip.add(new TranslationTextComponent("pgr.info.owner", name));
			}
			tooltip.add(new TranslationTextComponent("pgr.info.channel", channel));
			tooltip.add(new TranslationTextComponent("pgr.info.size", tag.getInt("width"), tag.getInt("height")));
			tooltip.add(new TranslationTextComponent("pgr.info.grabStrength" + tag.getInt("grabStrength")));
		} else {
			tooltip.add(new TranslationTextComponent("pgr.info.new"));
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged && (!oldStack.isItemEqual(newStack) || oldStack.getTag() == null || !oldStack.getTag().equals(newStack.getTag()));
	}
}
