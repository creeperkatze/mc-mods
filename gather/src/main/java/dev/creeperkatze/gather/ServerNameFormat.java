package dev.creeperkatze.gather;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Server names use & codes like &6 for gold, the LAN list can only show these classic colors and styles
public final class ServerNameFormat {
	private static final char CODE_PREFIX = '&';
	private static final Style CODE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);

	private ServerNameFormat() {
	}

	private static @Nullable ChatFormatting codeAt(String name, int index) {
		return name.charAt(index) == CODE_PREFIX && index + 1 < name.length() ? ChatFormatting.getByCode(name.charAt(index + 1)) : null;
	}

	// Turns & codes into the § codes Minecraft draws as colors and styles
	public static String toLegacy(String name) {
		StringBuilder legacy = new StringBuilder(name);
		for (int i = 0; i < name.length(); i++) {
			if (codeAt(name, i) != null) {
				legacy.setCharAt(i, ChatFormatting.PREFIX_CODE);
			}
		}
		return legacy.toString();
	}

	// Colors the edit box text but keeps the codes visible, so the cursor stays where it was typed
	public static FormattedCharSequence preview(String name, String visible, int offset) {
		List<Style> styles = new ArrayList<>(name.length());
		Style style = Style.EMPTY;
		for (int i = 0; i < name.length(); i++) {
			ChatFormatting format = codeAt(name, i);
			if (format != null) {
				styles.add(CODE_STYLE);
				styles.add(CODE_STYLE);
				// Bold is left out, its wider letters would move the cursor away from the text
				if (format == ChatFormatting.RESET) {
					style = Style.EMPTY;
				} else if (format != ChatFormatting.BOLD) {
					style = style.applyLegacyFormat(format);
				}
				i++;
			} else {
				styles.add(style);
			}
		}
		return sink -> {
			for (int i = 0; i < visible.length(); i++) {
				int index = offset + i;
				Style charStyle = index < styles.size() ? styles.get(index) : Style.EMPTY;
				if (!sink.accept(i, charStyle, visible.charAt(i))) {
					return false;
				}
			}
			return true;
		};
	}
}
