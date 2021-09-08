package com.github.lorenzopapi.pgr.portal.customizer;

import com.github.lorenzopapi.pgr.handler.PGRNetworkHandler;
import com.github.lorenzopapi.pgr.network.SUpdatePGData;
import com.github.lorenzopapi.pgr.util.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PGCustomizerScreen extends ContainerScreen<PGCustomizerContainer> implements IContainerListener {
	private static final ResourceLocation BG = new ResourceLocation(Reference.MOD_ID, "textures/gui/customizer.png");
	private final StringTextComponent wComponent = new StringTextComponent("W:");
	private final StringTextComponent hComponent = new StringTextComponent("H:");
	private TextFieldWidget widthField;
	private TextFieldWidget heightField;
	private final Slot input = this.container.getSlot(0);

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
		if (this.widthField.isFocused()) {
			this.heightField.setFocused2(false);
		} else if (this.heightField.isFocused()) {
			this.widthField.setFocused2(false);
		}
		this.widthField.tick();
		this.heightField.tick();
		this.widthField.setEnabled(this.input.getHasStack());
		this.heightField.setEnabled(this.input.getHasStack());
	}

	@Override
	protected void init() {
		super.init();
		int ww = this.guiLeft + this.font.getStringPropertyWidth(wComponent) + 6;
		int wh = this.guiLeft + this.font.getStringPropertyWidth(hComponent) + 6;
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		this.widthField = new TextFieldWidget(this.font, ww + 2, this.guiTop + 30 - 2, 40, 12, StringTextComponent.EMPTY) {
			@Override
			public boolean charTyped(char codePoint, int modifiers) {
				return Character.isDigit(codePoint) && super.charTyped(codePoint, modifiers);
			}
		};
		this.widthField.setMaxStringLength(6);
		this.widthField.setEnabled(false);
		this.widthField.setResponder(s -> {
			if (this.input.getHasStack() && !s.isEmpty()) {
				PGRNetworkHandler.HANDLER.sendToServer(new SUpdatePGData(this.input.getStack().copy(), Integer.parseInt(s), this.input.getStack().getTag().getInt("height")));
			}
		});
		this.children.add(this.widthField);
		this.heightField = new TextFieldWidget(this.font, wh + 2, this.guiTop + 45 - 2, 40, 12, StringTextComponent.EMPTY) {
			@Override
			public boolean charTyped(char codePoint, int modifiers) {
				return Character.isDigit(codePoint) && super.charTyped(codePoint, modifiers);
			}
		};
		this.heightField.setMaxStringLength(6);
		this.heightField.setEnabled(false);
		this.heightField.setResponder(s -> {
			if (this.input.getHasStack() && !s.isEmpty()) {
				PGRNetworkHandler.HANDLER.sendToServer(new SUpdatePGData(this.input.getStack().copy(), this.input.getStack().getTag().getInt("width"), Integer.parseInt(s)));
			}
		});
		this.children.add(this.heightField);
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
		this.heightField.render(stack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(stack, mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerForegroundLayer(MatrixStack stack, int x, int y) {
		super.drawGuiContainerForegroundLayer(stack, x, y);
		this.font.func_243248_b(stack, wComponent, 6, 30, 0x404040);
		this.font.func_243248_b(stack, hComponent, 6, 45, 0x404040);
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
			this.widthField.setText(stack.isEmpty() ? "" : String.valueOf(stack.getTag().getInt("width")));
			this.heightField.setText(stack.isEmpty() ? "" : String.valueOf(stack.getTag().getInt("height")));
			this.setListener(this.widthField);
			this.setListener(this.heightField);
		}
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {}
}
