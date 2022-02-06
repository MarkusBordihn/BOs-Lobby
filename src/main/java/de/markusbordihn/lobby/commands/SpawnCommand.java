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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.dimension.DimensionManager;

@EventBusSubscriber
public class SpawnCommand extends CustomCommand {

  public static final String NAME = "spawn";
  public static final String DIMENSION_NAME = "Spawn";

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;
  private static boolean defaultRestrictCommand = COMMON.defaultRestrictCommand.get();
  private static int generalCommandCoolDown = COMMON.generalCommandCoolDown.get();

  private static Map<Player, Long> coolDownPlayerMap = new ConcurrentHashMap<>();

  private static final SpawnCommand command = new SpawnCommand();

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    defaultRestrictCommand = COMMON.defaultRestrictCommand.get();
    generalCommandCoolDown = COMMON.generalCommandCoolDown.get();
  }

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    dispatcher
        .register(Commands.literal(NAME).requires(cs -> cs.hasPermission(0)).executes(command));
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    ServerPlayer player = context.getSource().getPlayerOrException();

    // Handle cool-down time of command to avoid command misusage.
    Long coolDownTimer = coolDownPlayerMap.getOrDefault(player, null);
    Long currentTimer = java.time.Instant.now().getEpochSecond();
    if (coolDownTimer != null && coolDownTimer > currentTimer) {
      sendFeedback(context, new TranslatableComponent(Constants.TELEPORT_FAILED_COOLDOWN,
          DIMENSION_NAME, coolDownTimer - currentTimer));
      return 0;
    } else {
      coolDownPlayerMap.put(player, currentTimer + generalCommandCoolDown);
    }

    // Provide feedback to the player for their teleporter request.
    if (DimensionManager.getDefaultDimension() == null) {
      sendFeedback(context, new TranslatableComponent(Constants.UNABLE_TO_TELEPORT_MESSAGE,
          DIMENSION_NAME, DimensionManager.getDefaultDimensionName()));
    } else if (!defaultRestrictCommand
        || player.getLevel() != DimensionManager.getDefaultDimension()) {
      sendFeedback(context,
          new TranslatableComponent(Constants.TELEPORT_TO_MESSAGE, DIMENSION_NAME));
      DimensionManager.teleportToDefault(player);
    } else {
      sendFeedback(context, new TranslatableComponent(
          Constants.TELEPORT_FAILED_ALREADY_IN_DIMENSION_MESSAGE, DIMENSION_NAME));
    }
    return 0;
  }
}
