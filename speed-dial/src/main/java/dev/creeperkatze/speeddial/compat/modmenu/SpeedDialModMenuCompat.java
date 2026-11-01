/*? if fabric {*/
package dev.creeperkatze.speeddial.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.creeperkatze.speeddial.config.SpeedDialConfigScreen;

public class SpeedDialModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return SpeedDialConfigScreen::create;
	}
}
/*?}*/
