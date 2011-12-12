package org.fusesource.mvnplugins.uberize.transformer;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.fusesource.mvnplugins.uberize.UberEntry;
import org.fusesource.mvnplugins.uberize.Uberizer;

import junit.framework.TestCase;

public class AddResourceTest extends TestCase {

	private Uberizer mockUberizer;
	private TreeMap<String, UberEntry> mockUberEntries;
	private File mockWorkDir;	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mockUberizer = createMock(Uberizer.class);
		mockUberEntries = (TreeMap<String, UberEntry>) createMock(TreeMap.class);
		mockWorkDir = createMock(File.class);
		replay(mockUberizer);
		replay(mockUberEntries);
		replay(mockWorkDir);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		verify(mockUberizer);
		verify(mockUberEntries);
		verify(mockWorkDir);
	}

	public void testProcessWithNonExistentFileDoesNothing() throws IOException {
		AddResource transform = new AddResource();
		File tmpFile = File.createTempFile("test", "txt");
		tmpFile.delete();
		assertFalse(tmpFile.exists());
		transform.file = tmpFile;
		transform.path = "/path/to/replace";
		transform.process(mockUberizer, mockWorkDir, mockUberEntries);
	}
	
	public void testProcessWithNullFileDoesNothing() throws IOException {
		AddResource transform = new AddResource();
		transform.file = null;
		transform.path = "/path/to/replace";
		transform.process(mockUberizer, mockWorkDir, mockUberEntries);
	}

	public void testProcessWithNullPathDoesNothing() throws IOException {
		AddResource transform = new AddResource();
		File tmpFile = File.createTempFile("test", "txt");
		assertTrue(tmpFile.exists());
		transform.file = tmpFile;
		transform.path = null;
		transform.process(mockUberizer, mockWorkDir, mockUberEntries);
	}

	public void testProcessAddsResourceToUberEntry() throws IOException {
		String path = "/path/to/replace";
		
		UberEntry existingEntry = new UberEntry(path);
		File existingFile = new File("/existing/file");
		existingEntry.addSource(existingFile);
		
		TreeMap<String, UberEntry> uberEntries = new TreeMap<String, UberEntry>();
		uberEntries.put(path, existingEntry);
		
		mockUberizer = createMock(Uberizer.class);
		mockWorkDir = createMock(File.class);
		replay(mockUberizer);
		replay(mockWorkDir);
		
		AddResource transform = new AddResource();
		File newFile = File.createTempFile("test", "txt");
		transform.file = newFile;
		transform.path = path;		
		transform.process(mockUberizer, mockWorkDir, uberEntries);

		assertEquals(1, uberEntries.size());
		UberEntry newEntry = uberEntries.get(path);
		assertNotSame(existingEntry, newEntry);
		assertEquals(2, newEntry.getSources().size());
		assertTrue(newEntry.getSources().contains(existingFile));
		assertTrue(newEntry.getSources().contains(newFile));
	}
	
	public void testProcessInitialisesUberEntryIfItIsFirstAtThatPath() throws IOException {
		String path = "/path/to/replace";
		
		TreeMap<String, UberEntry> uberEntries = new TreeMap<String, UberEntry>();		
		mockUberizer = createMock(Uberizer.class);
		mockWorkDir = createMock(File.class);
		replay(mockUberizer);
		replay(mockWorkDir);
		
		AddResource transform = new AddResource();
		File newFile = File.createTempFile("test", "txt");
		transform.file = newFile;
		transform.path = path;		
		transform.process(mockUberizer, mockWorkDir, uberEntries);

		assertEquals(1, uberEntries.size());
		UberEntry newEntry = uberEntries.get(path);
		assertNotNull(newEntry);
		assertEquals(1, newEntry.getSources().size());
		assertTrue(newEntry.getSources().contains(newFile));
	}

}
