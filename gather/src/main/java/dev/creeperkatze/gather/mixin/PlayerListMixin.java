package dev.creeperkatze.gather.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.creeperkatze.gather.LanHosting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@ModifyExpressionValue(method = "<init>", at = {
			@At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;WHITELIST_FILE:Ljava/io/File;", opcode = Opcodes.GETSTATIC),
			@At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;OPLIST_FILE:Ljava/io/File;", opcode = Opcodes.GETSTATIC),
			@At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;USERBANLIST_FILE:Ljava/io/File;", opcode = Opcodes.GETSTATIC),
			@At(value = "FIELD", target = "Lnet/minecraft/server/players/PlayerList;IPBANLIST_FILE:Ljava/io/File;", opcode = Opcodes.GETSTATIC)
	})
	private File gather$worldFile(File file, @Local(argsOnly = true) MinecraftServer server) {
		return LanHosting.playerListFile(server, file);
	}

	@ModifyReturnValue(method = "isWhiteListed", at = @At("RETURN"))
	private boolean gather$hostIsWhitelisted(boolean original, @Local(argsOnly = true) NameAndId player) {
		return original || LanHosting.isHost(((PlayerList) (Object) this).getServer(), player);
	}
}
