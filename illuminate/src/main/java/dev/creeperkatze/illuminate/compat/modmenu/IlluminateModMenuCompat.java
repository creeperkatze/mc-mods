/*? if fabric {*/
package dev.creeperkatze.illuminate.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.creeperkatze.illuminate.config.IlluminateConfigScreen;

public class IlluminateModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return IlluminateConfigScreen::create;
	}
}
/*?}*/
