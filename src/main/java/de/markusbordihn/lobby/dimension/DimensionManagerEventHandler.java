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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;

import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import de.markusbordihn.lobby.Constants;
import de.markusbordihn.lobby.config.CommonConfig;

@EventBusSubscriber
public class DimensionManagerEventHandler {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final CommonConfig.Config COMMON = CommonConfig.COMMON;

  protected DimensionManagerEventHandler() {}

  @SubscribeEvent
  public static void handleServerStartedEvent(ServerStartedEvent event) {
    if (Boolean.TRUE.equals(COMMON.fishingDisableMobSpawning.get())) {
      log.info("{} Disable Mob Spawning for fishing dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (Boolean.TRUE.equals(COMMON.lobbyDisableMobSpawning.get())) {
      log.info("{} Disable mob spawning for lobby dimension.",
          Constants.LOG_DIMENSION_MANAGER_PREFIX);
    }
    if (Boolean.TRUE.equals(COMMON.miningDisableMobSpawning.get())) {
      log.info("{} Disable mob spawning for mining dimension.",
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
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleEntityJoinLevelEvent(EntityJoinLevelEvent event) {

    // Ignore client side.
    Level level = event.getLevel();
    if (level.isClientSide()) {
      return;
    }

    // Ignore everything which is not the mining dimension.
    String dimensionLocation = level.dimension().location().toString();
    if (!dimensionLocation.equals(COMMON.miningDimension.get())) {
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

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleChunkLoadEvent(ChunkEvent.Load event) {

    // Ignore client side.
    LevelAccessor level = event.getLevel();
    if (level.isClientSide()) {
      return;
    }

    // Ignore everything which is not the mining dimension.
    DimensionType dimensionType = event.getLevel().dimensionType();
    if (DimensionManager.getMiningDimensionRaw() == null
        || !DimensionManager.getMiningDimensionRaw().dimensionType().equals(dimensionType)) {
      return;
    }

    ChunkAccess chunk = event.getChunk();
    Set<BlockPos> blockEntitiesPos = chunk.getBlockEntitiesPos();
    if (!blockEntitiesPos.isEmpty()) {
      for (BlockPos blockPos : blockEntitiesPos) {
        BlockEntity blockEntity = chunk.getBlockEntity(blockPos);

        // Remove spawners.
        if (COMMON.miningRemoveSpawner.get()
            && blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity) {
          log.debug("{} Removing spawner block entity {} at {}",
              Constants.LOG_DIMENSION_MANAGER_PREFIX, spawnerBlockEntity, blockPos);
          chunk.removeBlockEntity(blockPos);
        }

        // Remove loot chests to avoid easy getting of items in the mining dimension.
        else if (Boolean.TRUE.equals(COMMON.miningRemoveLootChest.get())
            && (blockEntity instanceof ChestBlockEntity
                || blockEntity instanceof BarrelBlockEntity)) {
          CompoundTag compoundTagSaving = chunk.getBlockEntityNbtForSaving(blockPos);
          if (compoundTagSaving != null && compoundTagSaving.contains("LootTable")
              && !compoundTagSaving.getString("LootTable").isEmpty()) {
            log.debug("{} Removing loot chest block entity {} at {}",
                Constants.LOG_DIMENSION_MANAGER_PREFIX, blockEntity, blockPos);
            chunk.removeBlockEntity(blockPos);
          }
        }

      }
    }
  }

  private static void handleSpawnEvent(LivingSpawnEvent event) {

    // Ignore client side.
    LevelAccessor level = event.getLevel();
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
    if (COMMON.fishingDimension.get().equals(dimensionLocation)) {
      handleSpawnEventFishing(event);
    } else if (COMMON.lobbyDimension.get().equals(dimensionLocation)) {
      handleSpawnEventLobby(event);
    } else if (COMMON.gamingDimension.get().equals(dimensionLocation)) {
      handleSpawnEventGaming(event);
    } else if (COMMON.miningDimension.get().equals(dimensionLocation)) {
      handleSpawnEventMining(level, entity, event);
    } else if (COMMON.voidDimension.get().equals(dimensionLocation)) {
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
    if (COMMON.miningRemoveSpawner.get()
        && event instanceof LivingSpawnEvent.CheckSpawn checkSpawn) {
      BaseSpawner spawner = checkSpawn.getSpawner();
      if (spawner != null) {
        BlockEntity blockEntity = spawner.getSpawnerBlockEntity();
        if (blockEntity != null) {
          BlockPos blockPos = blockEntity.getBlockPos();
          if (blockPos != null) {
            log.debug("{} Removing spawner {} at {}", Constants.LOG_DIMENSION_MANAGER_PREFIX,
                spawner, blockPos);
            level.removeBlock(blockPos, true);
          }
        }
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
