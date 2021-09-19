package com.github.lorenzopapi.pgr.portal.customizer;

import com.github.lorenzopapi.pgr.handler.PGRRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public class PGCContainer extends Container {
	private final IWorldPosCallable worldPosCallable;
	public final IInventory input = new Inventory(1) {
		public void markDirty() {
			super.markDirty();
			PGCContainer.this.onCraftMatrixChanged(this);
		}
	};
	public PGCContainer(int id, PlayerInventory inventory) {
		this(id, inventory, IWorldPosCallable.DUMMY);
	}

	public PGCContainer(int id, PlayerInventory playerInventory, IWorldPosCallable callable) {
		super(PGRRegistry.PG_CUSTOMIZER_CONTAINER.get(), id);
		this.worldPosCallable = callable;
		this.addSlot(new Slot(input, 0, 7, 62) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.hasTag() && stack.getItem() == PGRRegistry.PORTAL_GUN.get();
			}
		});
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 7 + column * 18, 96 + row * 18));
			}
		}

		for (int column = 0; column < 9; ++column) {
			this.addSlot(new Slot(playerInventory, column, 7 + column * 18, 154));
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return isWithinUsableDistance(worldPosCallable, playerIn, PGRRegistry.PG_CUSTOMIZER);
	}

	@Override
	public ContainerType<?> getType() {
		return PGRRegistry.PG_CUSTOMIZER_CONTAINER.get();
	}

	/**
	* ALL THE NEXT METHODS WERE HIGHLY IF NOT COMPLETELY COPIED FROM STONE CUTTER
	* */

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack is = slot.getStack();
			copy = is.copy();
			if (index == 0) {
				if (!this.mergeItemStack(is, 1, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else if (is.getItem() == PGRRegistry.PORTAL_GUN.get() && is.hasTag()) {
				if (!this.mergeItemStack(is, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 29) {
				if (!this.mergeItemStack(is, 28, 37, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < 38 && !this.mergeItemStack(is, 1, 28, false)) {
				return ItemStack.EMPTY;
			}

			if (is.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			}

			slot.onSlotChanged();
			if (is.getCount() == copy.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, is);
			this.detectAndSendChanges();
		}

		return copy;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		this.worldPosCallable.consume((world, pos) -> this.clearContainer(playerIn, playerIn.world, this.input));
	}
}
