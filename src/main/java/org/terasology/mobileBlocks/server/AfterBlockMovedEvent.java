// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.mobileBlocks.server;

import org.terasology.engine.entitySystem.event.Event;

public class AfterBlockMovedEvent implements Event {
    private final boolean success;

    public AfterBlockMovedEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
