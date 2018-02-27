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
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.util.GUtil;

import javax.inject.Inject;

/**
 * Package Container
 * This class provides all necessary methods for the
 * creation and configuration of all available packages
 * of an INTERSHOP cartridge (component).
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PackageContainer {

    private final static String STATICFILES = "staticfiles";

    private final static String LOCAL       = "general";
    private final static String SHARE       = "share";
    private final static String CARTRIDGE   = "cartridge";

    public final static String LOCAL_NAME = "local";
    public final static String SHARE_NAME = "share";
    public final static String CARTRIDGE_NAME = "cartridge";

    private final Project project;
    private final NamedDomainObjectContainer<ComponentPackage> packageContainer;

    @Inject
    public PackageContainer(final Project project) {
        this.project = project;
        packageContainer = project.container(ComponentPackage.class, new ComponentPackageFactory(project));
    }

    /**
     * Adds default local package
     *
     * @return the configured local component package
     */
    public ComponentPackage local() {
        return getLocalContainer(LOCAL_NAME);
    }

    /**
     * Configures default local package for Groovy
     *
     * @param c     Closure to configure the local package from type ComponentPackage
     */
    public void local(Closure c) {
        project.configure(local(), c);
    }

    /**
     * Configures default local package for Java/Kotlin
     *
     * @param configure     Action to configure local package
     */
    public void local(Action<? super ComponentPackage> configure) {
        configure.execute(local());
    }

    /**
     * Adds OS specific local packages for Groovy
     *
     * @param osclassifier  Short name of the OS classifier (win, linux, darwin)
     * @param c Closure to configure the local package from type ComponentPackage
     * @return the configured local component package
     */
    public ComponentPackage createLocal(String osclassifier, Closure c) {
        return (ComponentPackage)project.configure(getLocalOSContainer(osclassifier), c);
    }

    /**
     * Adds OS specific local packages for Java/Kotlin
     *
     * @param osclassifier  Short name of the OS classifier (win, linux, darwin)
     * @param configure     Action to configure local package
     * @return the configured local component package
     */
    public ComponentPackage createLocal(String osclassifier, Action<? super ComponentPackage> configure) {
        ComponentPackage pkg = getLocalOSContainer(osclassifier);
        configure.execute(pkg);
        return pkg;
    }

    /**
     * Adds default share package
     *
     * @return the configured share component package
     */
    public ComponentPackage share() {
        return getShareContainer(SHARE_NAME);
    }

    /**
     * Configures default share package for Groovy
     *
     * @param c     Closure to configure the share package from type ComponentPackage
     */
    public void share(Closure c) {
        project.configure(share(), c);
    }

    /**
     * Configures default share package for Java/Kotlin
     *
     * @param configure     Action to configure share package
     */
    public void share(Action<? super ComponentPackage> configure) {
        configure.execute(share());
    }

    /**
     * Adds OS specific share packages for Groovy
     *
     * @param osclassifier  Short name of the OS classifier (win, linux, darwin)
     * @param c Closure to configure the share package from type ComponentPackage
     * @return the configured share component package
     */
    public ComponentPackage createShare(String osclassifier, Closure c) {
        return (ComponentPackage)project.configure(getShareOSContainer(osclassifier), c);
    }

    /**
     * Adds OS specific share packages for Java/Kotlin
     *
     * @param osclassifier  Short name of the OS classifier (win, linux, darwin)
     * @param configure     Action to configure share package
     * @return the configured share component package
     */
    public ComponentPackage createShare(String osclassifier, Action<? super ComponentPackage> configure) {
        ComponentPackage pkg = getShareOSContainer(osclassifier);
        configure.execute(pkg);
        return pkg;
    }

    /**
     * Adds default cartridge package
     *
     * @return the configured cartridge component package
     */
    public ComponentPackage cartridge() {
        return getCartridgeContainer(CARTRIDGE_NAME);
    }

    /**
     * Configures default cartridge package for Groovy
     *
     * @param c     Closure to configure the cartridge package from type ComponentPackage
     */
    public void cartridge(Closure c) {
        project.configure(cartridge(), c);
    }

    /**
     * Configures default cartridge package for Java/Kotlin
     *
     * @param configure     Action to configure cartridge package
     */
    public void cartridge(Action<? super ComponentPackage> configure) {
        configure.execute(cartridge());
    }

    /**
     * Adds OS specific cartridge packages for Groovy
     *
     * @param osclassifier  Short name of the OS classifier (win, linux, darwin)
     * @param c Closure to configure the cartridge package from type ComponentPackage
     * @return the configured cartridge component package
     */
    public ComponentPackage createCartridge(String osclassifier, Closure c) {
        return (ComponentPackage)project.configure(getCartridgeOSContainer(osclassifier), c);
    }

    /**
     * Adds OS specific cartridge packages for Java/Kotlin
     *
     * @param osclassifier  Short name of the OS classifier (win, linux, darwin)
     * @param configure     Action to configure cartridge package
     * @return the configured cartridge component package
     */
    public ComponentPackage createCartridge(String osclassifier, Action<? super ComponentPackage> configure) {
        ComponentPackage pkg = getCartridgeOSContainer(osclassifier);
        configure.execute(pkg);
        return pkg;
    }

    /**
     * This is a collection of all files in the local folder of a cartridge.
     * The collection is also used for the creation of the local package.
     *
     * @return all cartridge files for shared filesystem
     */
    public FileCollection getLocalFiles() {
        return project.files(project.getLayout().getProjectDirectory().dir(STATICFILES.concat("/").concat(LOCAL).concat("/").concat("root")));
    }

    /**
     * This is a collection of all shared file of a cartridge.
     * The collection is also used for the creation of the cartridge package.
     *
     * @return all cartridge files for shared filesystem
     */
    public FileCollection getShareFiles() {
        return project.files(project.getLayout().getProjectDirectory().dir(STATICFILES.concat("/").concat(SHARE)));
    }

    /**
     * This is a collection of all cartridge specific files.
     * It is also used for the creation of the cartridge package.
     *
     * @return all cartridge files for cartridge folder
     */
    public FileCollection getCartridgeFiles() {
        ConfigurableFileCollection cartridgeFiles = project.files();
        cartridgeFiles.from(project.fileTree(project.getLayout().getProjectDirectory().dir(STATICFILES.concat("/").concat(CARTRIDGE))));
        cartridgeFiles.from(project.fileTree(project.getLayout().getProjectDirectory(), files -> files.include("edl/**")));
        return cartridgeFiles;
    }

    /**
     * This is the container instance of this special container.
     *
     * @return all available component packages of this container.
     */
    public NamedDomainObjectSet<ComponentPackage> getPackageContainer() {
        return packageContainer;
    }

    // --- private methods
    // local package with OS extension
    private ComponentPackage getLocalOSContainer(String osclassifier) {
        String pkgName = LOCAL_NAME.concat("_").concat(osclassifier);
        ComponentPackage pkg = getLocalContainer(pkgName);
        pkg.setOsExtension(osclassifier);
        return pkg;
    }

    // default local package
    private ComponentPackage getLocalContainer(String pkgName) {
        ComponentPackage pkg = packageContainer.findByName(pkgName);
        if(! GUtil.isTrue(pkg)) {
            pkg = packageContainer.create(pkgName, local -> {
                local.sources(getLocalFiles());
                local.setReleaseDirName("");
                local.setNameExtension(LOCAL_NAME);
            });
        }
        return pkg;
    }

    // share package with OS extension
    private ComponentPackage getShareOSContainer(String osclassifier) {
        String pkgName = SHARE_NAME.concat("_").concat(osclassifier);
        ComponentPackage pkg = getShareContainer(pkgName);
        pkg.setOsExtension(osclassifier);
        return pkg;
    }

    // default share package
    private ComponentPackage getShareContainer(String pkgName) {
        ComponentPackage pkg = packageContainer.findByName(pkgName);
        if(! GUtil.isTrue(pkg)) {
            pkg = packageContainer.create(pkgName, share -> {
                share.sources(getShareFiles());
                share.setReleaseDirName("");
                share.setNameExtension(SHARE_NAME);
            });
        }
        return pkg;
    }

    // cartridge package with OS extension
    private ComponentPackage getCartridgeOSContainer(String osclassifier) {
        String pkgName = CARTRIDGE_NAME.concat("_").concat(osclassifier);
        ComponentPackage pkg = getShareContainer(pkgName);
        pkg.setOsExtension(osclassifier);
        return pkg;
    }

    // cartridge share package
    @SuppressWarnings("SameParameterValue")
    private ComponentPackage getCartridgeContainer(String pkgName) {
        ComponentPackage pkg = packageContainer.findByName(pkgName);
        if(! GUtil.isTrue(pkg)) {
            pkg = packageContainer.create(pkgName, cartridge -> {
                cartridge.sources(getCartridgeFiles());

                cartridge.setReleaseDirName(project.getName().concat("/").concat("release"));
                cartridge.setNameExtension(CARTRIDGE_NAME);
            });
        }
        return pkg;
    }
}
