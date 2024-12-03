package WebProject.WebProject.oauth2;

import WebProject.WebProject.entity.User;
import WebProject.WebProject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userService.findByIdAndRole(email, "user");
        if (user == null) {
            String encodedValue = Base64.getEncoder().encodeToString("pass123".getBytes());
            String avatar = "https://haycafe.vn/wp-content/uploads/2022/02/Avatar-trang-den.png";
            user = new User("google", "user", encodedValue, name, avatar, email, null, null, null);
            userService.saveUser(user);
        }

        return oAuth2User;
    }
}
