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
package com.intershop.gradle.cartridge.extension;

import org.apache.commons.lang.WordUtils;
import org.gradle.api.Named;
import org.gradle.api.NonNullApi;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

/**
 * This class provides all information of a package
 * of an INTERSHOP cartridge.
 */
@SuppressWarnings("unused")
@NonNullApi
public class ComponentPackage implements Named {

    private final String pkgName;

    private final Property<String> baseNameProperty;
    private final Property<String> nameExtensionProperty;
    private final Property<String> osExtensionProperty;
    private final Property<String> releaseDirNameProperty;

    private final ConfigurableFileCollection sourcesProperty;

    @Inject
    ComponentPackage(final Project project, String pkgName) {
        this.pkgName = pkgName;

        // init properties
        baseNameProperty  = project.getObjects().property(String.class);
        nameExtensionProperty = project.getObjects().property(String.class);
        osExtensionProperty  = project.getObjects().property(String.class);
        releaseDirNameProperty = project.getObjects().property(String.class);
        sourcesProperty = project.files();

        // set defaults
        baseNameProperty.set(project.getName());

        String[] nameParts = pkgName.split("_");

        nameExtensionProperty.set(nameParts[0]);
        osExtensionProperty.set(nameParts.length > 1 ? nameParts[1] : "");
    }

    @Override
    public String getName() {
        return pkgName;
    }

    public Provider<String> getBaseNameProvider() {
        return baseNameProperty;
    }

    public String getBaseName() {
        return baseNameProperty.get();
    }

    public void setBaseName(String baseName) {
        baseNameProperty.set(baseName);
    }

    public Provider<String> getNameExtensionProvider() {
        return nameExtensionProperty;
    }

    public String getNameExtension() {
        return nameExtensionProperty.get();
    }

    public void setNameExtension(String name) {
        nameExtensionProperty.set(name);
    }

    public Provider<String> getOsExtensionPProvider() {
        return osExtensionProperty;
    }

    public String getOsExtension() {
        return osExtensionProperty.get();
    }

    public void setOsExtension(String name) {
        osExtensionProperty.set(name);
    }

    public Provider<String> getReleaseDirNameProvider() {
        return releaseDirNameProperty;
    }

    public String getReleaseDirName() {
        return releaseDirNameProperty.get();
    }

    public void setReleaseDirName(String name) {
        releaseDirNameProperty.set(name);
    }

    public FileCollection getSources() {
        return sourcesProperty;
    }

    public void setSources(FileCollection sources) {
        sourcesProperty.setFrom(sources);
    }

    public void sources(Object... paths) {
        sourcesProperty.from(paths);
    }

    public String getTaskName() {
        return "zip".concat(WordUtils.capitalize(pkgName));
    }
}
