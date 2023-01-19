/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.gradle;

import org.elasticsearch.gradle.ElasticsearchDistribution.Flavor;
import org.elasticsearch.gradle.ElasticsearchDistribution.Platform;
import org.elasticsearch.gradle.ElasticsearchDistribution.Type;
import org.elasticsearch.gradle.info.BuildParams;
import org.elasticsearch.gradle.info.GlobalBuildInfoPlugin;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.credentials.HttpHeaderCredentials;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RelativePath;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.authentication.http.HttpHeaderAuthentication;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.elasticsearch.gradle.Util.capitalize;

/**
 * A plugin to manage getting and extracting distributions of Elasticsearch.
 *
 * The source of the distribution could be from a local snapshot, a locally built
 * bwc snapshot, or the Elastic downloads service.
 */
public class DistributionDownloadPlugin implements Plugin<Project> {

    private static final String CONTAINER_NAME = "elasticsearch_distributions";
    private static final String FAKE_IVY_GROUP = "elasticsearch-distribution";
    private static final String FAKE_SNAPSHOT_IVY_GROUP = "elasticsearch-distribution-snapshot";
    private static final String DOWNLOAD_REPO_NAME = "elasticsearch-downloads";
    private static final String SNAPSHOT_REPO_NAME = "elasticsearch-snapshots";

    private BwcVersions bwcVersions;
    private NamedDomainObjectContainer<ElasticsearchDistribution> distributionsContainer;

    @Override
    public void apply(Project project) {
        // this is needed for isInternal
        project.getRootProject().getPluginManager().apply(GlobalBuildInfoPlugin.class);

        distributionsContainer = project.container(ElasticsearchDistribution.class, name -> {
            Configuration fileConfiguration = project.getConfigurations().create("es_distro_file_" + name);
            Configuration extractedConfiguration = project.getConfigurations().create("es_distro_extracted_" + name);
            return new ElasticsearchDistribution(name, project.getObjects(), fileConfiguration, extractedConfiguration);
        });
        project.getExtensions().add(CONTAINER_NAME, distributionsContainer);

        setupDownloadServiceRepo(project);

        if (BuildParams.isInternal()) {
            ExtraPropertiesExtension extraProperties = project.getExtensions().getExtraProperties();
            this.bwcVersions = (BwcVersions) extraProperties.get("bwcVersions");
        }

        project.afterEvaluate(this::setupDistributions);
    }

    @SuppressWarnings("unchecked")
    public static NamedDomainObjectContainer<ElasticsearchDistribution> getContainer(Project project) {
        return (NamedDomainObjectContainer<ElasticsearchDistribution>) project.getExtensions().getByName(CONTAINER_NAME);
    }

    // pkg private for tests
    void setupDistributions(Project project) {
        for (ElasticsearchDistribution distribution : distributionsContainer) {
            distribution.finalizeValues();

            DependencyHandler dependencies = project.getDependencies();
            // for the distribution as a file, just depend on the artifact directly
            dependencies.add(distribution.configuration.getName(), dependencyNotation(project, distribution));

            // no extraction allowed for rpm, deb or docker
            if (distribution.getType().shouldExtract()) {
                // for the distribution extracted, add a root level task that does the extraction, and depend on that
                // extracted configuration as an artifact consisting of the extracted distribution directory
                dependencies.add(
                    distribution.getExtracted().configuration.getName(),
                    projectDependency(project, ":", configName("extracted_elasticsearch", distribution))
                );
                // ensure a root level download task exists
                setupRootDownload(project.getRootProject(), distribution);
            }
        }
    }

