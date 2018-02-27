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
package com.intershop.gradle.cartridge.task;

import com.intershop.gradle.cartridge.extension.IntershopExtension;
import com.intershop.gradle.cartridge.extension.PackageContainer;
import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class ValidateCartridge extends DefaultTask {

    @TaskAction
    public void validateConfiguration() {
        final IntershopExtension extension = getProject().getExtensions().getByType(IntershopExtension.class);

        // calculate data for local files
        int localFilesSize = extension.getPackages().getLocalFiles().filter(File::isFile).getFiles().size();
        int localConfiguredPkgSize = extension.getPackages().getPackageContainer()
                .matching(pkg -> pkg.getNameExtension().equals(PackageContainer.LOCAL_NAME)).size();
        int localValidPkgSize = extension.getPackages().getPackageContainer()
                .matching(pkg -> pkg.getNameExtension().equals(PackageContainer.LOCAL_NAME) &&
                        ! pkg.getSources().filter(File::isFile).getFiles().isEmpty()).size();

        // calculate share for local files
        int shareFilesSize = extension.getPackages().getShareFiles().filter(File::isFile).getFiles().size();
        int shareConfiguredPkgSize = extension.getPackages().getPackageContainer()
                .matching(pkg -> pkg.getNameExtension().equals(PackageContainer.SHARE_NAME)).size();
        int shareValidPkgSize = extension.getPackages().getPackageContainer()
                .matching(pkg -> pkg.getNameExtension().equals(PackageContainer.SHARE_NAME) &&
                        ! pkg.getSources().filter(File::isFile).getFiles().isEmpty()).size();

        // calculate data for cartridge files
        int cartridgeFilesSize = extension.getPackages().getCartridgeFiles()
                .minus(extension.getStaticLibs()).filter(File::isFile).getFiles().size();
        int cartridgeConfiguredPkgSize = extension.getPackages().getPackageContainer()
                .matching(pkg -> pkg.getNameExtension().equals(PackageContainer.CARTRIDGE_NAME)).size();
        int cartridgeValidPkgSize = extension.getPackages().getPackageContainer()
                .matching(pkg -> pkg.getNameExtension().equals(PackageContainer.CARTRIDGE_NAME) &&
                        ! pkg.getSources().minus(extension.getStaticLibs()).filter(File::isFile).getFiles().isEmpty()).size();

        StringBuilder header = new StringBuilder();
        header.append("-----------------------------------------------------------------------------").append("\n");
        header.append("Intershop Cartridge '").append(getProject().getName());

        boolean warn = false;
        StringBuilder warnMessage = new StringBuilder("\n");
        warnMessage.append(header).append("' configuration - WARNING").append("\n");
        warnMessage.append("The cartridge '").append(getProject().getName());
        warnMessage.append("' contains files, but there are no configured packages.").append("\n\n");

        boolean error = false;
        StringBuilder errorMessage = new StringBuilder("\n");
        errorMessage.append(header).append("' configuration - ERRORS").append("\n");
        errorMessage.append("There are configured packages, but without source files.").append("\n\n");

        if(localFilesSize > 0 && localConfiguredPkgSize == 0) {
            // There are local files, but no configured local package -> warning
            warnMessage.append("   There are ").append(localFilesSize).append(" local file(s) and no local package.").append("\n");
            warnMessage.append("   -> Add configuration 'local' to Intershop extension.").append("\n");
            warn = true;
        } else  if(localValidPkgSize < localConfiguredPkgSize) {
            // There are configured local packages, but the package will be empty -> error
            errorMessage.append("   Local package is empty!").append("\n");
            errorMessage.append("   -> Remove configuration 'local' from Intershop extension.").append("\n");
            error = true;
        }

        if(shareFilesSize > 0 && shareConfiguredPkgSize == 0) {
            // There are share files, but no configured share package -> warning
            warnMessage.append("   There are ").append(shareFilesSize).append(" share file(s) and no share package.").append("\n");
            warnMessage.append("   -> Add configuration 'share' to Intershop extension.").append("\n");
            warn = true;
        } else if(shareValidPkgSize < shareConfiguredPkgSize) {
            // There are configured share packages, but the package will be empty -> error
            errorMessage.append("   Share package is empty!").append("\n");
            errorMessage.append("   -> Remove configuration 'share' from Intershop extension.").append("\n");
            error = true;
        }

        if(cartridgeFilesSize > 0 && cartridgeConfiguredPkgSize == 0) {
            // There are cartridge files, but no configured cartridge package -> warning
            warnMessage.append("   There are ").append(cartridgeFilesSize).append(" cartridge file(s) and no cartridge package.").append("\n");
            warnMessage.append("   -> Add configuration 'cartridge' to Intershop extension.").append("\n");
            warn = true;
        } else if(cartridgeValidPkgSize < cartridgeConfiguredPkgSize) {
            // There are configured cartridge packages, but the package will be empty -> error
            errorMessage.append("   Cartridge package is empty!").append("\n");
            errorMessage.append("   -> Remove configuration 'cartridge' from Intershop extension.").append("\n");
            error = true;
        }

        if(warn) {
            warnMessage.append("-----------------------------------------------------------------------------");
            System.out.println(warnMessage.toString());
        }
        if(error) {
            errorMessage.append("-----------------------------------------------------------------------------");
            System.err.println(errorMessage.toString());
            throw new InvalidUserDataException("There are configured packages, but without source files.");
        }
    }
}
