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

import java.util.List;
import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.utils.Tagged;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Applies tags to a shape.
 */
public final class TagsTrait extends StringListTrait implements ToSmithyBuilder<TagsTrait>, Tagged {
    public static final String NAME = "smithy.api#tags";

    private TagsTrait(List<String> values, FromSourceLocation sourceLocation) {
        super(NAME, values, sourceLocation);
    }

    public static final class Provider extends StringListTrait.Provider<TagsTrait> {
        public Provider() {
            super(NAME, TagsTrait::new);
        }
    }

    @Override
    public Builder toBuilder() {
        return builder().values(getValues());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends StringListTrait.Builder<TagsTrait, Builder> {
        private Builder() {}

        @Override
        public TagsTrait build() {
            return new TagsTrait(getValues(), getSourceLocation());
        }
    }
}
