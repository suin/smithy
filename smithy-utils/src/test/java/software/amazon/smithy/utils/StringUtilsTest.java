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

package software.amazon.smithy.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class StringUtilsTest {
    @Test
    public void uppercaseFirst() {
        assertThat(StringUtils.capitalize("foo"), equalTo("Foo"));
        assertThat(StringUtils.capitalize(" foo"), equalTo(" foo"));
        assertThat(StringUtils.capitalize("10-foo"), equalTo("10-foo"));
        assertThat(StringUtils.capitalize("_foo"), equalTo("_foo"));
    }

    @Test
    public void lowercaseFirst() {
        assertThat(StringUtils.uncapitalize("foo"), equalTo("foo"));
        assertThat(StringUtils.uncapitalize(" foo"), equalTo(" foo"));
        assertThat(StringUtils.uncapitalize("10-foo"), equalTo("10-foo"));
        assertThat(StringUtils.uncapitalize("_foo"), equalTo("_foo"));
        assertThat(StringUtils.uncapitalize("Foo"), equalTo("foo"));
        assertThat(StringUtils.uncapitalize(" Foo"), equalTo(" Foo"));
        assertThat(StringUtils.uncapitalize("10-Foo"), equalTo("10-Foo"));
        assertThat(StringUtils.uncapitalize("_Foo"), equalTo("_Foo"));
    }

    @Test
    public void capitalizesAndUncapitalizes() {
        assertThat(StringUtils.capitalize(null), equalTo(null));
        assertThat(StringUtils.uncapitalize(null), equalTo(null));

        assertThat(StringUtils.capitalize(""), equalTo(""));
        assertThat(StringUtils.uncapitalize(""), equalTo(""));

        assertThat(StringUtils.capitalize("Foo"), equalTo("Foo"));
        assertThat(StringUtils.uncapitalize("Foo"), equalTo("foo"));

        assertThat(StringUtils.capitalize("foo"), equalTo("Foo"));
        assertThat(StringUtils.uncapitalize("foo"), equalTo("foo"));
    }

    @Test
    public void wrapsText() {
        assertThat(StringUtils.wrap("hello, there, bud", 6), equalTo(String.format("hello,%nthere,%nbud")));
    }
}
