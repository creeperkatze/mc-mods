package dev.creeperkatze.gather.mixin;

import dev.creeperkatze.gather.LanHosting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin({BanIpCommands.class, BanListCommands.class, BanPlayerCommands.class, PardonCommand.class, PardonIpCommand.class, SetPlayerIdleTimeoutCommand.class, WhitelistCommand.class})
public class HostCommandsMixin {
	@ModifyArg(method = "register", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;requires(Ljava/util/function/Predicate;)Lcom/mojang/brigadier/builder/ArgumentBuilder;"))
	private static Predicate<CommandSourceStack> gather$allowHost(Predicate<CommandSourceStack> requirement) {
		return LanHosting.orHost(requirement);
	}
}
