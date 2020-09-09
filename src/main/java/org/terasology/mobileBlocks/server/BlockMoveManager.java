// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.mobileBlocks.server;

import org.terasology.engine.math.Direction;
import org.terasology.math.geom.Vector3i;

public interface BlockMoveManager {
    boolean moveBlock(Vector3i location, Direction direction, long time);
}
