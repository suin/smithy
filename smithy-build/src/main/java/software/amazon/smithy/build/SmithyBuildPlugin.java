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

package software.amazon.smithy.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import software.amazon.smithy.utils.ListUtils;

/**
 * Plugin extension class for SmithyBuild.
 *
 * <p>Plugins are used to contribute artifacts to SmithyBuild by writing
 * files to the {@link FileManifest} stored in the provided
 * {@link PluginContext} via the {@link PluginContext#getFileManifest()}.
 */
public interface SmithyBuildPlugin {
    /**
     * Gets the name of the plugin.
     *
     * @return Returns the name (e.g., "MyPlugin").
     */
    String getName();

    /**
     * Returns a list of aliases that the plugin also answers to.
     *
     * @return True if this plugin is responsible for the given name.
     */
    default Collection<String> getAliases() {
        return ListUtils.of();
    }

    /**
     * Plugins can choose whether or not to create artifacts based on whether
     * or not the projection encountered error or unsuppressed danger events.
     *
     * <p>By default plugins require that there are no errors or danger events
     * for a given projected model.
     *
     * @return Returns true if the plugin should only be called if there are
     *  no error or unsuppressed danger violations for the projection.
     */
    default boolean requiresValidModel() {
        return true;
    }

    /**
     * Executes the plugin, creating any number of artifacts.
     *
     * @param context Plugin context for build execution.
     */
    void execute(PluginContext context);

    /**
     * Creates a factory function used that creates SmithyBuildPlugins using
     * a list of resolved plugins.
     *
     * @param plugins Plugins to lookup by name in the factory function.
     * @return Returns the created factory function.
     */
    static Function<String, Optional<SmithyBuildPlugin>> createServiceFactory(Iterable<SmithyBuildPlugin> plugins) {
        // Copy from the provided iterator to prevent issues with potentially
        // caching a ServiceLoader using a Thread's context ClassLoader VM-wide.
        List<SmithyBuildPlugin> pluginList = new ArrayList<>();
        plugins.forEach(pluginList::add);
        return name -> pluginList.stream()
                .filter(plugin -> plugin.getName().equals(name) || plugin.getAliases().contains(name))
                .findFirst();
    }

    /**
     * Creates a SmithyBuildPlugin factory function using SPI and the current
     * Thread's context class loader.
     *
     * @return Returns the created factory function.
     * @see Thread#getContextClassLoader()
     */
    static Function<String, Optional<SmithyBuildPlugin>> createServiceFactory() {
        return createServiceFactory(ServiceLoader.load(SmithyBuildPlugin.class));
    }

    /**
     * Creates a SmithyBuildPlugin factory function using SPI.
     *
     * <p>This factory is used in {@link SmithyBuild} to create plugins
     * from configuration names.
     *
     * @param classLoader Class loader used to find plugin implementations.
     * @return Returns the created factory function.
     */
    static Function<String, Optional<SmithyBuildPlugin>> createServiceFactory(ClassLoader classLoader) {
        return createServiceFactory(ServiceLoader.load(SmithyBuildPlugin.class, classLoader));
    }
}
