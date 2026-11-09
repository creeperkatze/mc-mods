/*? if fabric {*/
package dev.creeperkatze.accelerate.loaders.fabric;

import dev.creeperkatze.accelerate.FlightSpeed;
import dev.creeperkatze.accelerate.Accelerate;
import dev.creeperkatze.accelerate.AccelerateKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
*///?}
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class AccelerateFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Accelerate.init();
		//? if >=26.1 {
		AccelerateKeybinds.all().forEach(KeyMappingHelper::registerKeyMapping);
		//?} else {
		/*AccelerateKeybinds.all().forEach(KeyBindingHelper::registerKeyBinding);
		*///?}
		ClientTickEvents.END_CLIENT_TICK.register(Accelerate::onClientTick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightSpeed.saveRememberedSpeed());
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> FlightSpeed.saveRememberedSpeed());
	}
}
/*?}*/
