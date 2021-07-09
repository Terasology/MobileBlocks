// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.mobileBlocks;

import org.joml.Vector3i;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

@Replicate
@ForceBlockActive
public class MovingBlockComponent implements Component<MovingBlockComponent> {
    private Block blockToRender;
    private Vector3i locationFrom;
    private Vector3i locationTo;
    private long timeStart;
    private long timeEnd;

    public MovingBlockComponent() {
    }

    public MovingBlockComponent(Block blockToRender, Vector3i locationFrom, Vector3i locationTo, long timeStart, long timeEnd) {
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

    @Override
    public void copy(MovingBlockComponent other) {
        this.blockToRender = other.blockToRender;
        this.locationFrom = other.locationFrom;
        this.locationTo = other.locationTo;
        this.timeStart = other.timeStart;
        this.timeEnd = other.timeEnd;
    }
}
