package com.github.lorenzopapi.pgr.portal.customizer;

import com.github.lorenzopapi.pgr.util.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PGCustomizerScreen extends ContainerScreen<PGCustomizerContainer> implements IContainerListener {
	private static final ResourceLocation BG = new ResourceLocation(Reference.MOD_ID, "textures/gui/customizer.png");
	private final StringTextComponent wComponent = new StringTextComponent("W:");
	//private final StringTextComponent hComponent = new StringTextComponent("H:");
	private TextFieldWidget widthField;
	//private TextFieldWidget heightField;

	public PGCustomizerScreen(PGCustomizerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.xSize = 175;
		this.ySize = 178;
		this.titleX = 6;
		this.playerInventoryTitleX = 6;
		this.playerInventoryTitleY = this.ySize - 94;
	}

	@Override
	public void tick() {
		super.tick();
		this.widthField.tick();
		//this.heightField.tick();
	}

	@Override
	protected void init() {
		super.init();
		int ww = this.guiLeft + this.font.getStringPropertyWidth(wComponent) + 6;
		//int wh = this.guiLeft + this.font.getStringPropertyWidth(hComponent) + 6;
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		this.widthField = new TextFieldWidget(this.font, ww + 2, this.guiTop + 30 - 2, 40, 12, StringTextComponent.EMPTY) {
			@Override
			public boolean charTyped(char codePoint, int modifiers) {
				return modifiers == 0 && Character.isDigit(codePoint) && super.charTyped(codePoint, modifiers);
			}
		};
		this.widthField.setMaxStringLength(4);
		this.widthField.setEnabled(false);
		this.widthField.setResponder(s -> {
			if (this.container.inputSlot.getHasStack()) {
				Reference.LOGGER.info("W:" + s);
				CompoundNBT nbt = new CompoundNBT();
				nbt.putInt("width", s.isEmpty() ? 1 : Integer.parseInt(s));
				ItemStack stack = this.container.inputSlot.getStack().copy();
				stack.getTag().merge(nbt);
				this.container.inputSlot.putStack(stack);
				this.container.detectAndSendChanges();
			}
		});
		this.children.add(this.widthField);
//		this.heightField = new TextFieldWidget(this.font, wh + 2, this.guiTop + 45 - 2, 40, 12, StringTextComponent.EMPTY) {
//			@Override
//			public boolean charTyped(char codePoint, int modifiers) {
//				return modifiers == 0 && Character.isDigit(codePoint) && super.charTyped(codePoint, modifiers);
//			}
//		};
//		this.heightField.setMaxStringLength(4);
//		this.heightField.setEnabled(false);
//		this.heightField.setResponder(s -> {
//			if (this.container.inputSlot.getHasStack()) {
//				Reference.LOGGER.info("H:" + s);
//				CompoundNBT nbt = new CompoundNBT();
//				nbt.putInt("height", s.isEmpty() ? 2 : Integer.parseInt(s));
//				ItemStack stack = this.container.inputSlot.getStack();
//				stack.getTag().merge(nbt);
//				this.container.inputSlot.putStack(stack);
//				this.container.detectAndSendChanges();
//			}
//		});
//		this.children.add(this.heightField);
		this.container.addListener(this);
	}

	@Override
	public void onClose() {
		super.onClose();
		this.minecraft.keyboardListener.enableRepeatEvents(false);
		this.container.removeListener(this);
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		this.widthField.render(stack, mouseX, mouseY, partialTicks);
//		this.heightField.render(stack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(stack, mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerForegroundLayer(MatrixStack stack, int x, int y) {
		super.drawGuiContainerForegroundLayer(stack, x, y);
		this.font.func_243248_b(stack, wComponent, 6, 30, 0x404040);
//		this.font.func_243248_b(stack, hComponent, 6, 45, 0x404040);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int x, int y) {
		this.renderBackground(stack);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(BG);
		this.blit(stack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
		this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
	}

	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
		if (slotInd == 0) {
			System.out.println();
			Reference.LOGGER.info("Tag: " + stack.getTag());
			this.widthField.setText(stack.isEmpty() ? "" : String.valueOf(stack.getTag().getInt("width")));
//			this.heightField.setText(stack.isEmpty() ? "" : String.valueOf(stack.getTag().getInt("height")));
			this.widthField.setEnabled(!stack.isEmpty());
//			this.heightField.setEnabled(!stack.isEmpty());
			this.setListener(this.widthField);
//			this.setListener(this.heightField);
		}
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}
}
