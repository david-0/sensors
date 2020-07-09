package org.myprojects.sensors.restservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {
//    /*
//     * Redirects all routes to FrontEnd except: '/', '/index.html', '/api', '/api/**'
//     */
//    @RequestMapping(value = "{_:^(?!index\\.html|api).*$}")
//    public String redirectApi() {
//        return "forward:/";
//    }

	// Match everything without a suffix (so not a static resource)
	@RequestMapping(value = "/**/{path:[^.]*}")
	public String redirect() {
		// Forward to home page so that route is preserved.
		return "forward:/";
	}
}
