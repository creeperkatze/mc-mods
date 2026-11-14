/*? if fabric {*/
package dev.creeperkatze.illuminate.loaders.fabric;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.IlluminateCommands;
//? if >=1.21.9 {
import dev.creeperkatze.illuminate.preview.IlluminateDebugEntry;
//?}
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=1.21.9 {
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
//?}

public class IlluminateFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Illuminate.init();
		ClientTickEvents.END_CLIENT_TICK.register(Illuminate::onClientTick);
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Illuminate.onClientStopping());
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(IlluminateCommands.create()));
		//? if >=1.21.9 {
		DebugScreenEntries.register(IlluminateDebugEntry.ID, new IlluminateDebugEntry());
		//?}
	}
}
/*?}*/
