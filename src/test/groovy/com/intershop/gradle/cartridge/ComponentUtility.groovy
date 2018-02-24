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

package com.intershop.gradle.cartridge

class ComponentUtility {

    static void prepareComponentStructure(File dir, String cartridgename='test-artridge') {
        // create main directories
        File staticfilesFolder = new File(dir, "staticfiles")

        // create staticfiles directories
        File cartridgeFolder = new File(staticfilesFolder, "cartridge")
        File shareFolder = new File(staticfilesFolder, "share")
        File localFolder = new File(staticfilesFolder, "general")

        // create share folder
        File systemFolder = new File(shareFolder, 'system')
        File sitesFolder = new File(shareFolder, 'sites')

        // create cartridge folders
        File componentsFolder = new File(cartridgeFolder, "components")
        File templatesFolder = new File(cartridgeFolder, "templates/default")
        File libFolder = new File(cartridgeFolder, "lib")
        File pipelineFolder = new File(cartridgeFolder, "pipelines")

        // create resource folders
        File resourceFolder = new File(libFolder, 'lib/resource')

        // create local folders
        File rootFolder = new File(localFolder, 'root')
        File winFolder = new File(localFolder, 'win.x86_64')
        File linuxFolder = new File(localFolder, 'linux.x86_64')
        File darwinFolder = new File(localFolder, 'darwin.x86_64')
        File specialLinuxFolder = new File(localFolder, 'linux-SLES10.0-x86_64')
        File specialWinFolder = new File(localFolder, 'win-AMD64')
    }

    static void prepareFolders(File srcdir, String folderpath, String cartridgeName='test-artridge', String filename = null, String filecontent = null) {
        File folder = new File(srcdir, folderpath.replace('$cartridge$', cartridgeName))
        folder.mkdirs()

        if(filename) {
            File file = new File(folder, filename.replace('$cartridge$', cartridgeName))
            if (filecontent) {
                file << filecontent
            }
        }
    }

    static void prepareDeploymentFile(File dir, String cartridgename='test-artridge') {
        prepareFolders(dir, 'deployment', cartridgename, 'deploy.gradle', '// deploy.gradle content')
    }

    static void prepareStaticSitesFolder(File dir, String cartridgename='test-artridge') {
        prepareFolders(dir, 'staticfiles/share/sites/$cartridge$/units/root/impex/src', cartridgename, 'Users.xml', '// content file users xml')
        prepareFolders(dir, 'staticfiles/share/sites/$cartridge$/units/root/impex/config', cartridgename, 'Users.xml', '// content file users xml')
    }

    static void prepareCartridgConfig(File dir, String cartridgename='test-artridge') {
        prepareFolders(dir, 'staticfiles/share/system/config/cartridges', cartridgename, '$cartridge$.properties', '# properties content')
    }

    static void prepareStaticCartridgFolder(File dir, String cartridgename='test-artridge') {
        prepareFolders(dir, 'staticfiles/cartridge')
        prepareFolders(dir, 'staticfiles/cartridge/pipelines', cartridgename, 'testpipeline.pipeline', '// pipeline content')
    }

    static void prepareStaticCartridgeSimpleLibFolder(File dir, String cartridgename='test-artridge') {
        prepareFolders(dir, 'staticfiles/cartridge/lib', cartridgename, 'testfile.jar', 'jar file content')
    }

    static void prepareStaticCartridgeResLibFolder(File dir, String cartridgename='test-artridge') {
        prepareFolders(dir, 'staticfiles/cartridge/lib/resources/$cartridge$/dbinit/scripts', cartridgename, 'testfile.ddl', '// filecontent of ddl file')
    }

    static void prepareCartridge(File dir, String cartridgename='test-artridge') {
        prepareStaticCartridgeResLibFolder(dir, cartridgename)
        prepareStaticCartridgeSimpleLibFolder(dir, cartridgename)
        prepareStaticCartridgFolder(dir, cartridgename)
        prepareStaticSitesFolder(dir, cartridgename)
        prepareDeploymentFile(dir, cartridgename)
    }
}
