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

package software.amazon.smithy.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeIndex;
import software.amazon.smithy.model.shapes.StringShape;
import software.amazon.smithy.model.traits.TraitDefinition;

public class ModelTest {

    @Test
    public void buildsModel() {
        ShapeIndex index = ShapeIndex.builder().build();
        TraitDefinition customTrait = TraitDefinition.builder()
                .name("ns.foo#baz")
                .build();
        Model model = Model.builder()
                .putMetadataProperty("name.name", Node.objectNode())
                .shapeIndex(index)
                .addTraitDefinition(customTrait)
                .smithyVersion(Model.MODEL_VERSION)
                .build();

        assertTrue(model.getMetadataProperty("name.name").isPresent());
        assertEquals(model.getSmithyVersion(), Model.MODEL_VERSION);
        assertThat(model.getTraitDefinitions(), hasSize(1));
        assertThat(model.getTraitDefinitions().iterator().next().getFullyQualifiedName(), equalTo("ns.foo#baz"));
    }

    @Test
    public void modelEquality() {
        Model modelA = Model.builder()
                .addTraitDefinition(TraitDefinition.builder()
                        .name("ns.foo#baz")
                        .build())
                .putMetadataProperty("foo", Node.from("baz"))
                .shapeIndex(ShapeIndex.builder()
                        .addShape(StringShape.builder().id("ns.foo#baz").build())
                        .build())
                .build();
        Model modelB = Model.builder()
                .shapeIndex(ShapeIndex.builder().build())
                .putMetadataProperty("foo", Node.from("baz"))
                .build();

        assertThat(modelA, equalTo(modelA));
        assertThat(modelA, not(equalTo(modelB)));
        assertThat(modelA, not(equalTo(null)));
    }
}
