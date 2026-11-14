/*? if neoforge {*/
/*package dev.creeperkatze.illuminate.loaders.neoforge;

import dev.creeperkatze.illuminate.Illuminate;
import dev.creeperkatze.illuminate.IlluminateCommands;
import dev.creeperkatze.illuminate.config.IlluminateConfigScreen;
//? if >=1.21.9 {
import dev.creeperkatze.illuminate.preview.IlluminateDebugEntry;
//?}
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
//? if >=1.21.9 {
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
//?}
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

@EventBusSubscriber(modid = Illuminate.MOD_ID, value = Dist.CLIENT)
@Mod(value = Illuminate.MOD_ID, dist = Dist.CLIENT)
public class IlluminateNeoForge {
	public IlluminateNeoForge(IEventBus modBus, ModContainer container) {
		Illuminate.init();
		container.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> IlluminateConfigScreen.create(parent));
		//? if >=1.21.9 {
		// Older NeoForge builds don't send mod bus events to @EventBusSubscriber classes
		modBus.addListener(IlluminateNeoForge::registerDebugEntries);
		//?}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Illuminate.onClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void registerCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(IlluminateCommands.create());
	}

	//? if >=1.21.9 {
	private static void registerDebugEntries(RegisterDebugEntriesEvent event) {
		event.register(IlluminateDebugEntry.ID, new IlluminateDebugEntry());
	}
	//?}

	@SubscribeEvent
	public static void onShuttingDown(GameShuttingDownEvent event) {
		Illuminate.onClientStopping();
	}
}
*//*?}*/
