package utils;

import java.util.Collection;

public class UtilityFunc {
	
	/**
	 * generic method to remove unseen mapcells 
	 * @param coll
	 * @return
	 */
	public static Collection<MapCell> removeUnseen(Collection<MapCell> coll) { 
		for(MapCell m : coll) {
			if (!m.isSeen()) {
				coll.remove(m);
			}
		}
		return coll; 
	}
}
