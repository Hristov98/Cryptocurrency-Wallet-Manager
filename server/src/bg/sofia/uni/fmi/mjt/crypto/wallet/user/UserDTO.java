package bg.sofia.uni.fmi.mjt.crypto.wallet.user;

public class UserDTO {
    private String username;
    private UserProfile profile;

    public UserDTO(String username, UserProfile profile) {
        this.username = username;
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }

    public UserProfile getProfile() {
        return profile;
    }
}
