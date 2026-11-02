package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

// Like /kick, banning the host's address by their name fails
@Mixin(BanIpCommands.class)
public class BanIpCommandsMixin {
	@WrapMethod(method = "banIpOrName")
	private static int gather$refuseHost(CommandSourceStack source, String target, @Nullable Component reason, Operation<Integer> original) throws CommandSyntaxException {
		ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(target);
		if (player != null && LanHosting.isHost(source.getServer(), player.getGameProfile())) {
			throw LanHosting.BAN_OWNER.create();
		}
		return original.call(source, target, reason);
	}
}
