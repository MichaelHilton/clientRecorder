package edu.oregonstate.cope.eclipse.installer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import edu.oregonstate.cope.eclipse.COPEPlugin;

public class PluginInstallationTest {

	private Installer installer;
	private HashSet<Path> workspaceFiles;
	private HashSet<Path> permanentFiles;

	@Before
	public void setUp() throws IOException {
		installer = new Installer();
		workspaceFiles = new HashSet<>();
		permanentFiles = new HashSet<>();

		addToFileSet(workspaceFiles, COPEPlugin.getDefault().getVersionedLocalStorage().toPath());
		addToFileSet(permanentFiles, COPEPlugin.getDefault().getBundleStorage().toPath());
	}

	private void deletePermanentFiles() throws IOException {
		for (Path path : permanentFiles) {
			Files.deleteIfExists(path);
		}
	}

	private void deleteWorkspaceFiles() throws IOException {
		for (Path path : workspaceFiles) {
			Files.deleteIfExists(path);
		}
	}

	private void addToFileSet(HashSet<Path> fileSet, Path root) {
		fileSet.add(root.resolve(Installer.SURVEY_FILENAME));
		fileSet.add(root.resolve(Installer.EMAIL_FILENAME));
		fileSet.add(root.resolve(COPEPlugin.getDefault()._getInstallationConfigFileName()));
	}

	private void assertAllFilesExist() {
		for (Path path : workspaceFiles) {
			System.out.println(path);
			assertTrue(Files.exists(path));
		}
		for (Path path : permanentFiles) {
			assertTrue(Files.exists(path));
		}
	}

	private void assertNoFilesExist() {
		for (Path path : workspaceFiles) {
			assertFalse(Files.exists(path));
		}
		for (Path path : permanentFiles) {
			assertFalse(Files.exists(path));
		}
	}

	@Test
	public void testInstallationFileEffects() throws Exception {
		assertAllFilesExist();
		
		deleteWorkspaceFiles();
		deletePermanentFiles();

		assertNoFilesExist();

		installer.doInstall();

		assertAllFilesExist();

		// --------------------------

		deletePermanentFiles();

		installer.doInstall();

		assertAllFilesExist();

		// --------------------------

		deleteWorkspaceFiles();

		installer.doInstall();

		assertAllFilesExist();

		// --------------------------

		installer.doInstall();

		assertAllFilesExist();
	}
}
