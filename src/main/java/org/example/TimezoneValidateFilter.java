package org.example;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;

@WebFilter(value = "/time")
public class TimezoneValidateFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String timezone = req.getParameter("timezone");
        if (timezone != null && !timezone.isEmpty()) {
            if (!isValidTimezone(timezone)) {
                res.setContentType("text/html; charset=utf-8");
                res.setStatus(400);
                res.getWriter().write("Error: Invalid timezone");
                res.getWriter().close();
            }
        }
        chain.doFilter(req, res);
    }

    private boolean isValidTimezone(String timezone) {
        try {
            if (timezone.contains(" ")) {
                String modifiedTimezone = timezone.replace(" ", "%2B");
                String decodedTimezone = URLDecoder.decode(modifiedTimezone, StandardCharsets.UTF_8);
                ZoneId.of(decodedTimezone);
            } else {
                ZoneId.of(timezone);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
