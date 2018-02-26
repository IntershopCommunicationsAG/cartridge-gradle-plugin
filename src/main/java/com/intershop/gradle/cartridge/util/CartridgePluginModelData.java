/*
 * Copyright 2018 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intershop.gradle.cartridge.util;

import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

/**
 * This class provides all information for
 * the handover from the project based space
 * to the role based space.
 */
public class CartridgePluginModelData {

    private final Provider<String> displayNameProvider;
    private final Provider<String> descriptionProvider;

    private final Provider<String> ivyPublicationNameProvider;
    private final Provider<String> mavenPublicationNameProvider;

    private final Provider<RegularFile> depoymentFileProvider;
    private final FileCollection staticLibs;

    private final SoftwareComponentContainer components;

    public CartridgePluginModelData(Provider<String> ivyPublicationNameProvider,
                                    Provider<String> mavenPublicationNameProvider,
                                    Provider<String> displayNameProvider,
                                    Provider<String> descriptionProvider,
                                    Provider<RegularFile> depoymentFileProvider,
                                    FileCollection staticLibs,
                                    SoftwareComponentContainer components) {

        this.ivyPublicationNameProvider = ivyPublicationNameProvider;
        this.mavenPublicationNameProvider = mavenPublicationNameProvider;
        this.displayNameProvider = displayNameProvider;
        this.descriptionProvider = descriptionProvider;
        this.depoymentFileProvider = depoymentFileProvider;
        this.staticLibs = staticLibs;
        this.components = components;
    }

    public Provider<String> getIvyPublicationNameProvider() {
        return ivyPublicationNameProvider;
    }

    public Provider<String> getMavenPublicationNameProvider() {
        return mavenPublicationNameProvider;
    }

    public Provider<String> getDisplayNameProvider() {
        return displayNameProvider;
    }

    public Provider<String> getDescriptionProvider() {
        return descriptionProvider;
    }

    public Provider<RegularFile> getDepoymentFileProvider() {
        return depoymentFileProvider;
    }

    public FileCollection getStaticLibs() {
        return staticLibs;
    }

    public SoftwareComponentContainer getComponents() {
        return components;
    }
}
