package beans.list;

import java.util.*;

public interface ListInterface {

	public String getIdForJsonList();
	public String getName();
	public Locale getLanguage();
	public boolean isSynonym();
	public String getItemType();
	public long getListItemId();
}
