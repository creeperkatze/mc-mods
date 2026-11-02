package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collection;
import java.util.List;

// Like /kick, banning skips the host and fails if the host was the only target
@Mixin(BanPlayerCommands.class)
public class BanPlayerCommandsMixin {
	@WrapMethod(method = "banPlayers")
	private static int gather$skipHost(CommandSourceStack source, Collection<NameAndId> players, @Nullable Component reason, Operation<Integer> original) throws CommandSyntaxException {
		List<NameAndId> others = players.stream().filter(player -> !LanHosting.isHost(source.getServer(), player)).toList();
		if (others.isEmpty() && !players.isEmpty()) {
			throw LanHosting.BAN_OWNER.create();
		}
		return original.call(source, others, reason);
	}
}
