package site.pushy.shirodemo.shiro;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import site.pushy.shirodemo.bean.Role;
import site.pushy.shirodemo.bean.User;
import site.pushy.shirodemo.service.UserService;
import site.pushy.shirodemo.service.UserServiceImpl;
import site.pushy.shirodemo.util.JWTUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Pushy
 * @since 2018/11/20 21:09
 */
@Component
public class MyRealm extends AuthorizingRealm {

    @Resource
    private UserService userService;

    @Override
    public String getName() {
        return "myRealm";
    }

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        return authenticationToken instanceof JwtToken;
    }

    /**
     *  默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        String id = JWTUtil.decode(token);  // 解密Token
        if (id == null) {
            // 解密失败，抛出异常
            throw new AuthenticationException("Invalid token.");
        }
        // 解密成功，返回SimpleAuthenticationInfo对象
        return new SimpleAuthenticationInfo(token, token, "myRealm");
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String id = JWTUtil.decode(principals.toString());
        User user = userService.getUserById(id);

        if (user != null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            // 获取当前用户的所有角色，并且通过addRole添加到simpleAuthorizationInfo当中
            // 这样当Shiro内部检查用户是否有某项权限时就会从SimpleAuthorizationInfo中拿取校验
            List<Role> roles = userService.listRoleByUserId(user.getId());
            for (Role role : roles) {
                simpleAuthorizationInfo.addRole(role.getName());
            }
            return simpleAuthorizationInfo;
        }
        return null;
    }
}
