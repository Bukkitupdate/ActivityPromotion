/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

import java.util.Map;

/**
 *
 * @author nplaschk
 */
public interface CFileHandler {
    
    public Map<String, APPlayer> load();
    public void save(Map<String, APPlayer> PLAYER);     
    public APPlayer loadPlayer(String name);
    public void savePlayer(String name, APPlayer player);
    public void close();
}
