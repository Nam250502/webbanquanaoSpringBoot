package WebProject.WebProject.entity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data // lombok giúp generate các hàm constructor, get, set v.v.
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {
	@Id()
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id = UUID.randomUUID().toString();
	
	@Column(name = "login_Type", columnDefinition = "nvarchar(50)")
	private String login_Type;
	
	@Column(name = "role", columnDefinition = "nvarchar(50)")
	private String role;
	
	@Column(name = "password",columnDefinition = "nvarchar(255)")
	private String password;
	
	@Column(name = "user_Name", columnDefinition = "nvarchar(255)")
	private String name;

	@Column(name = "avatar", columnDefinition = "nvarchar(255)")
	private String avatar;
	
	@Column(name = "email", columnDefinition = "nvarchar(255)")
	private String email;
	
	@Column(name = "phone_Number", columnDefinition = "nvarchar(255)")
	private String phone_Number;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Order> order;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Cart> cart;

	public User(String login_Type, String role, String password, String name, String avatar, String email, String phone_Number, List<Order> order, List<Cart> cart) {
		this.login_Type = login_Type;
		this.role = role;
		this.password = password;
		this.name = name;
		this.avatar = avatar;
		this.email = email;
		this.phone_Number = phone_Number;
		this.order = order;
		this.cart = cart;
	}
}
