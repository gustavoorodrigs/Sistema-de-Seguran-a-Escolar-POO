package com.escolasegura.core.security;

import com.escolasegura.modules.accesscontrol.model.Device;
import com.escolasegura.modules.accesscontrol.repository.DeviceRepository;
import com.escolasegura.modules.accesscontrol.service.DeviceTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class DeviceAuthenticationFilter extends OncePerRequestFilter {

    public static final String DEVICE_TOKEN_HEADER = "X-Device-Token";

    private final DeviceRepository deviceRepository;
    private final DeviceTokenService deviceTokenService;

    public DeviceAuthenticationFilter(DeviceRepository deviceRepository, DeviceTokenService deviceTokenService) {
        this.deviceRepository = deviceRepository;
        this.deviceTokenService = deviceTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String rawToken = request.getHeader(DEVICE_TOKEN_HEADER);

        if (rawToken != null && !rawToken.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
            String tokenHash = deviceTokenService.hashToken(rawToken.trim());
            Optional<Device> deviceOpt = deviceRepository.findByTokenHashAndActiveTrue(tokenHash);

            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        device.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}