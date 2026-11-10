/*? if fabric {*/
package dev.creeperkatze.resume.loaders.fabric;

import dev.creeperkatze.resume.Resume;
import net.fabricmc.api.ClientModInitializer;

public class ResumeFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Resume.init();
	}
}
/*?}*/
