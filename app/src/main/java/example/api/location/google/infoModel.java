package example.api.location.google;

public class infoModel {

    String name;
    String username;

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public infoModel() {
    }

    public infoModel(String name, String username) {
        this.name = name;
        this.username = username;
    }
}
