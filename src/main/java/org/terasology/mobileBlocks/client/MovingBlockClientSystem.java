// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.mobileBlocks.client;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.mobileBlocks.MovingBlockComponent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.nui.Color;
import org.terasology.world.block.Block;

@RegisterSystem(RegisterMode.CLIENT)
public class MovingBlockClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(MovingBlockClientSystem.class);

    @In
    private EntityManager entityManager;
    @In
    private Time time;

    @Override
    public void update(float delta) {
        long gameTimeInMs = time.getGameTimeInMs();
        for (EntityRef entityRef : entityManager.getEntitiesWith(MovingBlockComponent.class, LocationComponent.class, MeshComponent.class)) {
            MovingBlockComponent movingBlock = entityRef.getComponent(MovingBlockComponent.class);

            Vector3i locationFrom = movingBlock.getLocationFrom();
            Vector3i locationTo = movingBlock.getLocationTo();

            long timeStart = movingBlock.getTimeStart();
            long timeEnd = movingBlock.getTimeEnd();

            Vector3f result;
            if (gameTimeInMs <= timeStart) {
                result = new Vector3f(locationFrom);
            } else if (gameTimeInMs >= timeEnd) {
                result = new Vector3f(locationTo);
            } else {
                float resultDiff = 1f * (gameTimeInMs - timeStart) / (timeEnd - timeStart);
                result = new Vector3f(
                        locationFrom.x + resultDiff * (locationTo.x - locationFrom.x),
                        locationFrom.y + resultDiff * (locationTo.y - locationFrom.y),
                        locationFrom.z + resultDiff * (locationTo.z - locationFrom.z));
            }

            LocationComponent location = entityRef.getComponent(LocationComponent.class);
            location.setWorldPosition(result);
            entityRef.saveComponent(location);
        }
    }

    @ReceiveEvent
    public void movingStarted(OnActivatedComponent event, EntityRef entity, MovingBlockComponent movingBlock, LocationComponent locationComponent) {
        Block blockToRender = movingBlock.getBlockToRender();
        Mesh mesh = blockToRender.getMeshGenerator().getStandaloneMesh();

        MeshComponent meshComponent = new MeshComponent();
        meshComponent.mesh = mesh;
        meshComponent.material = Assets.getMaterial("engine:terrain").get();
        meshComponent.translucent = false;
        meshComponent.hideFromOwner = false;
        meshComponent.color = new Color(Color.white);

        entity.addComponent(meshComponent);
    }

    @ReceiveEvent
    public void beforeMovingFinished(BeforeDeactivateComponent event, EntityRef entity, MovingBlockComponent movingBlock, LocationComponent locatinComponent) {
        MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
        meshComponent.mesh.dispose();
        entity.removeComponent(MeshComponent.class);
    }
}
