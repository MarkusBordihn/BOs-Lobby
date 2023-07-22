/**
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.lobby.commands;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;
import de.markusbordihn.lobby.teleporter.PlayerTeleportManager;

public class VoidCommand extends CustomCommand {

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  private static Map<Player, Long> coolDownPlayerMap = new ConcurrentHashMap<>();

  private static final VoidCommand command = new VoidCommand();

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    if (Boolean.FALSE.equals(COMMON.voidEnabled.get())) {
      return;
    }
    registerCommand(COMMON.voidCommandName.get(), COMMON.voidDimensionName.get(),
        COMMON.voidCommandPermissionLevel.get());
    dispatcher.register(Commands.literal(COMMON.voidCommandName.get())
        .requires(cs -> cs.hasPermission(COMMON.voidCommandPermissionLevel.get()))
        .executes(command));
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();

    // Handle cool-down time of command to avoid command misusage.
    Long coolDownTimer = coolDownPlayerMap.getOrDefault(player, null);
    Long currentTimer = java.time.Instant.now().getEpochSecond();
    if (coolDownTimer != null && coolDownTimer > currentTimer) {
      sendFeedback(context,
          Component.translatable(Constants.TELEPORT_FAILED_COOLDOWN, COMMON.voidDimensionName.get(),
              coolDownTimer - currentTimer).withStyle(ChatFormatting.RED));
      return 0;
    } else {
      coolDownPlayerMap.put(player, currentTimer + COMMON.generalCommandCoolDown.get());
    }

    // Provide feedback to the player for their teleporter request.
    if (DimensionManager.getVoidDimension() == null) {
      sendFeedback(context, Component.translatable(Constants.UNABLE_TO_TELEPORT_MESSAGE,
          COMMON.voidDimensionName.get(), DimensionManager.getVoidDimensionName()));
    } else if (Boolean.TRUE.equals(!COMMON.voidRestrictCommand.get())
        || player.level() != DimensionManager.getVoidDimension()) {
      if (Boolean.TRUE.equals(COMMON.teleportDelayEnabled.get())
          && COMMON.teleportDelayCounter.get() > 0) {
        sendFeedback(context,
            Component.translatable(Constants.TELEPORT_TO_IN_MESSAGE, COMMON.voidDimensionName.get(),
                COMMON.teleportDelayCounter.get()).withStyle(ChatFormatting.GREEN));
        PlayerTeleportManager.teleportPlayerToVoid(player);
      } else {
        sendFeedback(context,
            Component.translatable(Constants.TELEPORT_TO_MESSAGE, COMMON.voidDimensionName.get())
                .withStyle(ChatFormatting.GREEN));
        DimensionManager.teleportToVoid(player);
      }
    } else {
      sendFeedback(context,
          Component.translatable(Constants.TELEPORT_FAILED_ALREADY_IN_DIMENSION_MESSAGE,
              COMMON.voidDimensionName.get()).withStyle(ChatFormatting.YELLOW));
    }
    return 0;
  }
}
