/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.mobileBlocks.server;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.event.BeforeDamagedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Direction;
import org.terasology.mobileBlocks.MovingBlockComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem
@Share(BlockMoveManager.class)
public class MovingBlockServerSystem extends BaseComponentSystem implements BlockMoveManager {
    public static final String EVENT_ID = "materializeBlock";
    @In
    private WorldProvider worldProvider;
    @In
    private Time time;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private DelayManager delayManager;

    @Override
    public boolean moveBlock(Vector3i location, Direction direction, long moveTime) {
        Vector3ic directionVector = direction.asVector3i();
        Vector3i moveLocation = new Vector3i(
                location.x + directionVector.x(),
                location.y + directionVector.y(),
                location.z + directionVector.z());

        Block blockAtLocation = worldProvider.getBlock(moveLocation);
        if (!blockAtLocation.isReplacementAllowed()) {
            return false;
        }

        long gameTime = time.getGameTimeInMs();

        Block invisibleBlock = blockManager.getBlock("MobileBlocks:MovingBlockReplacement");

        EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(location);

        blockEntity.send(new BeforeBlockMovesEvent());
        EntityRef endingEntity = blockEntity;

        Block block = worldProvider.getBlock(location);

        Prefab prefab = blockEntity.getParentPrefab();

        boolean success = false;
        EntityRef movingEntity = entityManager.create(prefab);
        try {
            movingEntity.addComponent(new LocationComponent(new Vector3f(location)));
            movingEntity.addComponent(new MovingBlockComponent(block, location, moveLocation, gameTime, gameTime + moveTime));
            blockEntity.send(new BlockTransitionDuringMoveEvent(true, movingEntity));

            Map<org.joml.Vector3i, Block> blocksToPlace = new HashMap<>();
            blocksToPlace.put(location, invisibleBlock);
            blocksToPlace.put(moveLocation, invisibleBlock);

            PlaceBlocks placeInvisibleBlocks = new PlaceBlocks(blocksToPlace, worldProvider.getWorldEntity());
            try {
                // Replace blocks with invisible ones
                worldProvider.getWorldEntity().send(placeInvisibleBlocks);
                if (placeInvisibleBlocks.isConsumed()) {
                    return false;
                }

                endingEntity = movingEntity;

                delayManager.addDelayedAction(movingEntity, EVENT_ID, moveTime);

                success = true;
                return true;
            } finally {
                endingEntity.send(new AfterBlockMovedEvent(success));
            }
        } finally {
            if (!success) {
                movingEntity.destroy();
            }
        }
    }

    @ReceiveEvent
    public void wakeUpToMaterializeBlock(DelayedActionTriggeredEvent event, EntityRef movingBlockEntity) {
        if (event.getActionId().equals(EVENT_ID)) {
            MovingBlockComponent movingBlock = movingBlockEntity.getComponent(MovingBlockComponent.class);
            Vector3i locationFrom = movingBlock.getLocationFrom();
            Vector3i locationTo = movingBlock.getLocationTo();

            Map<org.joml.Vector3i, Block> blocksToPlace = new HashMap<>();
            blocksToPlace.put(locationFrom, blockManager.getBlock(BlockManager.AIR_ID));
            blocksToPlace.put(locationTo, movingBlock.getBlockToRender());

            movingBlockEntity.send(new BeforeBlockMovesEvent());
            EntityRef endingEntity = movingBlockEntity;
            try {
                PlaceBlocks placeBlocks = new PlaceBlocks(blocksToPlace, worldProvider.getWorldEntity());
                worldProvider.getWorldEntity().send(placeBlocks);

                EntityRef newBlockEntity = blockEntityRegistry.getBlockEntityAt(locationTo);
                movingBlockEntity.send(new BlockTransitionDuringMoveEvent(false, newBlockEntity));
                endingEntity = newBlockEntity;
                movingBlockEntity.destroy();
            } finally {
                endingEntity.send(new AfterBlockMovedEvent(true));
            }
        }
    }

    @ReceiveEvent
    public void preventDestructionOfBlocksByOtherInstigators(PlaceBlocks placeBlocks, EntityRef world) {
        if (placeBlocks.getInstigator() != world) {
            for (Vector3ic location : placeBlocks.getBlocks().keySet()) {
                if (blockEntityRegistry.getBlockEntityAt(location).hasComponent(MovingBlockReplacementComponent.class)) {
                    placeBlocks.consume();
                    break;
                }
            }
        }
    }

    @ReceiveEvent
    public void preventDamagingOfBlocks(BeforeDamagedEvent event, EntityRef entity, MovingBlockReplacementComponent movingBlockReplacementComponent) {
        event.consume();
    }
}
