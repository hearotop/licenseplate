package org.license.Classes;

public class GuestInfo {
    private String guestID;
    private String guestName;

    public GuestInfo()
    {

    }

    public GuestInfo(String guestID, String guestName)
    {
        this.guestID = guestID;
        this.guestName = guestName;
    }

    public String getGuestID()
    {
        return guestID;
    }

    public String getGuestName()
    {
        return guestName;
    }

    public void setGuestID(String guestID)
    {
        this.guestID = guestID;
    }

    public void setGuestName(String guestName)
    {
        this.guestName = guestName;
    }


}
