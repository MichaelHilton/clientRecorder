package edu.oregonstate.cope.clientRecorder;

import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Records text changes and test runs from the IDE. Encodes changes in JSON
 * format.
 */

public class ClientRecorder {

	public static final String ECLIPSE_IDE = "eclipse";

	//change origin types
	public static final String CHANGE_ORIGIN_USER = "user";
	public static final String CHANGE_ORIGIN_REFRESH = "refresh";
	public static final String CHANGE_ORIGIN_REFACTORING = "refactoring";
	public static final String CHANGE_ORIGIN_UI_EVENT = "ui-event";
	public static final String CHANGE_ORIGIN_PASTE = "paste";
	public static final String CHANGE_ORIGIN_CUT = "cut";
	public static final String CHANGE_ORIGIN_UNDO = "undo";
	public static final String CHANGE_ORIGIN_REDO = "redo";

	//JSON property names
	protected static final String JSON_TEST_RESULT = "testResult";
	protected static final Object JSON_TEST_ELAPSED_TIME = "testElapsedTime";
	protected static final String JSON_CHANGE_ORIGIN = "changeOrigin";
	protected static final String JSON_LENGTH = "len";
	protected static final String JSON_OFFSET = "offset";
	protected static final String JSON_TIMESTAMP = "timestamp";
	protected static final String JSON_EVENT_TYPE = "eventType";
	protected static final String JSON_IDE = "IDE";
	protected static final String JSON_TEXT = "text";
	protected static final String JSON_ENTITY_ADDRESS = "entityAddress";
	protected static final String JSON_LAUNCH_ATTRIBUTES = "launchConfiguration";
	protected static final String JSON_LAUNCH_TIMESTAMP = "launchTimestamp";
	protected static final String JSON_REFACTORING_ID = "refactoringId";
	protected static final String JSON_REFACTORING_ARGUMENTS = "refactoringArguments";

	private String IDE;

	protected enum EventType {
		debugLaunch, normalLaunch, fileOpen, fileClose, textChange, testRun, snapshot, fileSave, launchEnd, refactoringLaunch, refactoringUndo, copy, resourceAdded, resourceRemoved
	};

	public String getIDE() {
		return IDE;
	}

	public void setIDE(String IDE) {
		this.IDE = IDE;
	}

	/**
	 * Parameter values are not checked for consistency. Fully qualified names
	 * include the workspace of the file.
	 * 
	 * @param text
	 *            This is the text that was added to the document
	 * @param offset
	 *            This is the location that the text was added in the doc
	 * @param length
	 *            length of the text that was removed
	 * @param sourceFile
	 *            fully qualified name of the file
	 * @param changeOrigin
	 *            who originated the change, ie user, refactoring engine, source
	 *            control
	 */
	public void recordTextChange(String text, int offset, int length, String sourceFile, String changeOrigin) {
		ChangePersister.instance().persist(buildTextChangeJSON(text, offset, length, sourceFile, changeOrigin));
	}

	public void recordDebugLaunch(String launchTime, String fullyQualifiedMainMethod, Map launchAttributes) {
		ChangePersister.instance().persist(buildLaunchEventJSON(EventType.debugLaunch, launchTime, fullyQualifiedMainMethod, launchAttributes));
	}

	public void recordNormalLaunch(String launchTime, String fullyQualifiedMainMethod, Map launchAttributes) {
		ChangePersister.instance().persist(buildLaunchEventJSON(EventType.normalLaunch, launchTime, fullyQualifiedMainMethod, launchAttributes));
	}
	
	public void recordLaunchEnd(String launchTime) {
		ChangePersister.instance().persist(buildLaunchEndEventJSON(EventType.launchEnd, launchTime));
	}

	public void recordFileOpen(String fullyQualifiedFileAddress) {
		ChangePersister.instance().persist(buildIDEEventJSON(EventType.fileOpen, fullyQualifiedFileAddress));
	}

	public void recordFileClose(String fullyQualifiedFileAddress) {
		ChangePersister.instance().persist(buildIDEEventJSON(EventType.fileClose, fullyQualifiedFileAddress));
	}

	public void recordTestRun(String fullyQualifiedTestMethod, String testResult, double elapsedTime) {
		ChangePersister.instance().persist(buildTestEventJSON(fullyQualifiedTestMethod, testResult, elapsedTime));
	}
	
	public void recordSnapshot(String snapshotPath) {
		ChangePersister.instance().persist(buildSnapshotJSON(snapshotPath));
	}
	
	public void recordFileSave(String filePath) {
		ChangePersister.instance().persist(buildIDEEventJSON(EventType.fileSave, filePath));
	}
	
	public void recordCopy(String entityAddress, int offset, int lenght, String copiedText) {
		ChangePersister.instance().persist(buildCopyJSON(EventType.copy, entityAddress, offset, lenght, copiedText));
	}

