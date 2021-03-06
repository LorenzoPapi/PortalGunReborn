package com.github.lorenzopapi.pgr.portal.customizer;

import com.github.lorenzopapi.pgr.handler.PGRNetworkHandler;
import com.github.lorenzopapi.pgr.network.SUpdateChannelColor;
import com.github.lorenzopapi.pgr.network.SUpdatePGData;
import com.github.lorenzopapi.pgr.util.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Predicate;

// I've probably never coded something so badly hardcoded in my life like holy shit
// I hate my life

public class PGCScreen extends ContainerScreen<PGCContainer> {
	private static final ResourceLocation BG = new ResourceLocation(Reference.MOD_ID, "textures/gui/customizer.png");
	private final StringTextComponent widthText = new StringTextComponent("W:");
	int textWidth = 0;
	private TextFieldWidget widthField;
	private TextFieldWidget heightField;
	private TextFieldWidget colorAField;
	private TextFieldWidget colorBField;
	private final Slot input = this.container.getSlot(0);

	public PGCScreen(PGCContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		this.xSize = 175;
		this.ySize = 178;
		this.titleX = 6;
		this.titleY = 5;
		this.playerInventoryTitleX = 6;
		this.playerInventoryTitleY = this.ySize - 94;
	}

	@Override
	public void tick() {
		super.tick();
		for (IGuiEventListener listener : this.children)
			if (listener instanceof PGCTextWidget) {
				PGCTextWidget pg = (PGCTextWidget) listener;
				pg.tick();
			}
		if (this.container.currentTag != null && this.container.updated) {
			this.container.updated = false;
			this.widthField.setText(String.valueOf(this.container.currentTag.getInt("width")));
			this.heightField.setText(String.valueOf(this.container.currentTag.getInt("height")));
			this.colorAField.setText(Integer.toHexString(this.container.currentInfo.colorA).toUpperCase());
			this.colorBField.setText(Integer.toHexString(this.container.currentInfo.colorB).toUpperCase());
		}
	}

	@Override
	protected void init() {
		super.init();
		this.textWidth = this.font.getStringPropertyWidth(widthText);
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		this.widthField = new PGCTextWidget(6 + textWidth + 2, 16, Character::isDigit);
		this.widthField.setResponder(s -> {
			if (this.input.getHasStack() && !s.isEmpty()) {
				PGRNetworkHandler.HANDLER.sendToServer(new SUpdatePGData(this.input.getStack().copy(), Integer.parseInt(s), this.container.currentTag.getInt("height")));
			}
		});
		this.children.add(this.widthField);
		this.heightField = new PGCTextWidget(6 + textWidth + 2, 32, Character::isDigit);
		this.heightField.setResponder(s -> {
			if (this.input.getHasStack() && !s.isEmpty()) {
				PGRNetworkHandler.HANDLER.sendToServer(new SUpdatePGData(this.input.getStack().copy(), this.container.currentTag.getInt("width"), Integer.parseInt(s)));
			}
		});
		this.children.add(this.heightField);
		this.colorAField = new PGCTextWidget(6 + textWidth + 2 + 45 + 2 + textWidth + 2, 16, c -> Character.isDigit(c) || (c >= 65 && c <= 70) || (c >= 90 && c <= 102));
		this.colorAField.setResponder(s -> {
			if (this.input.getHasStack() && !s.isEmpty()) {
				PGRNetworkHandler.HANDLER.sendToServer(new SUpdateChannelColor(this.container.currentInfo, Integer.decode("#" + s), this.container.currentInfo.colorB));

				this.container.currentInfo.colorA = Integer.decode("#" + s);
			}
		});
		this.children.add(this.colorAField);
		this.colorBField = new PGCTextWidget(6 + textWidth + 2 + 45 + 2 + textWidth + 2, 32, c -> Character.isDigit(c) || (c >= 65 && c <= 70) || (c >= 90 && c <= 102));
		this.colorBField.setResponder(s -> {
			if (this.input.getHasStack() && !s.isEmpty()) {
				PGRNetworkHandler.HANDLER.sendToServer(new SUpdateChannelColor(this.container.currentInfo, this.container.currentInfo.colorA, Integer.decode("#" + s)));
				this.container.currentInfo.colorB = Integer.decode("#" + s);
			}
		});
		this.children.add(this.colorBField);
	}

	@Override
	public void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(stack, mouseX, mouseY);
		this.font.func_243248_b(stack, widthText, 6, 18, 0x404040);
		this.font.func_243248_b(stack, new StringTextComponent("H:"), 6, 34, 0x404040);
		this.font.func_243248_b(stack, new StringTextComponent("A:"), 6 + textWidth + 2 + 45 + 2, 18, 0x404040);
		this.font.func_243248_b(stack, new StringTextComponent("B:"), 6 + textWidth + 2 + 45 + 2, 34, 0x404040);

		int startAX = 6;
		this.font.func_243248_b(stack, new StringTextComponent("A:"), startAX, 51, 0x404040);
		startAX += textWidth + 2;
		this.fillContour(stack, startAX - 1);
		this.fillColor(stack, startAX, 0xff000000 | this.container.currentInfo.colorA);

		int startBX = startAX + 16 + 3;
		this.font.func_243248_b(stack, new StringTextComponent("B:"), startBX , 51, 0x404040);
		startBX += textWidth + 2;
		this.fillContour(stack, startBX - 1);
		this.fillColor(stack, startBX,0xff000000 | this.container.currentInfo.colorB);
	}

	private void fillColor(MatrixStack stack, int x, int color) {
		int y = 47;
		fill(stack, x, y, x + 16, y + 16, color);
	}

	private void fillContour(MatrixStack stack, int x) {
		int y = 46;
		fill(stack, x, y, x + 18, y + 18, -6250336);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int x, int y) {
		this.renderBackground(stack);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(BG);
		this.blit(stack, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void onClose() {
		super.onClose();
		this.minecraft.keyboardListener.enableRepeatEvents(false);
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		for (IGuiEventListener listener : this.children)
			if (listener instanceof PGCTextWidget) {
				PGCTextWidget pg = (PGCTextWidget) listener;
				pg.render(stack, mouseX, mouseY, partialTicks);
			}
		this.renderHoveredTooltip(stack, mouseX, mouseY);
	}

	private class PGCTextWidget extends TextFieldWidget {
		private final Predicate<Character> charPredicate;
		public PGCTextWidget(int x, int y, Predicate<Character> predicate) {
			super(PGCScreen.this.font, PGCScreen.this.guiLeft + x, PGCScreen.this.guiTop + y, 45, 12, StringTextComponent.EMPTY);
			this.setMaxStringLength(6);
			this.setEnabled(false);
			this.charPredicate = predicate;
		}

		@Override
		public boolean charTyped(char codePoint, int modifiers) {
			return charPredicate.test(codePoint) && super.charTyped(codePoint, modifiers);
		}

		@Override
		public void tick() {
			super.tick();
			this.setEnabled(PGCScreen.this.input.getHasStack());
			if (!PGCScreen.this.input.getHasStack()) {
				this.setText("");
			}
		}
	}
}