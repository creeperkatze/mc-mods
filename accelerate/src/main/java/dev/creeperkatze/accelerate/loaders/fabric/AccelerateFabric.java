/*? if fabric {*/
package dev.creeperkatze.accelerate.loaders.fabric;

import dev.creeperkatze.accelerate.FlightSpeed;
import dev.creeperkatze.accelerate.Accelerate;
import dev.creeperkatze.accelerate.AccelerateKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class AccelerateFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Accelerate.init();
		AccelerateKeybinds.all().forEach(KeyMappingHelper::registerKeyMapping);
		ClientTickEvents.END_CLIENT_TICK.register(Accelerate::onClientTick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightSpeed.saveRememberedSpeed());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> FlightSpeed.saveRememberedSpeed());
	}
}
/*?}*/