    private void setupRootDownload(Project rootProject, ElasticsearchDistribution distribution) {
        String extractTaskName = extractTaskName(distribution);
        // NOTE: this is *horrendous*, but seems to be the only way to check for the existence of a registered task
        try {
            rootProject.getTasks().named(extractTaskName);
            // already setup this version
            return;
        } catch (UnknownTaskException e) {
            // fall through: register the task
        }
        setupDownloadServiceRepo(rootProject);

        final ConfigurationContainer configurations = rootProject.getConfigurations();
        String downloadConfigName = configName("elasticsearch", distribution);
        String extractedConfigName = "extracted_" + downloadConfigName;
        final Configuration downloadConfig = configurations.create(downloadConfigName);
        configurations.create(extractedConfigName);
        rootProject.getDependencies().add(downloadConfigName, dependencyNotation(rootProject, distribution));

        // add task for extraction, delaying resolving config until runtime
        if (distribution.getType() == Type.ARCHIVE || distribution.getType() == Type.INTEG_TEST_ZIP) {
            Supplier<File> archiveGetter = downloadConfig::getSingleFile;
            String extractDir = rootProject.getBuildDir().toPath().resolve("elasticsearch-distros").resolve(extractedConfigName).toString();
            TaskProvider<Sync> extractTask = rootProject.getTasks().register(extractTaskName, Sync.class, syncTask -> {
                syncTask.dependsOn(downloadConfig);
                syncTask.into(extractDir);
                syncTask.from((Callable<FileTree>) () -> {
                    File archiveFile = archiveGetter.get();
                    String archivePath = archiveFile.toString();
                    if (archivePath.endsWith(".zip")) {
                        return rootProject.zipTree(archiveFile);
                    } else if (archivePath.endsWith(".tar.gz")) {
                        return rootProject.tarTree(rootProject.getResources().gzip(archiveFile));
                    }
                    throw new IllegalStateException("unexpected file extension on [" + archivePath + "]");
                });

                // Workaround for https://github.com/elastic/elasticsearch/issues/49417
                syncTask.eachFile(details -> {
                    String[] segments = details.getRelativePath().getSegments();
                    if (segments[0].equals(".")) {
                        details.setRelativePath(new RelativePath(true, Arrays.copyOfRange(segments, 1, segments.length)));
                    }
                });
            });
            rootProject.getArtifacts()
                .add(
                    extractedConfigName,
                    rootProject.getLayout().getProjectDirectory().dir(extractDir),
                    artifact -> artifact.builtBy(extractTask)
                );
        }
    }

    private static void addIvyRepo(Project project, String name, String url, String group) {
        project.getRepositories().ivy(ivyRepo -> {
            ivyRepo.setName(name);
            ivyRepo.setUrl(url);
            ivyRepo.metadataSources(IvyArtifactRepository.MetadataSources::artifact);
            // this header is not a credential but we hack the capability to send this header to avoid polluting our download stats
            ivyRepo.credentials(HttpHeaderCredentials.class, creds -> {
                creds.setName("X-Elastic-No-KPI");
                creds.setValue("1");
            });
            ivyRepo.getAuthentication().create("header", HttpHeaderAuthentication.class);
            ivyRepo.patternLayout(layout -> layout.artifact("/downloads/elasticsearch/[module]-[revision](-[classifier]).[ext]"));
            ivyRepo.content(content -> content.includeGroup(group));
        });
        project.getRepositories().all(repo -> {
            if (repo.getName().equals(name) == false) {
                // all other repos should ignore the special group name
                repo.content(content -> content.excludeGroup(group));
            }
        });
    }

    private static void setupDownloadServiceRepo(Project project) {
        if (project.getRepositories().findByName(DOWNLOAD_REPO_NAME) != null) {
            return;
        }
        addIvyRepo(project, DOWNLOAD_REPO_NAME, "https://artifacts.elastic.co", FAKE_IVY_GROUP);
        if (BuildParams.isInternal() == false) {
            // external, so add snapshot repo as well
            addIvyRepo(project, SNAPSHOT_REPO_NAME, "https://snapshots.elastic.co", FAKE_SNAPSHOT_IVY_GROUP);
        }
    }

