package beans.helper;

/**
 * @deprecated
 */
public abstract class IllnessScriptKey {

	public static final int KEY_AGE = 1;
	
	private int key;
	//private long illnessScriptId;
	
	
	//public IllnessScriptKey(){}
	public int getKey() {return key;}
	public void setKey(int key) {this.key = key;}
	//public long getIllnessScriptId() {return illnessScriptId;}
	//public void setIllnessScriptId(long illnessScriptId) {this.illnessScriptId = illnessScriptId;}	

}
