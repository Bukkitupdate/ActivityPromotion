/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.Chumper.ActivityPromotion;

/**
 *
 * @author nplaschk
 */
public class APPlayer
{
    private Long timePlayed;
    private Long timeLastAction;
    private Long passivePeriod;
    private Long lastLogout;

    public void APPlayer(Long tP, Long tLA, Long pP, Long lL)
    {
        this.timePlayed = tP;
        this.timeLastAction = tLA;
        this.passivePeriod = pP;
        this.lastLogout = lL;
    }
    
    public Long getLastLogout() {
        return lastLogout;
    }

    public void setLastLogout(Long lastLogout) {
        this.lastLogout = lastLogout;
    }

    public Long getPassivePeriod() {
        return passivePeriod;
    }

    public void setPassivePeriod(Long passivePeriod) {
        this.passivePeriod = passivePeriod;
    }

    public Long getTimeLastAction() {
        return timeLastAction;
    }

    public void setTimeLastAction(Long timeLastAction) {
        this.timeLastAction = timeLastAction;
    }

    public Long getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(Long timePlayed) {
        this.timePlayed = timePlayed;
    }
}
