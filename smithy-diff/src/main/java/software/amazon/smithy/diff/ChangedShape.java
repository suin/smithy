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

package software.amazon.smithy.diff;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.utils.Pair;

/**
 * Represents a changed shape.
 *
 * @param <S> The type of shape. Note that this may be just {@link Shape}
 *   in the event that the shape changed classes.
 */
public final class ChangedShape<S extends Shape> implements FromSourceLocation {
    private final S oldShape;
    private final S newShape;
    private final Map<String, Pair<Trait, Trait>> traitDiff;

    ChangedShape(S oldShape, S newShape) {
        this.oldShape = oldShape;
        this.newShape = newShape;
        traitDiff = Collections.unmodifiableMap(findTraitDifferences(oldShape, newShape));
    }

    /**
     * Gets the old shape value.
     *
     * @return Returns the old shape.
     */
    public S getOldShape() {
        return oldShape;
    }

    /**
     * Gets the new shape value.
     *
     * @return Returns the new shape.
     */
    public S getNewShape() {
        return newShape;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return getNewShape().getSourceLocation();
    }

    /**
     * Gets the shape ID of the changed shape.
     *
     * @return Return the shape ID.
     */
    public ShapeId getShapeId() {
        return newShape.getId();
    }

    /**
     * Gets a stream of added traits.
     *
     * @return Returns the traits that were added.
     */
    public Stream<Trait> addedTraits() {
        return traitDiff.values().stream()
                .filter(pair -> pair.getLeft() == null)
                .map(Pair::getRight);
    }

    /**
     * Gets a stream of removed traits.
     *
     * @return Returns the traits that were removed.
     */
    public Stream<Trait> removedTraits() {
        return traitDiff.values().stream()
                .filter(pair -> pair.getRight() == null)
                .map(Pair::getLeft);
    }

    /**
     * Gets a stream of changed traits.
     *
     * @return Returns the traits that were changed.
     */
    public Stream<Pair<Trait, Trait>> changedTraits() {
        return traitDiff.values().stream().filter(pair -> pair.getLeft() != null && pair.getRight() != null);
    }

    /**
     * Gets a changed trait of a specific type.
     *
     * @param traitType Type of trait to find.
     * @param <T> Type of trait to find.
     * @return Returns the optionally found old and new typed trait values.
     */
    @SuppressWarnings("unchecked")
    public <T extends Trait> Optional<Pair<T, T>> getChangedTrait(Class<T> traitType) {
        return changedTraits()
                .filter(p -> traitType.isInstance(p.getLeft()) && traitType.isInstance(p.getRight()))
                .map(p -> (Pair<T, T>) p)
                .findFirst();
    }

    /**
     * Gets the trait differences between the old and new shape.
     *
     * <p>The returned map is a mapping of a trait name to a pair in which the
     * left side of the pair contains the nullable old trait value, and the
     * right side of the pair contains the nullable new trait value. The left
     * side will be null if the trait was added, the right side will be null
     * if the trait was removed, and both traits will be present if the trait
     * changed.
     *
     * @return Returns a map of each changed trait name to a pair of the old and new trait values.
     */
    public Map<String, Pair<Trait, Trait>> getTraitDifferences() {
        return traitDiff;
    }

    /**
     * Finds the trait differences between the old and new shape.
     *
     * @param oldShape Old shape.
     * @param newShape New shape.
     * @return Returns a map of each changed trait name to a pair of the old and new trait values.
     */
    private static Map<String, Pair<Trait, Trait>> findTraitDifferences(Shape oldShape, Shape newShape) {
        Map<String, Pair<Trait, Trait>> changes = new HashMap<>();
        for (Trait oldTrait : oldShape.getAllTraits().values()) {
            Trait newTrait = newShape.findTrait(oldTrait.getName()).orElse(null);
            if (newTrait == null) {
                changes.put(oldTrait.getName(), (Pair.of(oldTrait, null)));
            } else if (!newTrait.equals(oldTrait)) {
                changes.put(newTrait.getName(), Pair.of(oldTrait, newTrait));
            }
        }
        // Find traits that were added.
        newShape.getAllTraits().values().stream()
                .filter(newTrait -> !oldShape.findTrait(newTrait.getName()).isPresent())
                .forEach(newTrait -> changes.put(newTrait.getName(), Pair.of(null, newTrait)));

        return changes;
    }
}
