/*? if fabric {*/
package dev.creeperkatze.speeddial.loaders.fabric;

import dev.creeperkatze.speeddial.FlightSpeed;
import dev.creeperkatze.speeddial.SpeedDial;
import dev.creeperkatze.speeddial.SpeedDialKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class SpeedDialFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SpeedDial.init();
		SpeedDialKeybinds.all().forEach(KeyMappingHelper::registerKeyMapping);
		ClientTickEvents.END_CLIENT_TICK.register(SpeedDial::onClientTick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightSpeed.saveRememberedSpeed());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> FlightSpeed.saveRememberedSpeed());
	}
}
/*?}*/
