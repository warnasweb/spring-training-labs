package training.common.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;
@Component
public class CorrelationFilter extends OncePerRequestFilter {
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  String id=req.getHeader("X-Correlation-ID");
  if(id==null || !id.matches("[A-Za-z0-9._-]{1,100}")) id=UUID.randomUUID().toString();
  try(MDC.MDCCloseable ignored=MDC.putCloseable("correlationId",id)) { res.setHeader("X-Correlation-ID",id);chain.doFilter(req,res); }
 }
}
