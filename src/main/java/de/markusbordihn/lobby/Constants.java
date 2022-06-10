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

package de.markusbordihn.lobby;

public final class Constants {

  protected Constants() {}

  // General Mod definitions
  public static final String LOG_NAME = "Lobby";
  public static final String LOG_REGISTER_PREFIX = "Register " + LOG_NAME;
  public static final String LOG_DIMENSION_MANAGER_PREFIX = "[Lobby Dimension Manager]";
  public static final String LOG_TELEPORT_MANAGER_PREFIX = "[Lobby Teleport Manager]";
  public static final String LOG_PLAYER_MANAGER_PREFIX = "[Lobby Player Manager]";
  public static final String MOD_COMMAND = "lobby";
  public static final String MOD_ID = "lobby";
  public static final String MOD_NAME = "Lobby";
  public static final String ISSUE_REPORT = "https://github.com/MarkusBordihn/BOs-Lobby/issues";

  // Prefixes
  public static final String TEXT_PREFIX = "text.lobby.";

  // Messages
  public static final String UNABLE_TO_TELEPORT_MESSAGE =
      Constants.TEXT_PREFIX + "unable_to_teleport";
  public static final String TELEPORT_TO_MESSAGE = Constants.TEXT_PREFIX + "teleport_to";
  public static final String TELEPORT_TO_IN_MESSAGE = Constants.TEXT_PREFIX + "teleport_to_in";
  public static final String TELEPORT_FAILED_ALREADY_IN_DIMENSION_MESSAGE =
      Constants.TEXT_PREFIX + "failed_already_in_dimension";
  public static final String TELEPORT_FAILED_COOLDOWN = Constants.TEXT_PREFIX + "failed_cooldown";
}
