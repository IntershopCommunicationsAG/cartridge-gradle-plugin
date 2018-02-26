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
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Zip;

import java.io.File;

@SuppressWarnings("ALL")
public class ZipComponent extends Zip {

    private final ConfigurableFileCollection inputFilesProperty = getProject().files();

    @OutputFile
    public File getArchivePath() {
        setBaseName(getArtifactBaseName());
        setAppendix(getArtifactAppendix());
        setClassifier(getArtifactClassifier());

        StringBuilder path = new StringBuilder();
        path.append(IntershopExtension.MAIN_OUTPUTDIR_NAME);

        if(! getArtifactAppendix().isEmpty()) {
            path.append("/").append(getAppendix());
        }
        if(! getArtifactClassifier().isEmpty()) {
            path.append("_").append(getClassifier());
        }

        setDestinationDir(getProject().getLayout().getBuildDirectory().dir(path.toString()).get().getAsFile());

        return new File(getDestinationDir(), getArchiveName());
    }

    @InputFiles
    public FileCollection getInputFiles() {
        // necessary to  trigger the Zip task!
        from(inputFilesProperty);
        return inputFilesProperty;
    }

    public void setInputFiles(FileCollection files) {
        inputFilesProperty.setFrom(files);
    }

    private final ConfigurableFileCollection staticLibsProperty = getProject().files();

    @InputFiles
    public FileCollection getStaticLibs() {
        return staticLibsProperty;
    }

    public void setStaticLibs(FileCollection files) {
        staticLibsProperty.setFrom(files);
    }

    private final Property<String> releaseDirNameProperty = getProject().getObjects().property(String.class);

    @Input
    public String getReleaseDirName() {
        return releaseDirNameProperty.getOrElse("");
    }

    public void setReleaseDirName(String name) {
        releaseDirNameProperty.set(name);
    }

    public void provideReleaseDirName(Provider<String> releaseDirName) {
        releaseDirNameProperty.set(releaseDirName);
    }

    private final Property<String> artifactAppendixProperty = getProject().getObjects().property(String.class);

    @Input
    public String getArtifactAppendix() {
        return artifactAppendixProperty.getOrElse("");
    }

    public void setArtifactAppendix(String artifactAppendix) {
        artifactAppendixProperty.set(artifactAppendix);
    }

    public void provideArtifactAppendix(Provider<String> artifactAppendix) {
        artifactAppendixProperty.set(artifactAppendix);
    }

    private final Property<String> artifactClassifierProperty = getProject().getObjects().property(String.class);

    @Input
    public String getArtifactClassifier() {
        return artifactClassifierProperty.get();
    }

    public void setArtifactClassifier(String classifier) {
        artifactClassifierProperty.set(classifier);
    }

    public void provideArtifactClassifier(Provider<String> classifier) {
        artifactClassifierProperty.set(classifier);
    }

    private final Property<String> artifactBaseNameProperty = getProject().getObjects().property(String.class);

    @Input
    public String getArtifactBaseName() {
        return artifactBaseNameProperty.getOrElse(getProject().getName());
    }

    public void setArtifactBaseName(String baseName) {
        artifactBaseNameProperty.set(baseName);
    }

    public void provideArtifactBaseName(Provider<String> baseName) {
        artifactBaseNameProperty.set(baseName);
    }

    @TaskAction
    public void action() {

        // default configuration for component zip
        setIncludeEmptyDirs(true);
        setDuplicatesStrategy(DuplicatesStrategy.FAIL);

        // only three digits are configured (mask is 0000)
        setFileMode(640);
        setDirMode(750);

        // remove static libs from zip
        if(! getStaticLibs().isEmpty()) {
            eachFile(fileCopyDetails -> {
                if(getStaticLibs().contains(fileCopyDetails.getFile())) {
                    fileCopyDetails.exclude();
                }
            });
        }

        into(getReleaseDirName());

        // call super action ...
        copy();
    }


}
