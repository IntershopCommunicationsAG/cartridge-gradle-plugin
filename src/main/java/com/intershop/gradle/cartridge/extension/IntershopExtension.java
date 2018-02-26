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

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

@SuppressWarnings("ALL")
public class IntershopExtension {

    // names for the plugin
    public final static String ICMCOMPONENT_EXTENSION_NAME = "intershop";
    public final static String ICMCOMPONENT_GROUP_NAME = "Intershop Component Build";

    public final static String PRODUCT_NAME = "Intershop Commerce Management";
    public final static String PRODUCT_COPYRIGHT_OWNER = "Intershop Communications";

    public final static String MAIN_OUTPUTDIR_NAME = "ish-components";

    public final static String STATICFILES = "staticfiles";
    public final static String CARTRIDGE   = "cartridge";
    public final static String STATICLIBS  = "lib";

    public final static String DEPLOYGRADLE = "deployment/deploy.gradle";

    public final static String DEFAULT_IVYPUBLICATION = "ivyIntershop";
    public final static String DEFAULT_MAVENPUBLICATION = "mvnIntershop";

    private final Project project;

    private final Property<String> ivyPublicationNameProperty;
    private final Property<String> mavenPublicationNameProperty;

    private final Property<String> displayNameProperty;
    private final Property<String> descriptionProperty;

    private final RegularFileProperty deploymentFileProperty;
    private final ConfigurableFileCollection staticLibsProperty;
    private final PackageContainer packageContainer;

    @Inject
    public IntershopExtension(final Project project) {
        this.project = project;

        // init properties
        ivyPublicationNameProperty = project.getObjects().property(String.class);
        mavenPublicationNameProperty = project.getObjects().property(String.class);
        displayNameProperty = project.getObjects().property(String.class);
        descriptionProperty = project.getObjects().property(String.class);

        deploymentFileProperty = project.getLayout().fileProperty();
        staticLibsProperty = project.files();

        packageContainer = project.getObjects().newInstance(PackageContainer.class, project);
        // set defaults
        ivyPublicationNameProperty.set(DEFAULT_IVYPUBLICATION);
        mavenPublicationNameProperty.set(DEFAULT_MAVENPUBLICATION);
        displayNameProperty.set(project.getName());

        if(project.getDescription() != null && ! project.getDescription().isEmpty()) {
            descriptionProperty.set(project.getDescription());
        } else {
            descriptionProperty.set("");
        }

        deploymentFileProperty.set(project.getLayout().getProjectDirectory().file(DEPLOYGRADLE));
        staticLibsProperty.from(project.fileTree(project.getLayout().getProjectDirectory().dir(String.join("/", STATICFILES, CARTRIDGE, STATICLIBS)), files -> files.include("*.jar")));
    }

    // ivy publication name
    public Provider<String> getIvyPublicationNameProvider() {
        return ivyPublicationNameProperty;
    }

    public String getIvyPublicationName() {
        return ivyPublicationNameProperty.get();
    }

    public void setIvyPublicationName(String name) {
        ivyPublicationNameProperty.set(name);
    }

    // maven publication name
    public Provider<String> getMavenPublicationNameProvider() {
        return mavenPublicationNameProperty;
    }

    public String getMavenPublicationName() {
        return mavenPublicationNameProperty.get();
    }

    public void setMavenPublicationName(String name) {
        mavenPublicationNameProperty.set(name);
    }

    /**
     * This attribute defines a cartridge's display name.
     */
    public Provider<String> getDisplayNameProvider() {
        return displayNameProperty;
    }

    public String getDisplayName() {
        return displayNameProperty.get();
    }

    public void setDisplayName(String displayName) {
        displayNameProperty.set(displayName);
    }

    /**
     * This attribute defines the description for a cartridge.
     */
    public Provider<String> getDescriptionProvider() {
        return descriptionProperty;
    }

    public String getDescription() {
        return descriptionProperty.getOrElse("");
    }

    public void setDescription(String description) {
        descriptionProperty.set(description);
    }

    /**
     * Deployment file of a cartridge
     */
    public Provider<RegularFile> getDeploymentFileProvider() {
        return deploymentFileProperty;
    }

    /**
     * Diretory with static libraries of a component
     */
    public FileCollection getStaticLibs() {
        return staticLibsProperty;
    }

    public void setStaticLibs(FileCollection staticLibs) {
        staticLibsProperty.setFrom(staticLibs);
    }

    public void staticLibs(FileCollection staticLibs) {
        staticLibsProperty.from(staticLibs);
    }

    public PackageContainer getPackages() {
        return packageContainer;
    }

    // compatible for java and kotlin
    public void packages(Action<? super PackageContainer> action) {
        action.execute(packageContainer);
    }

    // compatible for groovy
    public void packages(Closure c) {
        project.configure(packageContainer, c);
    }
}

