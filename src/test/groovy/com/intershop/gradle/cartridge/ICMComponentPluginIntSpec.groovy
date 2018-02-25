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

import com.intershop.gradle.cartridge.extension.IntershopExtension
import com.intershop.gradle.test.AbstractIntegrationSpec
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

class ICMComponentPluginIntSpec extends AbstractIntegrationSpec {

    @Unroll
    def 'Test simple component build - #gradleVersion'(gradleVersion) {
        given:
        String projectName = "testproject"

        ComponentUtility.prepareCartridge(testProjectDir, projectName)
        createSettingsGradle(projectName)

        buildFile  << """
        plugins {
            id 'com.intershop.gradle.cartridge'
        }

        intershop {
            packages {
               local()
               share()
               cartridge()
            }
        }

        """.stripIndent()

        File cartridgeZip = new File(testProjectDir, "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}/cartridge/${projectName}-cartridge-unspecified.zip")
        File shareZip = new File(testProjectDir, "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}/share/${projectName}-share-unspecified.zip")

        when:
        List<String> args = ['zipCartridge', 'zipShare', 'zipLocal', '-s', '-i']
        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.task(":zipCartridge").outcome == TaskOutcome.SUCCESS
        result1.task(":zipLocal").outcome == TaskOutcome.NO_SOURCE
        result1.task(":zipShare").outcome == TaskOutcome.SUCCESS

        ! new File(testProjectDir, "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}/local/${projectName}-local.zip").exists()
        cartridgeZip.exists()
        shareZip.exists()
        dumpZipContent(cartridgeZip).contains("${projectName}/release/pipelines/testpipeline.pipeline".toString())
        !dumpZipContent(cartridgeZip).contains("${projectName}/release/lib/testfile.jar".toString())

        when:
        def result2 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result2.task(":zipCartridge").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":zipLocal").outcome == TaskOutcome.NO_SOURCE
        result2.task(":zipShare").outcome == TaskOutcome.UP_TO_DATE

        ! new File(testProjectDir, "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}/local/${projectName}-local.zip").exists()
        cartridgeZip.exists()
        shareZip.exists()

