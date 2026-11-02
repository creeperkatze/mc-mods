package dev.creeperkatze.gather.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.BanIpCommands;
import net.minecraft.server.commands.BanListCommands;
import net.minecraft.server.commands.BanPlayerCommands;
import net.minecraft.server.commands.DeOpCommands;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.commands.PardonCommand;
import net.minecraft.server.commands.PardonIpCommand;
import net.minecraft.server.commands.SetPlayerIdleTimeoutCommand;
import net.minecraft.server.commands.WhitelistCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
	@Shadow
	@Final
	private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void gather$registerServerCommands(Commands.CommandSelection selection, CommandBuildContext context,
			CallbackInfo ci) {
		if (selection != Commands.CommandSelection.INTEGRATED) {
			return;
		}
		BanIpCommands.register(this.dispatcher);
		BanListCommands.register(this.dispatcher);
		BanPlayerCommands.register(this.dispatcher);
		DeOpCommands.register(this.dispatcher);
		OpCommand.register(this.dispatcher);
		PardonCommand.register(this.dispatcher);
		PardonIpCommand.register(this.dispatcher);
		SetPlayerIdleTimeoutCommand.register(this.dispatcher);
		WhitelistCommand.register(this.dispatcher);
	}
}