    /**
     * Returns a dependency object representing the given distribution.
     *
     * The returned object is suitable to be passed to {@link DependencyHandler}.
     * The concrete type of the object will either be a project {@link Dependency} or
     * a set of maven coordinates as a {@link String}. Project dependencies point to
     * a project in the Elasticsearch repo either under `:distribution:bwc`,
     * `:distribution:archives` or :distribution:packages`. Maven coordinates point to
     * either the integ-test-zip coordinates on maven central, or a set of artificial
     * coordinates that resolve to the Elastic download service through an ivy repository.
     */
    private Object dependencyNotation(Project project, ElasticsearchDistribution distribution) {

        if (BuildParams.isInternal()) {
            // non-external project, so depend on local build

            if (VersionProperties.getElasticsearch().equals(distribution.getVersion())) {
                return projectDependency(project, distributionProjectPath(distribution), "default");
            }
            BwcVersions.UnreleasedVersionInfo unreleasedInfo = bwcVersions.unreleasedInfo(Version.fromString(distribution.getVersion()));
            if (unreleasedInfo != null) {
                assert distribution.getBundledJdk();
                return projectDependency(project, unreleasedInfo.gradleProjectPath, distributionProjectName(distribution));
            }
        }

        if (distribution.getType() == Type.INTEG_TEST_ZIP) {
            return "org.elasticsearch.distribution.integ-test-zip:elasticsearch:" + distribution.getVersion() + "@zip";
        }

        Version distroVersion = Version.fromString(distribution.getVersion());
        String extension = distribution.getType().toString();
        String classifier = ":x86_64";
        if (distribution.getType() == Type.ARCHIVE) {
            extension = distribution.getPlatform() == Platform.WINDOWS ? "zip" : "tar.gz";
            if (distroVersion.onOrAfter("7.0.0")) {
                classifier = ":" + distribution.getPlatform() + "-x86_64";
            } else {
                classifier = "";
            }
        } else if (distribution.getType() == Type.DEB) {
            if (distroVersion.onOrAfter("7.0.0")) {
                classifier = ":amd64";
            } else {
                classifier = "";
            }
        } else if (distribution.getType() == Type.RPM && distroVersion.before("7.0.0")) {
            classifier = "";
        }
        String flavor = "";
        if (distribution.getFlavor() == Flavor.OSS && distroVersion.onOrAfter("6.3.0")) {
            flavor = "-oss";
        }

        String group = distribution.getVersion().endsWith("-SNAPSHOT") ? FAKE_SNAPSHOT_IVY_GROUP : FAKE_IVY_GROUP;
        return group + ":elasticsearch" + flavor + ":" + distribution.getVersion() + classifier + "@" + extension;
    }

    private static Dependency projectDependency(Project project, String projectPath, String projectConfig) {
        if (project.findProject(projectPath) == null) {
            throw new GradleException("no project [" + projectPath + "], project names: " + project.getRootProject().getAllprojects());
        }
        Map<String, Object> depConfig = new HashMap<>();
        depConfig.put("path", projectPath);
        depConfig.put("configuration", projectConfig);
        return project.getDependencies().project(depConfig);
    }

    private static String distributionProjectPath(ElasticsearchDistribution distribution) {
        String projectPath = ":distribution";
        switch (distribution.getType()) {
            case INTEG_TEST_ZIP:
                projectPath += ":archives:integ-test-zip";
                break;

            case DOCKER:
                projectPath += ":docker:";
                projectPath += distributionProjectName(distribution);
                break;

            default:
                projectPath += distribution.getType() == Type.ARCHIVE ? ":archives:" : ":packages:";
                projectPath += distributionProjectName(distribution);
                break;
        }
        return projectPath;
    }

    private static String distributionProjectName(ElasticsearchDistribution distribution) {
        String projectName = "";
        if (distribution.getFlavor() == Flavor.OSS) {
            projectName += "oss-";
        }
        if (distribution.getBundledJdk() == false) {
            projectName += "no-jdk-";
        }

        if (distribution.getType() == Type.ARCHIVE) {
            if (Version.fromString(distribution.getVersion()).onOrAfter("7.0.0")) {
                Platform platform = distribution.getPlatform();
                projectName += platform.toString() + (platform == Platform.WINDOWS ? "-zip" : "-tar");
            } else {
                projectName = distribution.getFlavor().equals(Flavor.DEFAULT) ? "zip" : "oss-zip";
            }
        } else if (distribution.getType() == Type.DOCKER) {
            projectName += "docker-export";
        } else {
            projectName += distribution.getType();
        }
        return projectName;
    }

    private static String configName(String prefix, ElasticsearchDistribution distribution) {
        return String.format(
            "%s_%s_%s_%s%s%s",
            prefix,
            distribution.getVersion(),
            distribution.getType(),
            distribution.getPlatform() == null ? "" : distribution.getPlatform() + "_",
            distribution.getFlavor(),
            distribution.getBundledJdk() ? "" : "_nojdk"
        );
    }

    private static String extractTaskName(ElasticsearchDistribution distribution) {
        String taskName = "extractElasticsearch";
        if (distribution.getType() != Type.INTEG_TEST_ZIP) {
            if (distribution.getFlavor() == Flavor.OSS) {
                taskName += "Oss";
            }
            if (distribution.getBundledJdk() == false) {
                taskName += "NoJdk";
            }
        }
        if (distribution.getType() == Type.ARCHIVE) {
            taskName += capitalize(distribution.getPlatform().toString());
        } else if (distribution.getType() != Type.INTEG_TEST_ZIP) {
            taskName += capitalize(distribution.getType().toString());
        }
        taskName += distribution.getVersion();
        return taskName;
    }
}
