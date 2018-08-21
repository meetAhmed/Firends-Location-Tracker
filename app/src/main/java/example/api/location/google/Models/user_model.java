package example.api.location.google.Models;

public class user_model {

    String name;
    String profileAddress;
    String key;
    String email;
    String password;

    public user_model(String name , String email, String password , String key) {
        this.name = name;
        this.profileAddress = "none";
        this.key = key;
        this.email = email;
        this.password = password;
    }

    public user_model() {
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getProfileAddress() {
        return profileAddress;
    }

    public String getKey() {
        return key;
    }
}
