package dev.creeperkatze.illuminate.config;

import dev.creeperkatze.illuminate.Illuminate;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
//? if >=1.21.9 {
import net.minecraft.network.chat.FontDescription;
//?}
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

// Small glyphs from the illuminate:icons font for the device settings and the live preview
final class Icons {
	static final char DOT = '\uE000';
	static final char SIGNALRGB = '\uE001';
	static final char OPENRGB = '\uE002';
	static final char HYPERION = '\uE003';
	static final char KOFI = '\uE004';

	//? if >=1.21.9 {
	private static final FontDescription FONT = new FontDescription.Resource(
			Identifier.fromNamespaceAndPath(Illuminate.MOD_ID, "icons"));
	//?} else {
	/*private static final Identifier FONT = Identifier.fromNamespaceAndPath(Illuminate.MOD_ID, "icons");
	*///?}

	private Icons() {
	}

	// White so the glyph keeps its own colours
	static MutableComponent icon(char glyph) {
		return Component.literal(String.valueOf(glyph))
				.withStyle(style -> style.withFont(FONT).withColor(ChatFormatting.WHITE));
	}

	static MutableComponent label(char glyph, Component text) {
		return Component.empty().append(icon(glyph)).append(" ").append(text);
	}

	// A dot in the colour of the status, followed by its name
	static MutableComponent status(String status) {
		ChatFormatting color = switch (status) {
			case "connected" -> ChatFormatting.GREEN;
			case "connecting" -> ChatFormatting.YELLOW;
			case "unavailable" -> ChatFormatting.RED;
			default -> ChatFormatting.GRAY;
		};
		return Component.empty()
				.append(Component.literal(String.valueOf(DOT)).withStyle(style -> style.withFont(FONT).withColor(color)))
				.append(" ")
				.append(Component.translatable("illuminate.config.status." + status));
	}
}
