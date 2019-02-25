package appnogui;

/**
 * Enum zur Auswahl der MultiTSPSyn Option
 */
public enum MultiTSPSynOpt {
	syn, asyn;
	
	/**
	 * Die Methode überprüft, ob die Option für MultiTSPSyn verfügbar oder nicht
	 * 
	 * @param sOption - Option, welche geprüft werden soll
	 * @return boolean - ist als Option für MultiTSPSyn verfügbar oder nicht
	 */
	public static boolean isAsyn(String sOption) {
		if(sOption.equals(MultiTSPSynOpt.valueOf("asyn").toString()))
			return true;

	    return false;
    }
	
	/**
	 * Die Methode überprüft, ob die Option für MultiTSPSyn verfügbar oder nicht
	 * 
	 * @param sOption - Option, welche geprüft werden soll
	 * @return boolean - ist als Option für MultiTSPSyn verfügbar oder nicht
	 */
	public static boolean isSsyn(String sOption) {
		if(sOption.equals(MultiTSPSynOpt.valueOf("syn").toString()))
			return true;

	    return false;
    }
}
