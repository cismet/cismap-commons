package de.cismet.cismap.commons;
/*
 * Version.java
 * 
 * Created on 18. M\u00E4rz 2005, 12:44
 *
 * 1.2
 * Multiverschieben geht
 * Display Handle Bug entfernt
 * 1.3
 * Bug: wird BackgroundEnabled auf false gesetzt bevor ein FeatureService zur\u00FCckkommt wird dieser trotzdem angezeigt
 *      :-) behoben
 * 1.4
 * MultiPointSplitting
 * 1.5
 * 1.6 
 * Cursorkram
 * 1.7
 * Zeitverschiebung eingetragen
 * 1.8
 * Bugs entfernt (Handle sichtbar wenn RW Wechsel nicht im Select Mode)
 * 1.9
 * Versionsbug
 * 1.10
 * Versionsbug
 * 1.11
 * Bugfix: Split und MoveHandles
 * 1.12
 * Major Changes f\u00FCr Standalone Client
 * test
 */



/**
 * -
 * @author hell
 */
public class Version  {
    private  final static String VERSION="cismapCommons.jar Version:2 ($Date: 2009/10/15 09:02:50 $(+2) $Revision: 1.15 $";//NOI18N
    /** Creates a new instance of Version */
    public Version() {
        
    }
    
    public static void main(String[] args) {
        System.out.println(getVersion());
    }
    public static String getVersion() {
        return VERSION;
    }
    
}
