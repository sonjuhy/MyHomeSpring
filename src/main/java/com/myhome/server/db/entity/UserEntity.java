package com.myhome.server.db.entity;

import com.myhome.server.api.dto.UserDto;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


@Getter
@Entity
@Table(name = "USER_TB")
@ToString
@NoArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @Column(name = "USER_PK")
    private long userId;
    @Column(name = "ID_CHAR")
    private String id;
    @Column(name = "NAME_CHAR")
    private String name;
    @Column(name = "PASSWORD_CHAR")
    private String password;
    @Column(name = "ACCESS_CHAR")
    private String accessToken;
    @Column(name = "REFRESH_CHAR")
    private String refreshToken;
    @Column(name="AUTH_CHAR")
    private String auth;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> roles = new HashSet<>();
        for (String role : auth.split(",")){
            roles.add(new SimpleGrantedAuthority(role));
        }
        return roles;

    }

    @Override
    public String getUsername() {
        return getName();
    }

    // 계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금되었는지 확인하는 로직
        return true; // true -> 잠금되지 않았음
    }

    // 패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않았음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        // 계정이 사용 가능한지 확인하는 로직
        return true; // true -> 사용 가능
    }

    @Builder
    public UserEntity(UserDto userDto){
        this.userId = userDto.getUserId();
        this.accessToken = userDto.getAccessToken();
        this.refreshToken = userDto.getRefreshToken();
        this.name = userDto.getName();
        this.id = userDto.getId();
        this.password = userDto.getPassword();
        this.auth = userDto.getAuth();
    }

}
