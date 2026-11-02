/*? if fabric {*/
package dev.creeperkatze.accelerate.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.creeperkatze.accelerate.config.AccelerateConfigScreen;

public class AccelerateModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return AccelerateConfigScreen::create;
	}
}
/*?}*/
