package examen2_parcial2;

public class Entry {
    private String username;
    private long position;
    private Entry next;

    public Entry(String username, long position) {
        this.username = username;
        this.position = position;
        this.next = null;  
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public Entry getNext() {
        return next;
    }

    public void setNext(Entry next) {
        this.next = next;
    }
}

