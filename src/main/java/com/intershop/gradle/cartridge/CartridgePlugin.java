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
package com.intershop.gradle.cartridge;

import com.intershop.gradle.cartridge.extension.IntershopExtension;
import com.intershop.gradle.cartridge.task.ZipComponent;
import com.intershop.gradle.cartridge.util.CartridgePluginModelData;
import org.apache.log4j.Logger;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.model.Defaults;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.model.internal.core.ModelPath;
import org.gradle.model.internal.core.ModelReference;
import org.gradle.model.internal.core.ModelRegistrations;
import org.gradle.model.internal.registry.ModelRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import java.io.File;

public class CartridgePlugin implements Plugin<Project> {

    ModelRegistry modelRegistry;

    @Inject
    public CartridgePlugin(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    @Override
    public void apply(final Project project) {
        //add base plugin for all publications ...
        project.getPluginManager().apply(PublishingPlugin.class);

        project.getLogger().info("ICM Component plugin plugin adds extension {} to {}", IntershopExtension.ICMCOMPONENT_EXTENSION_NAME, project.getName());
        ExtensionContainer extensions = project.getExtensions();

        if(extensions.findByType(IntershopExtension.class) == null) {
            extensions.add(IntershopExtension.ICMCOMPONENT_EXTENSION_NAME, new IntershopExtension(project));
        }
        final IntershopExtension extension = extensions.findByType(IntershopExtension.class);

        extension.getPackages().getPackageContainer().all(pkg -> {
            ZipComponent task = project.getTasks().maybeCreate(pkg.getTaskName(), ZipComponent.class);
            task.setGroup(IntershopExtension.ICMCOMPONENT_GROUP_NAME);

            task.setInputFiles(pkg.getSources());
            task.provideReleaseDirName(pkg.getReleaseDirNameProvider());
            task.provideArtifactBaseName(pkg.getBaseNameProvider());
            task.provideArtifactAppendix(pkg.getNameExtensionProvider());
            task.provideArtifactClassifier(pkg.getOsExtensionPProvider());
            task.setStaticLibs(extension.getStaticLibs());
        });

        if(modelRegistry!= null && modelRegistry.state(new ModelPath("staticLibs")) == null) {
            modelRegistry.register(
                    ModelRegistrations.bridgedInstance(
                            ModelReference.of("cartridgeData", CartridgePluginModelData.class),
                                new CartridgePluginModelData(
                                        extension.getIvyPublicationNameProvider(),
                                        extension.getMavenPublicationNameProvider(),
                                        extension.getDisplayNameProvider(),
                                        extension.getDescriptionProvider(),
                                        extension.getDeploymentFileProvider(),
                                        extension.getStaticLibs()))
                            .descriptor("Model of Externale Cartridge Data.")
                            .build());
        }
    }

    static class Rules extends RuleSource {

        private static final Logger LOGGER = Logger.getLogger(Rules.class);

        @Defaults
        public static void configurePublishingPublications(ModelMap<Task> tasks,
                                                           final PublishingExtension publishing,
                                                           final CartridgePluginModelData cartridgeData) {

            PublicationContainer publications = publishing.getPublications();
            // add maven artifacts to publication
            try {
                publications.create(
                        cartridgeData.getMavenPublicationNameProvider().getOrElse(IntershopExtension.DEFAULT_MAVENPUBLICATION),
                        MavenPublication.class,
                        mvnPublication -> {
                            for(ZipComponent task : tasks.withType(ZipComponent.class).values()) {
                                mvnPublication.artifact(task, mvnArtifact -> {
                                    mvnArtifact.setExtension(task.getExtension());

                                    // an empty string for classifier is not allowed
                                    if(! task.getArtifactClassifier().isEmpty()) {
                                        mvnArtifact.setClassifier(task.getArtifactAppendix() + "_" + task.getArtifactClassifier());
                                    } else {
                                        mvnArtifact.setClassifier(task.getArtifactAppendix());
                                    }
                                });
                            }
                            // add static files
                            for(File file : cartridgeData.getStaticLibs().getFiles()) {
                                mvnPublication.artifact(file, mvnArtifact -> {
                                    mvnArtifact.setClassifier(getFileNamewithoutExtension(file.getName()));
                                    mvnArtifact.setExtension("jar");
                                });
                            }
                            File deploymentFile = cartridgeData.getDepoymentFileProvider().get().getAsFile();
                            if(deploymentFile.exists() && deploymentFile.isFile()) {
                                mvnPublication.artifact(deploymentFile, mvnArtifact -> {
                                    mvnArtifact.setClassifier("deploy-gradle");
                                });
                            }
                            // add description and displayname to pom descriptor
                            mvnPublication.getPom().withXml(xmlProvider -> {
                                Element root = xmlProvider.asElement();
                                Document document = root.getOwnerDocument();

                                NodeList propertiesList = root.getElementsByTagName("properties");
                                Node propertiesNode;
                                if(propertiesList.getLength() > 0) {
                                    propertiesNode = propertiesList.item(0);
                                } else {
                                    propertiesNode = document.createElement("properties");
                                    propertiesNode = root.appendChild(propertiesNode);
                                }

                                NodeList propNodes = propertiesNode.getChildNodes();
                                if(propNodes.getLength() > 0) {
                                    if(propNodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
                                        Element propsElement = (Element)propNodes.item(0);
                                        NodeList displayNameList = propsElement.getElementsByTagName("cartridge-displayname");
                                        if(displayNameList.getLength() > 0) {
                                            propertiesNode.removeChild(displayNameList.item(0));
                                        }
                                        NodeList descriptionList = propsElement.getElementsByTagName("cartridge-description");
                                        if(descriptionList.getLength() > 0) {
                                            propertiesNode.removeChild(descriptionList.item(0));
                                        }
                                    }
                                }
                                if(! cartridgeData.getDisplayNameProvider().getOrElse("").isEmpty()) {
                                    Element displayname = document.createElement("cartridge-displayname");
                                    displayname.appendChild(document.createTextNode(cartridgeData.getDisplayNameProvider().get()));
                                    propertiesNode.appendChild(displayname);
                                }
                                if(! cartridgeData.getDescriptionProvider().getOrElse("").isEmpty()) {
                                    Element description = document.createElement("cartridge-description");
                                    description.appendChild(document.createTextNode(cartridgeData.getDescriptionProvider().get()));
                                    propertiesNode.appendChild(description);
                                }
                            });
                        });
            } catch (InvalidUserDataException ex) {
                LOGGER.debug("Maven Publishing Plugin is not applied for CartridgePlugin!");
            }

            // add ivy artifacts to publication
            try {
                publications.create(
                        cartridgeData.getIvyPublicationNameProvider().getOrElse(IntershopExtension.DEFAULT_IVYPUBLICATION),
                        IvyPublication.class,
                        ivyPublication -> {
                            for(ZipComponent task : tasks.withType(ZipComponent.class).values()) {
                                ivyPublication.artifact(task, ivyArtifact -> {
                                    ivyArtifact.setName(task.getArtifactBaseName());
                                    ivyArtifact.setType(task.getArtifactAppendix());
                                    // an empty string for classifier is not allowed
                                    if(! task.getArtifactClassifier().isEmpty()) {
                                        ivyArtifact.setClassifier(task.getArtifactClassifier());
                                    }
                                });
                            }
                            for(File file : cartridgeData.getStaticLibs().getFiles()) {
                                ivyPublication.artifact(file, ivyArtifact -> {
                                    ivyArtifact.setName(getFileNamewithoutExtension(file.getName()));
                                });
                            }
                            File deploymentFile = cartridgeData.getDepoymentFileProvider().get().getAsFile();
                            if(deploymentFile.exists() && deploymentFile.isFile()) {
                                ivyPublication.artifact(deploymentFile, ivyArtifact -> {
                                    ivyArtifact.setName(getFileNamewithoutExtension(deploymentFile.getName()));
                                    ivyArtifact.setType("deploy-gradle");
                                });
                            }

                            // add description and displayname to ivy descriptor
                            ivyPublication.getDescriptor().withXml(xmlProvider -> {
                                Element root = xmlProvider.asElement();
                                Document document = root.getOwnerDocument();

                                root.setAttribute("xmlns:e", "http://ant.apache.org/ivy/extra");
                                NodeList infoList = root.getElementsByTagName("info");
                                if(infoList.getLength() > 0) {
                                    Node infoElement = infoList.item(0);

                                    if(! cartridgeData.getDescriptionProvider().getOrElse("").isEmpty()) {
                                        if(infoElement.getNodeType() == Node.ELEMENT_NODE) {
                                            NodeList descriptionList = ((Element)infoElement).getElementsByTagName("description");
                                            if(descriptionList.getLength() > 0) {
                                                infoElement.removeChild(descriptionList.item(0));
                                            }
                                        }

                                        Node description = document.createElement("description");
                                        description.appendChild(document.createTextNode(cartridgeData.getDescriptionProvider().get()));
                                        infoElement.appendChild(description);
                                    }
                                    if(! cartridgeData.getDisplayNameProvider().getOrElse("").isEmpty()) {
                                        if(infoElement.getNodeType() == Node.ELEMENT_NODE) {
                                            NodeList displayNameList = ((Element)infoElement).getElementsByTagName("e:displayName");
                                            if(displayNameList.getLength() > 0) {
                                                infoElement.removeChild(displayNameList.item(0));
                                            }
                                        }

                                        Node displayName = document.createElement("e:displayName");
                                        displayName.appendChild(document.createTextNode(cartridgeData.getDisplayNameProvider().get()));
                                        infoElement.appendChild(displayName);
                                    }
                                } else {
                                    LOGGER.error("Ivy descriptor does not contain info element!");
                                }
                            });
                        });

            } catch (InvalidUserDataException ex) {
                LOGGER.debug("Ivy Publishing Plugin is not applied for CartridgePlugin!");
            }
        }

        private static String getFileNamewithoutExtension(String filename) {
            int extensionIndex = filename.lastIndexOf(".");
            if (extensionIndex == -1)
                return filename;

            return filename.substring(0, extensionIndex);
        }
    }
}