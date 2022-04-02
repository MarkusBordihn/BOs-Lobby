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

package de.markusbordihn.lobby.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

import de.markusbordihn.lobby.Constants;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public final class CommonConfig {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private CommonConfig() {}

  public static final ForgeConfigSpec commonSpec;
  public static final Config COMMON;

  static {
    com.electronwill.nightconfig.core.Config.setInsertionOrderPreserved(true);
    final Pair<Config, ForgeConfigSpec> specPair =
        new ForgeConfigSpec.Builder().configure(Config::new);
    commonSpec = specPair.getRight();
    COMMON = specPair.getLeft();
    log.info("Registering {} common config ...", Constants.MOD_NAME);
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, commonSpec);
  }

  public static class Config {

    public final ForgeConfigSpec.IntValue generalCommandCoolDown;
    public final ForgeConfigSpec.BooleanValue generalDefaultToLobby;
    public final ForgeConfigSpec.BooleanValue generalDefaultToLobbyAlways;

    public final ForgeConfigSpec.ConfigValue<String> defaultDimension;
    public final ForgeConfigSpec.BooleanValue defaultRestrictCommand;
    public final ForgeConfigSpec.BooleanValue defaultUseCustomSpawnPoint;
    public final ForgeConfigSpec.IntValue defaultFireProtection;
    public final ForgeConfigSpec.IntValue defaultFallProtection;
    public final ForgeConfigSpec.IntValue defaultHeal;
    public final ForgeConfigSpec.IntValue defaultSpawnPointX;
    public final ForgeConfigSpec.IntValue defaultSpawnPointY;
    public final ForgeConfigSpec.IntValue defaultSpawnPointZ;

    public final ForgeConfigSpec.ConfigValue<String> lobbyDimension;
    public final ForgeConfigSpec.BooleanValue lobbyRestrictCommand;
    public final ForgeConfigSpec.BooleanValue lobbyDisableMobSpawning;
    public final ForgeConfigSpec.BooleanValue lobbyUseCustomSpawnPoint;
    public final ForgeConfigSpec.IntValue lobbySpawnPointX;
    public final ForgeConfigSpec.IntValue lobbySpawnPointY;
    public final ForgeConfigSpec.IntValue lobbySpawnPointZ;
    public final ForgeConfigSpec.ConfigValue<List<String>> lobbyBuilderList;

    public final ForgeConfigSpec.ConfigValue<String> miningDimension;
    public final ForgeConfigSpec.BooleanValue miningRestrictCommand;
    public final ForgeConfigSpec.BooleanValue miningDisableBatSpawning;
    public final ForgeConfigSpec.BooleanValue miningDisableMobSpawning;
    public final ForgeConfigSpec.BooleanValue miningDisableMinecartChestSpawning;
    public final ForgeConfigSpec.BooleanValue miningRemoveSpawner;
    public final ForgeConfigSpec.BooleanValue miningUseCustomSpawnPoint;
    public final ForgeConfigSpec.IntValue miningSpawnPointX;
    public final ForgeConfigSpec.IntValue miningSpawnPointY;
    public final ForgeConfigSpec.IntValue miningSpawnPointZ;

    public final ForgeConfigSpec.ConfigValue<String> fishingDimension;
    public final ForgeConfigSpec.BooleanValue fishingRestrictCommand;
    public final ForgeConfigSpec.BooleanValue fishingDisableMobSpawning;
    public final ForgeConfigSpec.BooleanValue fishingUseCustomSpawnPoint;
    public final ForgeConfigSpec.IntValue fishingSpawnPointX;
    public final ForgeConfigSpec.IntValue fishingSpawnPointY;
    public final ForgeConfigSpec.IntValue fishingSpawnPointZ;
    public final ForgeConfigSpec.ConfigValue<List<String>> fishingBuilderList;

    Config(ForgeConfigSpec.Builder builder) {
      builder.comment(Constants.MOD_NAME);

      builder.push("Commands");
      generalCommandCoolDown =
          builder.comment("Delay in seconds before a teleport command could be used again!")
              .defineInRange("generalCommandCoolDown", 30, 1, 300);
      generalDefaultToLobby = builder.comment(
          "Only teleport player to the lobby for their first connect or after a server restart!")
          .define("generalDefaultToLobby", true);
      generalDefaultToLobbyAlways = builder.comment("Always teleport player to the lobby!")
          .define("generalDefaultToLobbyAlways", false);
      builder.pop();

      builder.push("Commands");
      defaultRestrictCommand = builder.comment(
          "If enabled the teleport command could not be used if the user is already in the default dimension.")
          .define("defaultRestrictCommand", false);
      lobbyRestrictCommand = builder.comment(
          "If enabled the teleport command could not be used if the user is already in the lobby dimension.")
          .define("lobbyRestrictCommand", false);
      miningRestrictCommand = builder.comment(
          "If enabled the teleport command could not be used if the user is already in the mining dimension.")
          .define("miningRestrictCommand", false);
      fishingRestrictCommand = builder.comment(
          "If enabled the teleport command could not be used if the user is already in the fishing dimension.")
          .define("fishingRestrictCommand", false);
      builder.pop();

      builder.push("Default Dimension");
      defaultDimension = builder.define("defaultDimension", "minecraft:overworld");
      defaultUseCustomSpawnPoint = builder.define("defaultUseCustomSpawnPoint", false);
      defaultFallProtection =
          builder.comment("Defines the amount of ticks how long the fall protection is enabled.")
              .defineInRange("defaultFallProtection", 400, 0, 1200);
      defaultFireProtection =
          builder.comment("Defines the amount of ticks how long the fire protection is enabled.")
              .defineInRange("defaultFireProtection", 400, 0, 1200);
      defaultHeal =
          builder.comment("Defines the amount of ticks how long the heal is enabled.")
              .defineInRange("defaultHeal", 0, 0, 1200);
      defaultSpawnPointX = builder.defineInRange("defaultSpawnPointX", 68, -1000, 1000);
      defaultSpawnPointY = builder.defineInRange("defaultSpawnPointY", 65, -1000, 1000);
      defaultSpawnPointZ = builder.defineInRange("defaultSpawnPointZ", -89, -1000, 1000);
      builder.pop();

      builder.push("Lobby Dimension");
      lobbyDimension = builder.define("lobbyDimension", "lobby:lobby_dimension");
      lobbyDisableMobSpawning = builder.define("lobbyDisableMobSpawning", true);
      lobbyUseCustomSpawnPoint = builder.define("lobbyUseCustomSpawnPoint", false);
      lobbySpawnPointX = builder.defineInRange("lobbySpawnPointX", 9, -1000, 1000);
      lobbySpawnPointY = builder.defineInRange("lobbySpawnPointY", 9, -1000, 1000);
      lobbySpawnPointZ = builder.defineInRange("lobbySpawnPointZ", 9, -1000, 1000);
      lobbyBuilderList = builder.comment(
          "List of builders which are automatically switched to the creative mode inside the lobby dimension.")
          .define("lobbyBuilderList", new ArrayList<String>(Arrays.asList("")));
      builder.pop();

      builder.push("Mining Dimension");
      miningDimension = builder.define("miningDimension", "lobby:mining_dimension");
      miningDisableMobSpawning = builder.define("miningDisableMobSpawning", true);
      miningDisableBatSpawning = builder.define("miningDisableBatSpawning", true);
      miningDisableMinecartChestSpawning =
          builder.define("miningDisableMinecartChestSpawning", true);
      miningRemoveSpawner = builder.define("miningRemoveSpawner", true);
      miningUseCustomSpawnPoint = builder.define("miningUseCustomSpawnPoint", false);
      miningSpawnPointX = builder.defineInRange("miningSpawnPointX", 200, -1000, 1000);
      miningSpawnPointY = builder.defineInRange("miningSpawnPointY", 11, -1000, 1000);
      miningSpawnPointZ = builder.defineInRange("miningSpawnPointZ", 558, -1000, 1000);
      builder.pop();

      builder.push("Fishing Dimension");
      fishingDimension = builder.define("fishingDimension", "lobby:fishing_dimension");
      fishingDisableMobSpawning = builder.define("fishingDisableMobSpawning", true);
      fishingUseCustomSpawnPoint = builder.define("fishingUseCustomSpawnPoint", false);
      fishingSpawnPointX = builder.defineInRange("fishingSpawnPointX", 42, -1000, 1000);
      fishingSpawnPointY = builder.defineInRange("fishingSpawnPointY", 51, -1000, 1000);
      fishingSpawnPointZ = builder.defineInRange("fishingSpawnPointZ", 12, -1000, 1000);
      fishingBuilderList = builder.comment(
          "List of builders which are automatically switched to the creative mode inside the fishing dimension.")
          .define("fishingBuilderList", new ArrayList<String>(Arrays.asList("")));
      builder.pop();

    }
  }

}
