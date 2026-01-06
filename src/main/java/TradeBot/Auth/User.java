package TradeBot.Auth;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 50)
    private String username;


    @Column(nullable = false, length = 255)
    private String email;


    @Column(nullable = false, length = 100)
    private String password; // BCrypt


    @Column(nullable = false)
    private boolean enabled = true;


    @Column(nullable = false)
    private Instant createdAt = Instant.now();


    @Column(nullable = false)
    private Instant updatedAt = Instant.now();


    private Instant lastLoginAt;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    @PreUpdate
    public void onUpdate(){ this.updatedAt = Instant.now(); }


    // getters/setters


    public void setId(Long id) {    // ← ЭТО ДОБАВИТЬ
        this.id = id;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt(){ return createdAt; }
    public Instant getUpdatedAt(){ return updatedAt; }
    public Instant getLastLoginAt(){ return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt){ this.lastLoginAt = lastLoginAt; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}