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

package software.amazon.smithy.model.traits;

import java.math.BigDecimal;
import java.util.Optional;
import software.amazon.smithy.model.SourceException;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.utils.MapUtils;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Constrains a shape to minimum and maximum numeric range.
 */
public final class RangeTrait extends AbstractTrait implements ToSmithyBuilder<RangeTrait> {
    public static final String NAME = "smithy.api#range";

    private final BigDecimal min;
    private final BigDecimal max;

    private RangeTrait(Builder builder) {
        super(NAME, builder.sourceLocation);
        this.min = builder.min;
        this.max = builder.max;
        if (max == null && min == null) {
            throw new SourceException("One of 'min' or 'max' must be provided.", getSourceLocation());
        }
    }

    /**
     * Gets the min value.
     *
     * @return returns the optional min value.
     */
    public Optional<BigDecimal> getMin() {
        return Optional.ofNullable(min);
    }

    /**
     * Gets the max value.
     *
     * @return returns the optional max value.
     */
    public Optional<BigDecimal> getMax() {
        return Optional.ofNullable(max);
    }

    @Override
    protected Node createNode() {
        return new ObjectNode(MapUtils.of(), getSourceLocation())
                .withOptionalMember("min", getMin().map(Node::from))
                .withOptionalMember("max", getMax().map(Node::from));
    }

    @Override
    public Builder toBuilder() {
        return builder().min(min).max(max).sourceLocation(getSourceLocation());
    }

    /**
     * @return Returns a new RangeTrait builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder used to create a LongTrait.
     */
    public static final class Builder extends AbstractTraitBuilder<RangeTrait, Builder> {
        private BigDecimal min;
        private BigDecimal max;

        public Builder min(BigDecimal min) {
            this.min = min;
            return this;
        }

        public Builder max(BigDecimal max) {
            this.max = max;
            return this;
        }

        @Override
        public RangeTrait build() {
            return new RangeTrait(this);
        }
    }

    public static final class Provider implements TraitService {
        @Override
        public String getTraitName() {
            return NAME;
        }

        @Override
        public RangeTrait createTrait(ShapeId target, Node value) {
            ObjectNode objectNode = value.expectObjectNode();
            BigDecimal minValue = objectNode.getMember("min")
                    .map(node -> new BigDecimal(node.expectNumberNode().getValue().toString())).orElse(null);
            BigDecimal maxValue = objectNode.getMember("max")
                    .map(node -> new BigDecimal(node.expectNumberNode().getValue().toString())).orElse(null);
            return builder().sourceLocation(value).min(minValue).max(maxValue).build();
        }
    }
}
