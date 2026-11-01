/*? if neoforge {*/
/*package dev.creeperkatze.speeddial.loaders.neoforge;

import dev.creeperkatze.speeddial.FlightSpeed;
import dev.creeperkatze.speeddial.SpeedDial;
import dev.creeperkatze.speeddial.SpeedDialKeybinds;
import dev.creeperkatze.speeddial.config.SpeedDialConfigScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.GameShuttingDownEvent;

@EventBusSubscriber(modid = SpeedDialNeoForge.MOD_ID, value = Dist.CLIENT)
@Mod(value = SpeedDialNeoForge.MOD_ID, dist = Dist.CLIENT)
public class SpeedDialNeoForge {
	// NeoForge mod ids can not contain hyphens
	public static final String MOD_ID = "speed_dial";

	public SpeedDialNeoForge(ModContainer container) {
		SpeedDial.init();
		container.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) -> SpeedDialConfigScreen.create(parent));
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		SpeedDialKeybinds.all().forEach(event::register);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		SpeedDial.onClientTick(Minecraft.getInstance());
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
