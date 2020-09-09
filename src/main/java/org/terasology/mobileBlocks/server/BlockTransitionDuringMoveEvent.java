// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.mobileBlocks.server;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class BlockTransitionDuringMoveEvent implements Event {
    private final boolean fromBlock;
    private final EntityRef intoEntity;

    public BlockTransitionDuringMoveEvent(boolean fromBlock, EntityRef intoEntity) {
        this.fromBlock = fromBlock;
        this.intoEntity = intoEntity;
    }

    public boolean isFromBlock() {
        return fromBlock;
    }

    public EntityRef getIntoEntity() {
        return intoEntity;
    }
}
