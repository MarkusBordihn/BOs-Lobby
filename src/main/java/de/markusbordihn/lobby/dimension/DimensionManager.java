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

package de.markusbordihn.lobby.dimension;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;
import de.markusbordihn.lobby.datapack.DataPackHandler;
import de.markusbordihn.lobby.teleporter.TeleporterManager;

@EventBusSubscriber
public class DimensionManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  private static String defaultDimension = COMMON.defaultDimension.get();
  private static int defaultFallProtection = COMMON.defaultFallProtection.get();
  private static int defaultFireProtection = COMMON.defaultFireProtection.get();
  private static int defaultHeal = COMMON.defaultHeal.get();

  private static boolean fishingEnabled = COMMON.fishingEnabled.get();
  private static String fishingDimension = COMMON.fishingDimension.get();
  private static boolean fishingDisableMobSpawning = COMMON.fishingDisableMobSpawning.get();
  private static List<String> fishingBuilderList = COMMON.fishingBuilderList.get();

  private static boolean gamingEnabled = COMMON.gamingEnabled.get();
  private static String gamingDimension = COMMON.gamingDimension.get();
  private static boolean gamingDisableMobSpawning = COMMON.gamingDisableMobSpawning.get();
  private static List<String> gamingBuilderList = COMMON.gamingBuilderList.get();

  private static boolean lobbyEnabled = COMMON.lobbyEnabled.get();
  private static String lobbyDimension = COMMON.lobbyDimension.get();
  private static boolean lobbyDisableMobSpawning = COMMON.lobbyDisableMobSpawning.get();
  private static List<String> lobbyBuilderList = COMMON.lobbyBuilderList.get();

  private static boolean miningEnabled = COMMON.miningEnabled.get();
  private static String miningDimension = COMMON.miningDimension.get();
  private static boolean miningDisableBatSpawning = COMMON.miningDisableBatSpawning.get();
  private static boolean miningDisableMobSpawning = COMMON.miningDisableMobSpawning.get();
  private static boolean miningDisableMinecartChestSpawning =
      COMMON.miningDisableMinecartChestSpawning.get();
  private static boolean miningRemoveSpawner = COMMON.miningRemoveSpawner.get();

  private static boolean voidEnabled = COMMON.voidEnabled.get();
  private static String voidDimension = COMMON.voidDimension.get();
  private static boolean voidDisableMobSpawning = COMMON.voidDisableMobSpawning.get();
  private static List<String> voidBuilderList = COMMON.voidBuilderList.get();

  private static Set<ServerPlayer> gameTypeReset = ConcurrentHashMap.newKeySet();
  private static Set<String> ignoredDimension = ConcurrentHashMap.newKeySet();

  private static ServerLevel defaultLevel = null;
  private static ServerLevel fishingLevel = null;
  private static ServerLevel gamingLevel = null;
  private static ServerLevel lobbyLevel = null;
  private static ServerLevel miningLevel = null;
  private static ServerLevel voidLevel = null;

  protected DimensionManager() {}

  @SubscribeEvent
  public static void handleServerAboutToStartEvent(ServerAboutToStartEvent event) {
    // Reset mapping to avoid issues.
    defaultLevel = null;
    fishingLevel = null;
    lobbyLevel = null;
    miningLevel = null;

    // Make sure we have the current config settings.
    defaultDimension = COMMON.defaultDimension.get();
    defaultFallProtection = COMMON.defaultFallProtection.get();
    defaultFireProtection = COMMON.defaultFireProtection.get();
    defaultHeal = COMMON.defaultHeal.get();

    fishingEnabled = COMMON.fishingEnabled.get();
    fishingDimension = COMMON.fishingDimension.get();
    fishingDisableMobSpawning = COMMON.fishingDisableMobSpawning.get();
    fishingBuilderList = COMMON.fishingBuilderList.get();

    gamingEnabled = COMMON.gamingEnabled.get();
    gamingDimension = COMMON.gamingDimension.get();
    gamingDisableMobSpawning = COMMON.gamingDisableMobSpawning.get();
    gamingBuilderList = COMMON.gamingBuilderList.get();

    lobbyEnabled = COMMON.lobbyEnabled.get();
    lobbyDimension = COMMON.lobbyDimension.get();
    lobbyDisableMobSpawning = COMMON.lobbyDisableMobSpawning.get();
    lobbyBuilderList = COMMON.lobbyBuilderList.get();

    miningEnabled = COMMON.miningEnabled.get();
    miningDimension = COMMON.miningDimension.get();
    miningDisableBatSpawning = COMMON.miningDisableBatSpawning.get();
    miningDisableMobSpawning = COMMON.miningDisableMobSpawning.get();
    miningDisableMinecartChestSpawning = COMMON.miningDisableMinecartChestSpawning.get();
    miningRemoveSpawner = COMMON.miningRemoveSpawner.get();

    voidEnabled = COMMON.voidEnabled.get();
    voidDimension = COMMON.voidDimension.get();
    voidDisableMobSpawning = COMMON.voidDisableMobSpawning.get();
    voidBuilderList = COMMON.voidBuilderList.get();
  }

  @SubscribeEvent
  public static void handleServerStartedEvent(ServerStartedEvent event) {
    // Map dimension and init dimension structure if needed.
    mapServerLevel(event.getServer());

    if (defaultFallProtection > 0) {
      log.info("{} Enable fall protection for default dimension for {} ticks.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, defaultFallProtection);
    } else {
      log.warn("{} Disable fall protection for default dimension!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }

    if (defaultFireProtection > 0) {
      log.info("{} Enable fire protection for default dimension for {} ticks.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, defaultFallProtection);
    } else {
      log.warn("{} Disable fire protection for default dimension!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }

    if (defaultHeal > 0) {
      log.info("{} Enable heal for default dimension for {} ticks.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, defaultHeal);
    }

    if (fishingDisableMobSpawning) {
      log.info("{} Disable Mob Spawning for fishing dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (lobbyDisableMobSpawning) {
      log.info("{} Disable mob spawning for lobby dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (miningDisableBatSpawning) {
      log.info("{} Disable bat spawning for mining dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (miningDisableMinecartChestSpawning) {
      log.info("{} Disable minecraft chest spawning for mining dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (miningDisableMobSpawning) {
      log.info("{} Disable mob spawning for mining dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
  }

  @SubscribeEvent
  public static void onChangeDimension(PlayerChangedDimensionEvent event) {
    Player player = event.getPlayer();
    String fromLocation = event.getFrom().location().toString();
    String toLocation = event.getTo().location().toString();

    // Make sure normal users are in Adventure mode for the fishing dimension even if they are using
    // tp or similar commands.
    if (toLocation.equals(fishingDimension)) {
      if (!fishingBuilderList.isEmpty()
          && fishingBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for fishing dimension.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
      return;
    }

    // Make sure normal users are in Adventure mode for the gaming dimension even if they are using
    // tp or similar commands.
    if (toLocation.equals(gamingDimension)) {
      if (!gamingBuilderList.isEmpty()
          && gamingBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for gaming dimension.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
      return;
    }

    // Make sure normal users are in Adventure mode for the lobby dimension even if they are using
    // tp or similar commands.
    if (toLocation.equals(lobbyDimension)) {
      if (!lobbyBuilderList.isEmpty() && lobbyBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for lobby.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
      return;
    }

    // Make sure normal users are in survival mode for the mining dimension even if they are using
    // tp or similar commands.
    if (toLocation.equals(miningDimension)) {
      changeGameType(player, GameType.SURVIVAL);
      return;
    }

    // Make sure builders are in creative mode for the void dimension even if they are using
    // tp or similar commands.
    if (toLocation.equals(voidDimension)) {
      if (!voidBuilderList.isEmpty() && voidBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for void.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      }
      return;
    }

    // Reset game type to survival if user is on the gameTypeReset list or comes from the fishing,
    // gaming, lobby or void dimensions.
    if (player instanceof ServerPlayer serverPlayer
        && (gameTypeReset.contains(serverPlayer) || (!fromLocation.isEmpty()
            && (fromLocation.equals(lobbyDimension) || fromLocation.equals(fishingDimension)
                || fromLocation.equals(gamingDimension) || fromLocation.equals(voidDimension))))) {

      // Add fall and fire protection for the player, if enabled.
      if (defaultFallProtection > 0) {
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, defaultFallProtection, 0,
            false, true, false));
        player.resetFallDistance();
      }
      if (defaultFireProtection > 0) {
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, defaultFireProtection, 0,
            false, true, false));
      }
      if (defaultHeal > 0) {
        player
            .addEffect(new MobEffectInstance(MobEffects.HEAL, defaultHeal, 0, false, true, false));
      }

      // Change Game Type
      changeGameType(serverPlayer, GameType.SURVIVAL);

      // Remove user from reset list
      if (gameTypeReset.contains(serverPlayer)) {
        gameTypeReset.remove(serverPlayer);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleEntityJoinWorldEvent(EntityJoinWorldEvent event) {

    // Ignore client side and everything which is not the mining dimension.
    Level level = event.getWorld();
    String dimensionLocation = level.dimension().location().toString();
    if (level.isClientSide() || !dimensionLocation.equals(miningDimension)) {
      return;
    }

    // Ignore specific entities and deny spawn of all others.
    Entity entity = event.getEntity();
    if (entity instanceof ItemEntity || entity instanceof ExperienceOrb
        || entity instanceof LightningBolt || entity instanceof FallingBlockEntity
        || entity instanceof Projectile || entity instanceof Player) {
      return;
    }

    // Allow/deny Minecart Chest spawning
    if (miningDisableMinecartChestSpawning && entity instanceof MinecartChest) {
      event.setResult(Event.Result.DENY);
    }

    // Allow/deny Mob spawning
    if (miningDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleLivingCheckSpawnEvent(LivingSpawnEvent.CheckSpawn event) {
    handleSpawnEvent(event);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleLivingSpecialSpawnEvent(LivingSpawnEvent.SpecialSpawn event) {
    handleSpawnEvent(event);
  }

  private static void mapServerLevel(MinecraftServer server) {
    // Skip search if we already found all relevant dimensions.
    if (defaultLevel != null && lobbyLevel != null && miningLevel != null && fishingLevel != null) {
      return;
    }

    // Mapping names to server level for easier access.
    for (ServerLevel serverLevel : server.getAllLevels()) {
      String dimensionLocation = serverLevel.dimension().location().toString();
      if (dimensionLocation.equals(defaultDimension)) {
        if (defaultLevel == null) {
          log.info("{} ✔️ Found default dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, defaultDimension, serverLevel);
          defaultLevel = serverLevel;
        }
      } else if (lobbyEnabled && dimensionLocation.equals(lobbyDimension)) {
        if (lobbyLevel == null) {
          log.info("{} ✔️ Found lobby dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, lobbyDimension, serverLevel);
          lobbyLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(lobbyLevel);
        }
      } else if (miningEnabled && dimensionLocation.equals(miningDimension)) {
        if (miningLevel == null) {
          log.info("{} ✔️ Found mining dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, miningDimension, serverLevel);
          miningLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(miningLevel);
        }
      } else if (fishingEnabled && dimensionLocation.equals(fishingDimension)) {
        if (fishingLevel == null) {
          log.info("{} ✔️ Found fishing dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, fishingDimension, serverLevel);
          fishingLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(fishingLevel);
        }
      } else if (gamingEnabled && dimensionLocation.equals(gamingDimension)) {
        if (gamingLevel == null) {
          log.info("{} ✔️ Found gaming dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, gamingDimension, serverLevel);
          gamingLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(gamingLevel);
        }
      } else if (voidEnabled && dimensionLocation.equals(voidDimension)) {
        if (voidLevel == null) {
          log.info("{} ✔️ Found void dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, voidDimension, serverLevel);
          voidLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(voidLevel);
        }
      } else {
        if (ignoredDimension.isEmpty() || !ignoredDimension.contains(dimensionLocation)) {
          log.info("{} Ignore dimension {}: {}", Constants.LOG_DIMENSION_MANAGER_PREFIX,
              dimensionLocation, serverLevel);
        } else {
          ignoredDimension.add(dimensionLocation);
        }
      }
    }

    // Give error messages, if we are unable to match any dimension.
    if (defaultLevel == null) {
      log.error("{} ⚠️ Unable to find default dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, defaultDimension);
    }
    if (fishingLevel == null && fishingEnabled) {
      log.error("{} ⚠️ Unable to find fishing dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, fishingDimension);
    }
    if (gamingLevel == null && gamingEnabled) {
      log.error("{} ⚠️ Unable to find gaming dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, gamingDimension);
    }
    if (lobbyLevel == null && lobbyEnabled) {
      log.error("{} ⚠️ Unable to find lobby dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, lobbyDimension);
    }
    if (miningLevel == null && miningEnabled) {
      log.error("{} ⚠️ Unable to find mining dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, miningDimension);
    }
    if (voidLevel == null && voidEnabled) {
      log.error("{} ⚠️ Unable to find void dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, voidDimension);
    }

    if (defaultLevel != null && fishingLevel == null && lobbyLevel == null && miningLevel == null) {
      log.error("{} ⚠️ Unable to find the needed custom dimensions!\n"
          + "If this is the first time you see this message or if you just started a new world, try to restart your server to generate them automatically!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
  }

  public static ServerLevel getLobbyDimension() {
    if (lobbyLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return lobbyLevel;
  }

  public static String getLobbyDimensionName() {
    return lobbyDimension;
  }


  public static ServerLevel getFishingDimension() {
    if (fishingLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return fishingLevel;
  }

  public static String getFishingDimensionName() {
    return fishingDimension;
  }

  public static ServerLevel getGamingDimension() {
    if (gamingLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return gamingLevel;
  }

  public static String getGamingDimensionName() {
    return gamingDimension;
  }

  public static ServerLevel getMiningDimension() {
    if (miningLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return miningLevel;
  }

  public static String getMiningDimensionName() {
    return miningDimension;
  }


  public static ServerLevel getDefaultDimension() {
    if (defaultLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return defaultLevel;
  }

  public static String getDefaultDimensionName() {
    return defaultDimension;
  }

  public static ServerLevel getVoidDimension() {
    if (voidLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return voidLevel;
  }

  public static String getVoidDimensionName() {
    return voidDimension;
  }

  public static void teleportToDefault(ServerPlayer player) {
    if (TeleporterManager.teleportToDefaultDimension(player)) {
      changeGameType(player, GameType.SURVIVAL);
    }
  }

  public static void teleportToFishing(ServerPlayer player) {
    if (TeleporterManager.teleportToFishingDimension(player)) {
      if (!fishingBuilderList.isEmpty()
          && fishingBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for fishing dimension.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
    }
  }

  public static void teleportToGaming(ServerPlayer player) {
    if (TeleporterManager.teleportToGamingDimension(player)) {
      if (!gamingBuilderList.isEmpty()
          && gamingBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for gaming.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
    }
  }

  public static void teleportToLobby(ServerPlayer player) {
    if (TeleporterManager.teleportToLobbyDimension(player)) {
      if (!lobbyBuilderList.isEmpty() && lobbyBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for lobby.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
    }
  }

  public static void teleportToMining(ServerPlayer player) {
    if (TeleporterManager.teleportToMiningDimension(player)) {
      changeGameType(player, GameType.SURVIVAL);
    }
  }

  public static void teleportToVoid(ServerPlayer player) {
    if (TeleporterManager.teleportToVoidDimension(player)) {
      if (!voidBuilderList.isEmpty() && voidBuilderList.contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for void.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      } else {
        changeGameType(player, GameType.ADVENTURE);
      }
    }
  }

  public static void changeGameType(Player player, GameType gameType) {
    if (player instanceof ServerPlayer serverPlayer) {
      changeGameType(serverPlayer, gameType);
    }
  }

  public static void changeGameType(ServerPlayer serverPlayer, GameType gameType) {
    GameType currentGameType = serverPlayer.gameMode.getGameModeForPlayer();
    if (currentGameType != gameType && shouldChangeGameType(serverPlayer)) {
      // Add player to reset list of the game mode if game type is not survival to avoid cheating.
      if (gameType != GameType.SURVIVAL) {
        gameTypeReset.add(serverPlayer);
      }
      log.debug("{} Changing players {} game type from {} to {}", serverPlayer, currentGameType,
          Constants.LOG_DIMENSION_MANAGER_PREFIX, gameType);
      serverPlayer.setGameMode(gameType);
    }
  }

  private static boolean shouldChangeGameType(Player player) {
    return !(player.isSpectator() || (player.hasPermissions(2) && player.isCreative()));
  }

  private static void handleSpawnEvent(LivingSpawnEvent event) {

    // Ignore client side.
    LevelAccessor level = event.getWorld();
    if (level.isClientSide()) {
      return;
    }

    // Ignore null entities and specific entities.
    Entity entity = event.getEntity();
    if (entity == null || entity instanceof Projectile) {
      return;
    }

    // Control spawns depending on the dimension.
    String dimensionLocation = entity.getLevel().dimension().location().toString();
    if (fishingDisableMobSpawning && fishingDimension.equals(dimensionLocation)) {
      handleSpawnEventFishing(event);
    } else if (lobbyDisableMobSpawning && lobbyDimension.equals(dimensionLocation)) {
      handleSpawnEventLobby(event);
    } else if (gamingDisableMobSpawning && gamingDimension.equals(dimensionLocation)) {
      handleSpawnEventGaming(event);
    } else if ((miningDisableBatSpawning || miningDisableMinecartChestSpawning
        || miningDisableMobSpawning) && miningDimension.equals(dimensionLocation)) {
      handleSpawnEventMining(level, entity, event);
    } else if (voidDisableMobSpawning && voidDimension.equals(dimensionLocation)) {
      handleSpawnEventVoid(event);
    }
  }

  private static void handleSpawnEventFishing(LivingSpawnEvent event) {
    if (fishingDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventGaming(LivingSpawnEvent event) {
    if (gamingDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventLobby(LivingSpawnEvent event) {
    if (lobbyDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventMining(LevelAccessor level, Entity entity,
      LivingSpawnEvent event) {
    // Removing spawners as soon they try to spawn something.
    if (miningRemoveSpawner && event instanceof LivingSpawnEvent.CheckSpawn checkSpawn
        && checkSpawn.getSpawner() != null) {
      BaseSpawner spawner = checkSpawn.getSpawner();
      BlockPos blockPos = spawner.getSpawnerBlockEntity().getBlockPos();
      if (blockPos != null) {
        log.debug("{} Removing spawner {} at {}", Constants.LOG_DIMENSION_MANAGER_PREFIX, spawner,
            blockPos);
        level.removeBlock(blockPos, true);
      }
    }

    // Allow/deny bat spawning for better cave experience
    if (entity instanceof Bat) {
      if (miningDisableBatSpawning) {
        event.setResult(Event.Result.DENY);
      }
      return;
    }

    // Allow/deny Minecart Chest spawning
    if (entity instanceof MinecartChest) {
      if (miningDisableMinecartChestSpawning) {
        event.setResult(Event.Result.DENY);
      }
      return;
    }

    if (miningDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventVoid(LivingSpawnEvent event) {
    if (voidDisableMobSpawning) {
      event.setResult(Event.Result.DENY);
    }
  }

}
