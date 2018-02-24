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
import org.gradle.api.Project;

import javax.inject.Inject;

public class PackageContainer {

    final static String STATICFILES = "staticfiles";

    final static String LOCAL       = "general";
    final static String SHARE       = "share";
    final static String CARTRIDGE   = "cartridge";

    final static String LOCAL_NAME = "local";
    final static String SHARE_NAME = "share";
    final static String CARTRIDGE_NAME = "cartridge";

    private final Project project;
    private final NamedDomainObjectContainer<ComponentPackage> packageContainer;

    @Inject
    public PackageContainer(final Project project) {
        this.project = project;
        packageContainer = project.container(ComponentPackage.class, new ComponentPackageFactory(project));
    }

    // default local package
    public ComponentPackage local() {
        ComponentPackage pkg = packageContainer.findByName(LOCAL_NAME);
        if(pkg == null) {
            pkg = packageContainer.create(LOCAL_NAME, local -> {
                local.sources(project.files(project.getLayout().getProjectDirectory().dir(STATICFILES.concat("/").concat(LOCAL))));
                local.setReleaseDirName("");
                local.setNameExtension(LOCAL_NAME);
            });
        }
        return pkg;
    }

    // default share package
    public ComponentPackage share() {
        ComponentPackage pkg = packageContainer.findByName(SHARE_NAME);
        if(pkg == null) {
            pkg = packageContainer.create(SHARE_NAME, share -> {
                share.sources(project.files(project.getLayout().getProjectDirectory().dir(STATICFILES.concat("/").concat(SHARE))));
                share.setReleaseDirName("");
                share.setNameExtension(SHARE_NAME);
            });
        }
        return pkg;
    }

    // default cartridge package
    public ComponentPackage cartridge() {
        ComponentPackage pkg = packageContainer.findByName(CARTRIDGE_NAME);
        if(pkg == null) {
            pkg = packageContainer.create(CARTRIDGE_NAME, cartridge -> {
                cartridge.sources(project.fileTree(project.getLayout().getProjectDirectory().dir(STATICFILES.concat("/").concat(CARTRIDGE))));
                cartridge.sources(project.fileTree(project.getLayout().getProjectDirectory(), files -> files.include("edl/**")));

                cartridge.setReleaseDirName(project.getName().concat("/").concat("release"));
                cartridge.setNameExtension(CARTRIDGE_NAME);
            });
        }
        return pkg;
    }

    // configurate default packages in groovy
    public void local(Closure c) {
        project.configure(local(), c);
    }

    public void share(Closure c) {
        project.configure(share(), c);
    }

    public void cartridge(Closure c) {
        project.configure(cartridge(), c);
    }

    // configure default packages in java or kotlin
    public void local(Action<? super ComponentPackage> configure) {
        configure.execute(local());
    }

    public void share(Action<? super ComponentPackage> configure) {
        configure.execute(share());
    }

    public void cartridge(Action<? super ComponentPackage> configure) {
        configure.execute(cartridge());
    }

    // direct access to container
    public NamedDomainObjectContainer<ComponentPackage> getPackageContainer() {
        return packageContainer;
    }
}