        where:
        gradleVersion << supportedGradleVersions
    }

    @Unroll
    def 'Test publish component build - ivy happy path - #gradleVersion'(gradleVersion) {
        given:
        String projectName = "testproject"

        ComponentUtility.prepareCartridge(testProjectDir, projectName)

        String testGroupName = 'com.intershop.test'
        String testVersion = '1.0.0'

        createSettingsGradle(projectName)

        buildFile  << """
        plugins {
            id 'ivy-publish'
            id 'com.intershop.gradle.cartridge'
        }
        
        group = '${testGroupName}'
        version = '${testVersion}'

        intershop {
            packages {
                share()
                cartridge()
            }
        }
        
        publishing {
            repositories {
                ivy {
                    url "\${rootProject.buildDir}/repo"
                    layout('pattern') {
                        ivy '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'
                        artifact '[organisation]/[module]/[revision]/[ext]s/[artifact]-[type](-[classifier])-[revision].[ext]'
                        artifact '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'
                    }
                }
            }
        }
        
        """.stripIndent()

        String pluginBuildDir = "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}"

        File cartridgeZip = new File(testProjectDir, "${pluginBuildDir}/cartridge/${projectName}-cartridge-${testVersion}.zip")
        File shareZip = new File(testProjectDir, "${pluginBuildDir}/share/${projectName}-share-${testVersion}.zip")
        File ivyFile = new File(testProjectDir, "build/publications/ivyIntershop/ivy.xml")

        String pluginRepoDir = "build/repo/${testGroupName}/${projectName}/${testVersion}"

        File repoCartridgeZip = new File(testProjectDir, "${pluginRepoDir}/zips/${projectName}-cartridge-${testVersion}.zip")
        File repoShareZip = new File(testProjectDir, "${pluginRepoDir}/zips/${projectName}-share-${testVersion}.zip")
        File repoJarFile = new File(testProjectDir, "${pluginRepoDir}/jars/testfile-jar-${testVersion}.jar")
        File repoDeployFile = new File(testProjectDir, "${pluginRepoDir}/gradles/deploy-deploy-gradle-${testVersion}.gradle")
        File repoIvyDescriptor = new File(testProjectDir, "${pluginRepoDir}/ivys/ivy-${testVersion}.xml")

        when:
        List<String> args = ['publish', '-s', '-i']
        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        // check tasks
        result1.task(":zipCartridge").outcome == TaskOutcome.SUCCESS
        result1.task(":zipShare").outcome == TaskOutcome.SUCCESS
        result1.task(":publishIvyIntershopPublicationToIvyRepository").outcome == TaskOutcome.SUCCESS
        result1.task(":publish").outcome == TaskOutcome.SUCCESS

        // check files
        cartridgeZip.exists()
        shareZip.exists()
        ivyFile.exists()

        repoCartridgeZip.exists()
        repoShareZip.exists()
        repoJarFile.exists()
        repoDeployFile.exists()
        repoIvyDescriptor.exists()

        // check content
        repoDeployFile.text == new File(testProjectDir, "deployment/deploy.gradle").text

        dumpZipContent(repoShareZip).contains("sites/${projectName}/units/root/impex/config/Users.xml".toString())
        dumpZipContent(repoShareZip).contains("sites/${projectName}/units/root/impex/src/Users.xml".toString())
        dumpZipContent(repoCartridgeZip).contains("${projectName}/release/pipelines/testpipeline.pipeline".toString())
        !dumpZipContent(repoCartridgeZip).contains("${projectName}/release/lib/testfile.jar".toString())

        ivyFile.text.contains("<e:displayName>${projectName}</e:displayName>")
        ivyFile.text.contains('<ivy-module xmlns:e="http://ant.apache.org/ivy/extra" version="2.0">')
        ivyFile.text.contains('<artifact conf="compile" ext="jar" name="testfile" type="jar"/>')
        ivyFile.text.contains('<artifact ext="zip" name="' + projectName + '" type="cartridge"/>')
        ivyFile.text.contains('<artifact ext="zip" name="' + projectName + '" type="share"')
        ivyFile.text.contains('<artifact ext="gradle" name="deploy" type="deploy-gradle"/>')

        when:
        def result2 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result2.task(":zipCartridge").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":zipShare").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":publishIvyIntershopPublicationToIvyRepository").outcome == TaskOutcome.SUCCESS
        result2.task(":publish").outcome == TaskOutcome.SUCCESS


        where:
        gradleVersion << supportedGradleVersions
    }

    @Unroll
    def 'Test publish component build - maven happy path - #gradleVersion'(gradleVersion) {
        given:
        String projectName = "testproject"

        ComponentUtility.prepareCartridge(testProjectDir, projectName)

        String testGroupName = 'com.intershop.test'
        String testVersion = '1.0.0'

        createSettingsGradle(projectName)

        buildFile  << """
        plugins {
            id 'maven-publish'
            id 'com.intershop.gradle.cartridge'
        }
        
        group = '${testGroupName}'
        version = '${testVersion}'

        intershop {
            packages {
                share()
                cartridge()
            }
        }
        
        publishing {
            repositories {
                maven {
                    url "\${rootProject.buildDir}/repo"
                }
            }
        }
        
        """.stripIndent()

        String pluginBuildDir = "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}"

        File cartridgeZip = new File(testProjectDir, "${pluginBuildDir}/cartridge/${projectName}-cartridge-${testVersion}.zip")
        File shareZip = new File(testProjectDir, "${pluginBuildDir}/share/${projectName}-share-${testVersion}.zip")
        File pomFile = new File(testProjectDir, "build/publications/mvnIntershop/pom-default.xml")

        String pluginRepoDir = "build/repo/${testGroupName.split('\\.').join('/')}/${projectName}/${testVersion}/${projectName}-${testVersion}"

        File repoCartridgeZip = new File(testProjectDir, "${pluginRepoDir}-cartridge.zip")
        File repoShareZip = new File(testProjectDir, "${pluginRepoDir}-share.zip")
        File repoJarFile = new File(testProjectDir, "${pluginRepoDir}-testfile.jar")
        File repoDeployFile = new File(testProjectDir, "${pluginRepoDir}-deploy-gradle.gradle")
        File repoPomDescriptor = new File(testProjectDir, "${pluginRepoDir}.pom")

        when:
        List<String> args = ['publish', '-s', '-i']
        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        // check tasks
        result1.task(":zipCartridge").outcome == TaskOutcome.SUCCESS
        result1.task(":zipShare").outcome == TaskOutcome.SUCCESS
        result1.task(":publishMvnIntershopPublicationToMavenRepository").outcome == TaskOutcome.SUCCESS
        result1.task(":publish").outcome == TaskOutcome.SUCCESS

        // check files
        cartridgeZip.exists()
        shareZip.exists()
        pomFile.exists()

        repoCartridgeZip.exists()
        repoShareZip.exists()
        repoJarFile.exists()
        repoDeployFile.exists()
        repoPomDescriptor.exists()

        // check content
        repoDeployFile.text == new File(testProjectDir, "deployment/deploy.gradle").text

        dumpZipContent(repoShareZip).contains("sites/${projectName}/units/root/impex/config/Users.xml".toString())
        dumpZipContent(repoShareZip).contains("sites/${projectName}/units/root/impex/src/Users.xml".toString())
        dumpZipContent(repoCartridgeZip).contains("${projectName}/release/pipelines/testpipeline.pipeline".toString())
        !dumpZipContent(repoCartridgeZip).contains("${projectName}/release/lib/testfile.jar".toString())

        pomFile.text.contains("<cartridge-displayname>${projectName}</cartridge-displayname>".toString())

        when:
        def result2 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result2.task(":zipCartridge").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":zipShare").outcome == TaskOutcome.UP_TO_DATE
        result2.task(":publishMvnIntershopPublicationToMavenRepository").outcome == TaskOutcome.SUCCESS
        result2.task(":publish").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << supportedGradleVersions
    }

    @Unroll
    def 'Test publish component with JavaPlugin - #gradleVersion'(gradleVersion) {
        given:
        String projectName = "testproject"

        ComponentUtility.prepareCartridge(testProjectDir, projectName)

        String testGroupName = 'com.intershop.test'
        String testVersion = '1.0.0'

        createSettingsGradle(projectName)
        writeJavaTestClass('com.intershop.test')

        buildFile  << """
        plugins {
            id 'ivy-publish'
            id 'java'
            id 'com.intershop.gradle.cartridge'
        }
        
        group = '${testGroupName}'
        version = '${testVersion}'

        intershop {
            packages {
                share()
                cartridge()
            }
        }
        
        publishing {
            repositories {
                ivy {
                    url "\${rootProject.buildDir}/repo"
                    layout('pattern') {
                        ivy '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'
                        artifact '[organisation]/[module]/[revision]/[ext]s/[artifact]-[type](-[classifier])-[revision].[ext]'
                        artifact '[organisation]/[module]/[revision]/[type]s/ivy-[revision].xml'
                    }
                }
            }
        }
        
        """.stripIndent()

        String pluginBuildDir = "build/${IntershopExtension.MAIN_OUTPUTDIR_NAME}"

        File cartridgeZip = new File(testProjectDir, "${pluginBuildDir}/cartridge/${projectName}-cartridge-${testVersion}.zip")
        File shareZip = new File(testProjectDir, "${pluginBuildDir}/share/${projectName}-share-${testVersion}.zip")
        File ivyFile = new File(testProjectDir, "build/publications/ivyIntershop/ivy.xml")
        File javaFile = new File(testProjectDir, "build/libs/${projectName}-${testVersion}.jar");

        String pluginRepoDir = "build/repo/${testGroupName}/${projectName}/${testVersion}"

        File repoCartridgeZip = new File(testProjectDir, "${pluginRepoDir}/zips/${projectName}-cartridge-${testVersion}.zip")
        File repoShareZip = new File(testProjectDir, "${pluginRepoDir}/zips/${projectName}-share-${testVersion}.zip")
        File repoJarFile = new File(testProjectDir, "${pluginRepoDir}/jars/testfile-jar-${testVersion}.jar")
        File repoJavaFile = new File(testProjectDir, "${pluginRepoDir}/jars/testproject-jar-${testVersion}.jar")
        File repoDeployFile = new File(testProjectDir, "${pluginRepoDir}/gradles/deploy-deploy-gradle-${testVersion}.gradle")
        File repoIvyDescriptor = new File(testProjectDir, "${pluginRepoDir}/ivys/ivy-${testVersion}.xml")

        when:
        List<String> args = ['publish', '-s', '-i']
        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        true

        // check tasks
        result1.task(":zipCartridge").outcome == TaskOutcome.SUCCESS
        result1.task(":zipShare").outcome == TaskOutcome.SUCCESS
        result1.task(":classes").outcome == TaskOutcome.SUCCESS
        result1.task(":jar").outcome == TaskOutcome.SUCCESS
        result1.task(":publishIvyIntershopPublicationToIvyRepository").outcome == TaskOutcome.SUCCESS
        result1.task(":publish").outcome == TaskOutcome.SUCCESS

        // check files
        cartridgeZip.exists()
        shareZip.exists()
        ivyFile.exists()
        javaFile.exists()

        repoCartridgeZip.exists()
        repoShareZip.exists()
        repoJarFile.exists()
        repoDeployFile.exists()
        repoIvyDescriptor.exists()
        repoJavaFile.exists()

        // check content
        repoDeployFile.text == new File(testProjectDir, "deployment/deploy.gradle").text

        dumpZipContent(repoShareZip).contains("sites/${projectName}/units/root/impex/config/Users.xml".toString())
        dumpZipContent(repoShareZip).contains("sites/${projectName}/units/root/impex/src/Users.xml".toString())
        dumpZipContent(repoCartridgeZip).contains("${projectName}/release/pipelines/testpipeline.pipeline".toString())
        !dumpZipContent(repoCartridgeZip).contains("${projectName}/release/lib/testfile.jar".toString())

        ivyFile.text.contains("<e:displayName>${projectName}</e:displayName>")
        ivyFile.text.contains('<ivy-module xmlns:e="http://ant.apache.org/ivy/extra" version="2.0">')
        ivyFile.text.contains('<artifact conf="compile" ext="jar" name="testfile" type="jar"/>')
        ivyFile.text.contains('<artifact conf="compile" ext="jar" name="' + projectName + '" type="jar"/>')
        ivyFile.text.contains('<artifact ext="zip" name="' + projectName + '" type="cartridge"/>')
        ivyFile.text.contains('<artifact ext="zip" name="' + projectName + '" type="share"')
        ivyFile.text.contains('<artifact ext="gradle" name="deploy" type="deploy-gradle"/>')


        where:
        gradleVersion << supportedGradleVersions
    }

    File createSettingsGradle(String projectName) {
        File settingsFile = new File(testProjectDir, 'settings.gradle')
        settingsFile << """
        rootProject.name = '${projectName}'
        """.stripIndent()

        return settingsFile
    }

    List<String> dumpZipContent(File zipFIle) {
        List<String> entries = []
        def zf = new java.util.zip.ZipFile(zipFIle)
        zf.entries().findAll { !it.directory }.each {
            entries.add(it.name)
        }
        return entries
    }
}