	protected JSONObject buildCommonJSONObj(Enum eventType) {
		JSONObject obj;
		obj = new JSONObject();
		obj.put(JSON_IDE, this.getIDE());
		obj.put(JSON_EVENT_TYPE, eventType.toString());
		obj.put(JSON_TIMESTAMP, (System.currentTimeMillis() / 1000) + "");

		return obj;
	}

	protected JSONObject buildTextChangeJSON(String text, int offset, int length, String sourceFile, String changeOrigin) {
		if (text == null || sourceFile == null || changeOrigin == null) {
			throw new RuntimeException("Change parameters cannot be null");
		}
		if (sourceFile.isEmpty())
			throw new RuntimeException("Source File cannot be empty");
		if (changeOrigin.isEmpty())
			throw new RuntimeException("Change Origin cannot be empty");

		JSONObject obj = buildCommonJSONObj(EventType.textChange);
		obj.put(JSON_TEXT, text);
		obj.put(JSON_OFFSET, offset);
		obj.put(JSON_LENGTH, length);
		obj.put(JSON_ENTITY_ADDRESS, sourceFile);
		obj.put(JSON_CHANGE_ORIGIN, changeOrigin);

		return obj;
	}

	protected JSONObject buildIDEEventJSON(Enum EventType, String fullyQualifiedEntityAddress) {
		if (fullyQualifiedEntityAddress == null) {
			throw new RuntimeException("Fully Qualified Entity address cannot be null");
		}

		JSONObject obj;
		obj = buildCommonJSONObj(EventType);
		obj.put(JSON_ENTITY_ADDRESS, fullyQualifiedEntityAddress);

		return obj;
	}
	
	protected JSONObject buildLaunchEventJSON(Enum EventType, String launchTime, String fullyQualifiedEntityAddress, Map launchAttributes) {
		JSONObject json = buildIDEEventJSON(EventType, fullyQualifiedEntityAddress);
		json.put(JSON_LAUNCH_ATTRIBUTES, launchAttributes);
		json.put(JSON_LAUNCH_TIMESTAMP, launchTime);
		return json;
	}
	
	protected JSONObject buildLaunchEndEventJSON(Enum eventType, String launchTime) {
		JSONObject jsonObject = buildCommonJSONObj(eventType);
		jsonObject.put(JSON_LAUNCH_TIMESTAMP, launchTime);
		return jsonObject;
	}

	protected JSONObject buildTestEventJSON(String fullyQualifiedTestMethod, String testResult, double elapsedTime) {
		if (fullyQualifiedTestMethod == null || testResult == null)
			throw new RuntimeException("Arguments cannot be null");
		if (fullyQualifiedTestMethod.isEmpty() || testResult.isEmpty())
			throw new RuntimeException("Arguments cannot be empty");

		JSONObject obj = buildCommonJSONObj(EventType.testRun);
		obj.put(JSON_ENTITY_ADDRESS, fullyQualifiedTestMethod);
		obj.put(JSON_TEST_RESULT, testResult);
		obj.put(JSON_TEST_ELAPSED_TIME, elapsedTime);

		return obj;
	}

	protected JSONObject buildSnapshotJSON(String snapshotPath) {
		if (snapshotPath == null)
			throw new RuntimeException("Arguments cannot be null");
		
		if(snapshotPath.isEmpty())
			throw new RuntimeException("Arguments cannot be empty");
		
		JSONObject obj = buildCommonJSONObj(EventType.snapshot);
		obj.put(JSON_ENTITY_ADDRESS, snapshotPath);
		
		return obj;
	}

	public void recordRefactoring(String refactoringName, Map argumentMap) {
		ChangePersister.instance().persist(buildRefactoringEvent(EventType.refactoringLaunch, refactoringName, argumentMap));
	}
	
	public void recordRefactoringUndo(String refactoringName, Map argumentsMap) {
		ChangePersister.instance().persist(buildRefactoringEvent(EventType.refactoringUndo, refactoringName, argumentsMap));
	}
	
	protected JSONObject buildRefactoringEvent(Enum eventType, String refactoringID, Map argumentsMap) {
		JSONObject jsonObj = buildCommonJSONObj(eventType);
		jsonObj.put(JSON_REFACTORING_ID, refactoringID);
		jsonObj.put(JSON_REFACTORING_ARGUMENTS, argumentsMap);
		
		return jsonObj;
	}
	
	protected JSONObject buildCopyJSON(EventType copy, String entityAddress, int offset, int lenght, String copiedText) {
		JSONObject jsonObj = buildCommonJSONObj(copy);
		jsonObj.put(JSON_ENTITY_ADDRESS, entityAddress);
		jsonObj.put(JSON_OFFSET, offset);
		jsonObj.put(JSON_LENGTH, lenght);
		jsonObj.put(JSON_TEXT, copiedText);
		return jsonObj;
	}
}
