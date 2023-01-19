package org.elasticsearch.gradle.precommit;

import org.elasticsearch.gradle.test.GradleUnitTestCase;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;

public class DependencyLicensesTaskTests extends GradleUnitTestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private UpdateShasTask updateShas;

    private TaskProvider<DependencyLicensesTask> task;

    private Project project;

    private Dependency dependency;

    @Before
    public void prepare() {
        project = createProject();
        task = createDependencyLicensesTask(project);
        updateShas = createUpdateShasTask(project, task);
        dependency = project.getDependencies().localGroovy();
    }

    @Test
    public void givenProjectWithLicensesDirButNoDependenciesThenShouldThrowException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("exists, but there are no dependencies"));

        getLicensesDir(project).mkdir();
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithoutLicensesDirButWithDependenciesThenShouldThrowException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("does not exist, but there are dependencies"));

        project.getDependencies().add("compile", dependency);
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithoutLicensesDirNorDependenciesThenShouldReturnSilently() throws Exception {
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithDependencyButNoShaFileThenShouldReturnException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("Missing SHA for "));

        File licensesDir = getLicensesDir(project);
        createFileIn(licensesDir, "groovy-all-LICENSE.txt", "");
        createFileIn(licensesDir, "groovy-all-NOTICE.txt", "");

        project.getDependencies().add("compile", project.getDependencies().localGroovy());
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithDependencyButNoLicenseFileThenShouldReturnException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("Missing LICENSE for "));

        project.getDependencies().add("compile", project.getDependencies().localGroovy());

        getLicensesDir(project).mkdir();
        updateShas.updateShas();
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithDependencyButNoNoticeFileThenShouldReturnException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("Missing NOTICE for "));

        project.getDependencies().add("compile", dependency);

        createFileIn(getLicensesDir(project), "groovy-all-LICENSE.txt", "");

        updateShas.updateShas();
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithDependencyAndEverythingInOrderThenShouldReturnSilently() throws Exception {
        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);

        createAllDefaultDependencyFiles(licensesDir, "groovy-all");
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithALicenseButWithoutTheDependencyThenShouldThrowException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("Unused license "));

        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);
        createAllDefaultDependencyFiles(licensesDir, "groovy-all");
        createFileIn(licensesDir, "non-declared-LICENSE.txt", "");

        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithANoticeButWithoutTheDependencyThenShouldThrowException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("Unused notice "));

        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);
        createAllDefaultDependencyFiles(licensesDir, "groovy-all");
        createFileIn(licensesDir, "non-declared-NOTICE.txt", "");

        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithAShaButWithoutTheDependencyThenShouldThrowException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("Unused sha files found: \n"));

        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);
        createAllDefaultDependencyFiles(licensesDir, "groovy-all");
        createFileIn(licensesDir, "non-declared.sha1", "");

        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithADependencyWithWrongShaThenShouldThrowException() throws Exception {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("SHA has changed! Expected "));

        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);
        createAllDefaultDependencyFiles(licensesDir, "groovy-all");

        Path groovySha = Files.list(licensesDir.toPath()).filter(file -> file.toFile().getName().contains("sha")).findFirst().get();

        Files.write(groovySha, new byte[] { 1 }, StandardOpenOption.CREATE);

        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithADependencyMappingThenShouldReturnSilently() throws Exception {
        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);
        createAllDefaultDependencyFiles(licensesDir, "groovy");

        Map<String, String> mappings = new HashMap<>();
        mappings.put("from", "groovy-all");
        mappings.put("to", "groovy");

        task.get().mapping(mappings);
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithAIgnoreShaConfigurationAndNoShaFileThenShouldReturnSilently() throws Exception {
        project.getDependencies().add("compile", dependency);

        File licensesDir = getLicensesDir(project);
        createFileIn(licensesDir, "groovy-all-LICENSE.txt", "");
        createFileIn(licensesDir, "groovy-all-NOTICE.txt", "");

        task.get().ignoreSha("groovy-all");
        task.get().checkDependencies();
    }

    @Test
    public void givenProjectWithoutLicensesDirWhenAskingForShaFilesThenShouldThrowException() {
        expectedException.expect(GradleException.class);
        expectedException.expectMessage(containsString("isn't a valid directory"));

        task.get().getShaFiles();
    }

    private Project createProject() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(JavaPlugin.class);

        return project;
    }

    private void createAllDefaultDependencyFiles(File licensesDir, String dependencyName) throws IOException, NoSuchAlgorithmException {
        createFileIn(licensesDir, dependencyName + "-LICENSE.txt", "");
        createFileIn(licensesDir, dependencyName + "-NOTICE.txt", "");

        updateShas.updateShas();
    }

    private File getLicensesDir(Project project) {
        return getFile(project, "licenses");
    }

    private File getFile(Project project, String fileName) {
        return project.getProjectDir().toPath().resolve(fileName).toFile();
    }

    private void createFileIn(File parent, String name, String content) throws IOException {
        parent.mkdir();

        Path file = parent.toPath().resolve(name);
        file.toFile().createNewFile();

        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private UpdateShasTask createUpdateShasTask(Project project, TaskProvider<DependencyLicensesTask> dependencyLicensesTask) {
        UpdateShasTask task = project.getTasks().register("updateShas", UpdateShasTask.class).get();

        task.setParentTask(dependencyLicensesTask);
        return task;
    }

    private TaskProvider<DependencyLicensesTask> createDependencyLicensesTask(Project project) {
        TaskProvider<DependencyLicensesTask> task = project.getTasks()
            .register("dependencyLicenses", DependencyLicensesTask.class, new Action<DependencyLicensesTask>() {
                @Override
                public void execute(DependencyLicensesTask dependencyLicensesTask) {
                    dependencyLicensesTask.setDependencies(getDependencies(project));
                }
            });

        return task;
    }

    private FileCollection getDependencies(Project project) {
        return project.getConfigurations().getByName("compile");
    }
}
