// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.mobileBlocks;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.math.geom.Vector3i;

@Replicate
@ForceBlockActive
public class MovingBlockComponent implements Component {
    private Block blockToRender;
    private Vector3i locationFrom;
    private Vector3i locationTo;
    private long timeStart;
    private long timeEnd;

    public MovingBlockComponent() {
    }

    public MovingBlockComponent(Block blockToRender, Vector3i locationFrom, Vector3i locationTo, long timeStart,
                                long timeEnd) {
        this.blockToRender = blockToRender;
        this.locationFrom = locationFrom;
        this.locationTo = locationTo;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public Block getBlockToRender() {
        return blockToRender;
    }

    public Vector3i getLocationFrom() {
        return locationFrom;
    }

    public Vector3i getLocationTo() {
        return locationTo;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }
}
