package org.example;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;

    public void init() throws ServletException {
        engine = new TemplateEngine();

        JavaxServletWebApplication javaxServletWebApplication = JavaxServletWebApplication
                .buildApplication(this.getServletContext());

        WebApplicationTemplateResolver resolver = new WebApplicationTemplateResolver(javaxServletWebApplication);

        resolver.setPrefix("/WEB-INF/temp/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=utf-8");

        String encodedTimezone = req.getParameter("timezone");

        ZoneId zoneId;
        if (encodedTimezone != null && !encodedTimezone.isEmpty()) {
            String modifiedTimezone = encodedTimezone.replace(" ", "+");
            resp.addCookie(new Cookie("lastTimezone", modifiedTimezone));
            zoneId = ZoneId.of(modifiedTimezone);
        } else {
            zoneId = getTimezoneFromCookie(req);

            if (zoneId == null) {
                zoneId = ZoneId.of("UTC");
            }
        }

        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
        String formattedTime = currentTime.format(formatter);
        PrintWriter out = resp.getWriter();

        Context context = new Context();
        context.setVariable("formattedTime", formattedTime);

        engine.process("time_template", context, out);
    }

    private ZoneId getTimezoneFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    return ZoneId.of(cookie.getValue());
                }
            }
        }
        return null;
    }
}
