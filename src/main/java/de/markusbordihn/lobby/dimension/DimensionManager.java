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
  }

  @SubscribeEvent
  public static void handleServerStartedEvent(ServerStartedEvent event) {
    // Map dimension and init dimension structure if needed.
    mapServerLevel(event.getServer());

    if (COMMON.defaultFallProtection.get() > 0) {
      log.info("{} Enable fall protection for default dimension for {} ticks.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.defaultFallProtection.get());
    } else {
      log.warn("{} Disable fall protection for default dimension!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }

    if (COMMON.defaultFireProtection.get() > 0) {
      log.info("{} Enable fire protection for default dimension for {} ticks.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.defaultFallProtection.get());
    } else {
      log.warn("{} Disable fire protection for default dimension!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }

    if (COMMON.defaultHeal.get() > 0) {
      log.info("{} Enable heal for default dimension for {} ticks.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.defaultHeal.get());
    }

    if (Boolean.TRUE.equals(COMMON.fishingDisableMobSpawning.get())) {
      log.info("{} Disable Mob Spawning for fishing dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (Boolean.TRUE.equals(COMMON.lobbyDisableMobSpawning.get())) {
      log.info("{} Disable mob spawning for lobby dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (Boolean.TRUE.equals(COMMON.miningDisableBatSpawning.get())) {
      log.info("{} Disable bat spawning for mining dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (Boolean.TRUE.equals(COMMON.miningDisableMinecartChestSpawning.get())) {
      log.info("{} Disable minecraft chest spawning for mining dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (Boolean.TRUE.equals(COMMON.miningDisableMobSpawning.get())) {
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
    if (toLocation.equals(COMMON.fishingDimension.get())) {
      if (!COMMON.fishingBuilderList.get().isEmpty()
          && COMMON.fishingBuilderList.get().contains(player.getName().getString())) {
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
    if (toLocation.equals(COMMON.gamingDimension.get())) {
      if (!COMMON.gamingBuilderList.get().isEmpty()
          && COMMON.gamingBuilderList.get().contains(player.getName().getString())) {
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
    if (toLocation.equals(COMMON.lobbyDimension.get())) {
      if (!COMMON.lobbyBuilderList.get().isEmpty()
          && COMMON.lobbyBuilderList.get().contains(player.getName().getString())) {
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
    if (toLocation.equals(COMMON.miningDimension.get())) {
      changeGameType(player, GameType.SURVIVAL);
      return;
    }

    // Make sure builders are in creative mode for the void dimension even if they are using
    // tp or similar commands.
    if (toLocation.equals(COMMON.voidDimension.get())) {
      if (!COMMON.voidBuilderList.get().isEmpty()
          && COMMON.voidBuilderList.get().contains(player.getName().getString())) {
        log.info("{} Give builder {} creative mode for void.",
            Constants.LOG_DIMENSION_MANAGER_PREFIX, player.getName().getString());
        changeGameType(player, GameType.CREATIVE);
      }
      return;
    }

    // Reset game type to survival if user is on the gameTypeReset list or comes from the fishing,
    // gaming, lobby or void dimensions.
    if (player instanceof ServerPlayer serverPlayer && (gameTypeReset.contains(serverPlayer)
        || (!fromLocation.isEmpty() && (fromLocation.equals(COMMON.lobbyDimension.get())
            || fromLocation.equals(COMMON.fishingDimension.get())
            || fromLocation.equals(COMMON.gamingDimension.get())
            || fromLocation.equals(COMMON.voidDimension.get()))))) {

      // Add fall and fire protection for the player, if enabled.
      if (COMMON.defaultFallProtection.get() > 0) {
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
            COMMON.defaultFallProtection.get(), 0, false, true, false));
        player.resetFallDistance();
      }
      if (COMMON.defaultFireProtection.get() > 0) {
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
            COMMON.defaultFireProtection.get(), 0, false, true, false));
      }
      if (COMMON.defaultHeal.get() > 0) {
        player.addEffect(new MobEffectInstance(MobEffects.HEAL, COMMON.defaultHeal.get(), 0, false,
            true, false));
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
    if (level.isClientSide() || !dimensionLocation.equals(COMMON.miningDimension.get())) {
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
    if (Boolean.TRUE.equals(COMMON.miningDisableMinecartChestSpawning.get())
        && entity instanceof MinecartChest) {
      event.setResult(Event.Result.DENY);
    }

    // Allow/deny Mob spawning
    if (Boolean.TRUE.equals(COMMON.miningDisableMobSpawning.get())) {
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
      if (dimensionLocation.equals(COMMON.defaultDimension.get())) {
        if (defaultLevel == null) {
          log.info("{} ✔️ Found default dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.defaultDimension.get(), serverLevel);
          defaultLevel = serverLevel;
        }
      } else if (Boolean.TRUE.equals(COMMON.lobbyEnabled.get())
          && dimensionLocation.equals(COMMON.lobbyDimension.get())) {
        if (lobbyLevel == null) {
          log.info("{} ✔️ Found lobby dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.lobbyDimension.get(), serverLevel);
          lobbyLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(lobbyLevel);
        }
      } else if (Boolean.TRUE.equals(COMMON.miningEnabled.get())
          && dimensionLocation.equals(COMMON.miningDimension.get())) {
        if (miningLevel == null) {
          log.info("{} ✔️ Found mining dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.miningDimension.get(), serverLevel);
          miningLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(miningLevel);
        }
      } else if (Boolean.TRUE.equals(COMMON.fishingEnabled.get())
          && dimensionLocation.equals(COMMON.fishingDimension.get())) {
        if (fishingLevel == null) {
          log.info("{} ✔️ Found fishing dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.fishingDimension.get(), serverLevel);
          fishingLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(fishingLevel);
        }
      } else if (Boolean.TRUE.equals(COMMON.gamingEnabled.get())
          && dimensionLocation.equals(COMMON.gamingDimension.get())) {
        if (gamingLevel == null) {
          log.info("{} ✔️ Found gaming dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.gamingDimension.get(), serverLevel);
          gamingLevel = serverLevel;
          DataPackHandler.prepareDataPackOnce(gamingLevel);
        }
      } else if (Boolean.TRUE.equals(COMMON.voidEnabled.get())
          && dimensionLocation.equals(COMMON.voidDimension.get())) {
        if (voidLevel == null) {
          log.info("{} ✔️ Found void dimension with name {}: {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.voidDimension.get(), serverLevel);
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
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.defaultDimension.get());
    }
    if (fishingLevel == null && COMMON.fishingEnabled.get()) {
      log.error("{} ⚠️ Unable to find fishing dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.fishingDimension.get());
    }
    if (gamingLevel == null && COMMON.gamingEnabled.get()) {
      log.error("{} ⚠️ Unable to find gaming dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.gamingDimension.get());
    }
    if (lobbyLevel == null && COMMON.lobbyEnabled.get()) {
      log.error("{} ⚠️ Unable to find lobby dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.lobbyDimension.get());
    }
    if (miningLevel == null && COMMON.miningEnabled.get()) {
      log.error("{} ⚠️ Unable to find mining dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.miningDimension.get());
    }
    if (voidLevel == null && COMMON.voidEnabled.get()) {
      log.error("{} ⚠️ Unable to find void dimension named {}!",
          Constants.LOG_DIMENSION_MANAGER_PREFIX, COMMON.voidDimension.get());
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
    return COMMON.lobbyDimension.get();
  }


  public static ServerLevel getFishingDimension() {
    if (fishingLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return fishingLevel;
  }

  public static String getFishingDimensionName() {
    return COMMON.fishingDimension.get();
  }

  public static ServerLevel getGamingDimension() {
    if (gamingLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return gamingLevel;
  }

  public static String getGamingDimensionName() {
    return COMMON.gamingDimension.get();
  }

  public static ServerLevel getMiningDimension() {
    if (miningLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return miningLevel;
  }

  public static String getMiningDimensionName() {
    return COMMON.miningDimension.get();
  }


  public static ServerLevel getDefaultDimension() {
    if (defaultLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return defaultLevel;
  }

  public static String getDefaultDimensionName() {
    return COMMON.defaultDimension.get();
  }

  public static ServerLevel getVoidDimension() {
    if (voidLevel == null) {
      mapServerLevel(ServerLifecycleHooks.getCurrentServer());
    }
    return voidLevel;
  }

  public static String getVoidDimensionName() {
    return COMMON.voidDimension.get();
  }

  public static void teleportToDefault(ServerPlayer player) {
    if (TeleporterManager.teleportToDefaultDimension(player)) {
      changeGameType(player, GameType.SURVIVAL);
    }
  }

  public static void teleportToFishing(ServerPlayer player) {
    if (TeleporterManager.teleportToFishingDimension(player)) {
      if (!COMMON.fishingBuilderList.get().isEmpty()
          && COMMON.fishingBuilderList.get().contains(player.getName().getString())) {
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
      if (!COMMON.gamingBuilderList.get().isEmpty()
          && COMMON.gamingBuilderList.get().contains(player.getName().getString())) {
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
      if (!COMMON.lobbyBuilderList.get().isEmpty()
          && COMMON.lobbyBuilderList.get().contains(player.getName().getString())) {
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
      if (!COMMON.voidBuilderList.get().isEmpty()
          && COMMON.voidBuilderList.get().contains(player.getName().getString())) {
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
    if (Boolean.TRUE.equals(COMMON.fishingDisableMobSpawning.get())
        && COMMON.fishingDimension.get().equals(dimensionLocation)) {
      handleSpawnEventFishing(event);
    } else if (Boolean.TRUE.equals(COMMON.lobbyDisableMobSpawning.get())
        && COMMON.lobbyDimension.get().equals(dimensionLocation)) {
      handleSpawnEventLobby(event);
    } else if (Boolean.TRUE.equals(COMMON.gamingDisableMobSpawning.get())
        && COMMON.gamingDimension.get().equals(dimensionLocation)) {
      handleSpawnEventGaming(event);
    } else if ((COMMON.miningDisableBatSpawning.get()
        || COMMON.miningDisableMinecartChestSpawning.get() || COMMON.miningDisableMobSpawning.get())
        && COMMON.miningDimension.get().equals(dimensionLocation)) {
      handleSpawnEventMining(level, entity, event);
    } else if (Boolean.TRUE.equals(COMMON.voidDisableMobSpawning.get())
        && COMMON.voidDimension.get().equals(dimensionLocation)) {
      handleSpawnEventVoid(event);
    }
  }

  private static void handleSpawnEventFishing(LivingSpawnEvent event) {
    if (Boolean.TRUE.equals(COMMON.fishingDisableMobSpawning.get())) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventGaming(LivingSpawnEvent event) {
    if (Boolean.TRUE.equals(COMMON.gamingDisableMobSpawning.get())) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventLobby(LivingSpawnEvent event) {
    if (Boolean.TRUE.equals(COMMON.lobbyDisableMobSpawning.get())) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventMining(LevelAccessor level, Entity entity,
      LivingSpawnEvent event) {
    // Removing spawners as soon they try to spawn something.
    if (COMMON.miningRemoveSpawner.get() && event instanceof LivingSpawnEvent.CheckSpawn checkSpawn
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
      if (Boolean.TRUE.equals(COMMON.miningDisableBatSpawning.get())) {
        event.setResult(Event.Result.DENY);
      }
      return;
    }

    // Allow/deny Minecart Chest spawning
    if (entity instanceof MinecartChest) {
      if (Boolean.TRUE.equals(COMMON.miningDisableMinecartChestSpawning.get())) {
        event.setResult(Event.Result.DENY);
      }
      return;
    }

    if (Boolean.TRUE.equals(COMMON.miningDisableMobSpawning.get())) {
      event.setResult(Event.Result.DENY);
    }
  }

  private static void handleSpawnEventVoid(LivingSpawnEvent event) {
    if (Boolean.TRUE.equals(COMMON.voidDisableMobSpawning.get())) {
      event.setResult(Event.Result.DENY);
    }
  }

}
