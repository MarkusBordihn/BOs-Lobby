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

import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.util.ITeleporter;

public class DimensionTeleporter implements ITeleporter {

  private int x;
  private int y;
  private int z;
  private boolean hasCoordinates = false;

  public DimensionTeleporter() {
    super();
  }

  public DimensionTeleporter(int x, int y, int z) {
    super();
    this.x = x;
    this.y = y;
    this.z = z;
    this.hasCoordinates = true;
  }

  @Override
  public Entity placeEntity(Entity entity, ServerLevel currentLevel, ServerLevel targetLevel,
      float yaw, Function<Boolean, Entity> repositionEntity) {
    return repositionEntity.apply(false);
  }

  @Override
  public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld,
      Function<ServerLevel, PortalInfo> defaultPortalInfo) {
    BlockPos spawnPoint = destWorld.getSharedSpawnPos();
    if (this.hasCoordinates && entity.isAlive()) {
      entity.setPos(this.x, this.y, this.z);
    } else if (spawnPoint != null) {
      entity.setPos(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
    }
    return this.isVanilla() ? defaultPortalInfo.apply(destWorld)
        : new PortalInfo(entity.position(), Vec3.ZERO, entity.getYRot(), entity.getXRot());
  }
}
