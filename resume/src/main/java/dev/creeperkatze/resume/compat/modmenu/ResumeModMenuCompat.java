/*? if fabric {*/
package dev.creeperkatze.resume.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.creeperkatze.resume.config.ResumeConfigScreen;

public class ResumeModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ResumeConfigScreen::create;
	}
}
/*?}*/
