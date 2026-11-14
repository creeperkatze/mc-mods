/*? if neoforge {*/
/*package dev.creeperkatze.accelerate.loaders.neoforge;

import dev.creeperkatze.accelerate.FlightSpeed;
import dev.creeperkatze.accelerate.Accelerate;
import dev.creeperkatze.accelerate.AccelerateKeybinds;
import dev.creeperkatze.accelerate.config.AccelerateConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

@EventBusSubscriber(modid = Accelerate.MOD_ID, value = Dist.CLIENT)
@Mod(value = Accelerate.MOD_ID, dist = Dist.CLIENT)
public class AccelerateNeoForge {
	public AccelerateNeoForge(IEventBus modBus, ModContainer container) {
		Accelerate.init();
		container.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> AccelerateConfigScreen.create(parent));
		// Older NeoForge builds don't send mod bus events to @EventBusSubscriber classes
		modBus.addListener(AccelerateNeoForge::registerKeyMappings);
	}

	private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		AccelerateKeybinds.all().forEach(event::register);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Accelerate.onClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		FlightSpeed.saveRememberedSpeed();
	}

	@SubscribeEvent
	public static void onShuttingDown(GameShuttingDownEvent event) {
		FlightSpeed.saveRememberedSpeed();
	}
}
*//*?}*/
