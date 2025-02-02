/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.aws.traits.apigateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.KnowledgeIndex;
import software.amazon.smithy.model.shapes.EntityShape;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeIndex;
import software.amazon.smithy.model.shapes.ToShapeId;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.utils.MapUtils;

/**
 * Computes the API Gateway integration for each operation,
 * resource, and service shape in a model.
 */
public class IntegrationTraitIndex implements KnowledgeIndex {
    private Map<ShapeId, Map<ShapeId, Trait>> traits = new HashMap<>();

    public IntegrationTraitIndex(Model model) {
        model.getShapeIndex().shapes(ServiceShape.class).forEach(service -> {
            Map<ShapeId, Trait> serviceMap = new HashMap<>();
            traits.put(service.getId(), serviceMap);
            walk(model.getShapeIndex(), service.getId(), service, null);
        });
    }

    /**
     * Get the integration trait for a particular operation, resource, or
     * service bound within a service.
     *
     * @param service Service shape or ShapeId thereof.
     * @param shape Operation, service, or resource shape in the service.
     *  When the service shape ID is provided, the integration trait of the
     *  service is returned if present.
     *
     * @return The integration trait or an empty optional if none set
     */
    public Optional<Trait> getIntegrationTrait(ToShapeId service, ToShapeId shape) {
        return Optional.ofNullable(traits.getOrDefault(service.toShapeId(), MapUtils.of())
                .get(shape.toShapeId()));
    }

    /**
     * Get the integration trait for a particular operation, resource, or
     * service bound within a service of a specific type.
     *
     * @param service Service shape or ShapeId thereof.
     * @param shape Operation, service, or resource shape in the service.
     *  When the service shape ID is provided, the integration trait of the
     *  service is returned if present.
     * @param type Integration trait type.
     * @param <T> Type of trait to retrieve.
     *
     * @return The integration trait or an empty optional if none set or
     *  if not of the expected type.
     */
    public <T extends Trait> Optional<T> getIntegrationTrait(ToShapeId service, ToShapeId shape, Class<T> type) {
        return getIntegrationTrait(service, shape).filter(type::isInstance).map(type::cast);
    }

    private void walk(ShapeIndex index, ShapeId service, EntityShape current, Trait trait) {
        Trait updatedTrait = extractTrait(current, trait);
        Map<ShapeId, Trait> serviceMapping = traits.get(service);
        serviceMapping.put(current.getId(), updatedTrait);

        for (ShapeId resource : current.getResources()) {
            index.getShape(resource)
                    .flatMap(Shape::asResourceShape)
                    .ifPresent(resourceShape -> walk(index, service, resourceShape, updatedTrait));
        }

        for (ShapeId operation : current.getAllOperations()) {
            index.getShape(operation).ifPresent(op -> serviceMapping.put(operation, extractTrait(op, updatedTrait)));
        }
    }

    private static Trait extractTrait(Shape shape, Trait defaultValue) {
        return shape.getTrait(MockIntegrationTrait.class)
                .map(trait -> (Trait) trait)
                .orElseGet(() -> shape.getTrait(IntegrationTrait.class)
                        .map(trait -> (Trait) trait)
                        .orElse(defaultValue));
    }
}
